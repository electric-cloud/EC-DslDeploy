package com.electriccloud.commander.dsl.util

import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.util.XmlSlurper
import java.io.File

import org.codehaus.groovy.control.CompilerConfiguration
import com.electriccloud.commander.dsl.DslDelegatingScript

abstract class BaseProject extends DslDelegatingScript {

	def getProjectDSLFile(File projectDir) {
		File projDSLFile = new File(projectDir, 'project.dsl')
		if(projDSLFile.exists()) {
			return projDSLFile
		} else {
			return new File(projectDir, 'project.groovy')
		}
	}

	def loadProject(String projectDir, String projectName) {
		// load the project.groovy
		println "Entering loadProject(" +  projectDir.toString() + ",$projectName)"

		File dslFile=getProjectDSLFile(projectDir).absolutePath;
		def proj=evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
	}

	def loadProjectProperties(String projectDir, String projectName) {
		println "Entering loadProjectProperties(" +  projectDir.toString() + ",$projectName)"

		// Recursively navigate each file or sub-directory in the properties directory
		//Create a property corresponding to a file,
		// or create a property sheet for a sub-directory before navigating into it
		projPropDir=new File(projectDir, 'dsl/properties')
		if (projPropertyDir.isDirectory) {
			loadNestedProperties("/projects/$projectName", projPropertyDir)
		}	else {
			println "No property directory for project $projectName"
		}
	}

	def loadNestedProperties(String propRoot, File propsDir) {
		println "Entering loadNestedProperties($propRoot," +  propsDir.toString() + ")"
		propsDir.eachFile { dir ->
			println "  parsing " + dir.toString()
			int extension = dir.name.lastIndexOf('.')
			int endIndex = extension > -1 ? extension : dir.name.length()
			String propName = dir.name.substring(0, endIndex)
			String propPath = "${propRoot}/${propName}"
			if (dir.directory) {
				property propName, {
					loadNestedProperties(propPath, dir)
				}
			} else {
				def exists = getProperty(propPath, suppressNoSuchPropertyException: true, expand: false)
				if (exists) {
					modifyProperty propertyName: propPath, value: dir.text
				} else {
					createProperty propertyName: propPath, value: dir.text
				}

			}
		}
	}

	def loadProcedures(String projectDir, String projectName, List stepsWithAttachedCredentials) {

		// Loop over the sub-directories in the procedures directory
		// and evaluate procedures if a procedure.dsl file exists

		File procsDir = new File(projectDir, 'dsl/procedures')
		procsDir.eachDir {

			File procDslFile = getProcedureDSLFile(it)
			if (procDslFile?.exists()) {
				println "Processing procedure DSL file ${procDslFile.absolutePath}"
				def proc = loadProcedure(projectDir, projectName, procDslFile.absolutePath)

				//create formal parameters using form.xml
				File formXml = new File(it, 'form.xml')
				if (formXml.exists()) {
					println "Processing form XML $formXml.absolutePath"
					buildFormalParametersFromFormXml(proc, formXml)
				}

			}

		}

		// project boiler-plate
		setupprojectMetadata(projectDir, projectName, stepsWithAttachedCredentials)
	}

	def getProcedureDSLFile(File procedureDir) {

		if (procedureDir.name.toLowerCase().endsWith('_ignore')) {
			return null
		}

		File procDSLFile = new File(procedureDir, 'procedure.dsl')
		if(procDSLFile.exists()) {
			return procDSLFile
		} else {
			return new File(procedureDir, 'procedure.groovy')
		}
	}

	def loadProcedure(String projectDir, String projectName, String dslFile) {
		return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
	}

	//Helper function to load another dsl script and evaluate it in-context
	def evalInlineDsl(String dslFile, Map bindingMap) {

		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setScriptBaseClass(DelegatingScript.class.getName());
		GroovyShell sh = new GroovyShell(this.class.classLoader, bindingMap? new Binding(bindingMap) : new Binding(), cc);
		DelegatingScript script = (DelegatingScript)sh.parse(new File(dslFile))
		script.setDelegate(this);
		return script.run();
	}

	def nullIfEmpty(def value) {
		value == '' ? null : value
	}

	def buildFormalParametersFromFormXml(def proc, File formXml) {

		def formElements = new XmlSlurper().parseText(formXml.text)

		procedure proc.procedureName, {

			ec_parameterForm = formXml.text
			formElements.formElement.each { formElement ->
				def expansionDeferred = formElement.expansionDeferred == "true" ? "1" : "0"
				println "expansionDeferred: ${formElement.property}: $expansionDeferred"

				formalParameter "$formElement.property",
						defaultValue: formElement.value,
						required: nullIfEmpty(formElement.condition) ? 0 : ( nullIfEmpty(formElement.required) ?: 0 ),
						description: formElement.documentation,
						type: formElement.type,
						label: formElement.label,
						expansionDeferred: expansionDeferred

				if (formElement['attachedAsParameterToStep'] && formElement['attachedAsParameterToStep'] != '') {
					formElement['attachedAsParameterToStep'].toString().split(',').each { attachToStep ->
						println("Attaching parameter $formElement.property to step $attachToStep")
						attachParameter(projectName: proc.projectName,
								procedureName: proc.procedureName,
								stepName: attachToStep,
								formalParameterName: formElement.property)
					}
				}

				//setup custom editor data for each parameter
				property 'ec_customEditorData', procedureName: proc.procedureName, {
					property 'parameters', {
						property "$formElement.property", {
							formType = 'standard'
							println "Form element $formElement.property, type: '${formElement.type.toString()}'"
							if ('checkbox' == formElement.type.toString()) {
								checkedValue = formElement.checkedValue?:'true'
								uncheckedValue = formElement.uncheckedValue?:'false'
								initiallyChecked = formElement.initiallyChecked?:'0'
							} else if ('select' == formElement.type.toString() ||
									'radio' == formElement.type.toString()) {
								int count = 0
								property "options", {
									formElement.option.each { option ->
										count++
										property "option$count", {
											property 'text', value: "${option.name}"
											property 'value', value: "${option.value}"
										}
									}
									type = 'list'
									optionCount = count
								}
							}
						}
					}
				}

			}
		}
	}

}
