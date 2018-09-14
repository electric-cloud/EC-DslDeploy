package com.electriccloud.commander.dsl.util

import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.util.XmlSlurper
import java.io.File

import org.codehaus.groovy.control.CompilerConfiguration
import com.electriccloud.commander.dsl.DslDelegatingScript

abstract class BaseProject extends DslDelegatingScript {
	// return the object.groovy or object.dsl
	//		AKA project.groovy, procedure.dsl, pipeline.groovy, ...
	def getObjectDSLFile(File objDir, String obj) {
		File dslFile = new File(objDir, obj + '.groovy')
		if (dslFile.exists()) {
			return dslFile
		} else {
			dslFile = new File(objDir, obj + '.dsl')
			if (dslFile.exists()) {
				return dslFile
			} else {
				return null
			}
		}
	}

	def loadProject(String projectDir, String projectName) {
		// load the project.groovy if it exists
		File dslFile=getObjectDSLFile(new File(projectDir), "project");
		if (dslFile?.exists()) {
			println "Processing project DSL file ${dslFile.absolutePath}"
		  def proj=evalInlineDsl(dslFile.toString(), [projectName: projectName, projectDir: projectDir])
		}
	}

	def loadProjectProperties(String projectDir, String projectName) {
		// Recursively navigate each file or sub-directory in the properties directory
		//Create a property corresponding to a file,
		// or create a property sheet for a sub-directory before navigating into it
		def projPropertyDir=new File(projectDir, 'properties')
		if (projPropertyDir.directory) {
			loadNestedProperties("/projects/$projectName", projPropertyDir)
		}	else {
			println "No properties directory for project $projectName"
		}
	}

	def loadNestedProperties(String propRoot, File propsDir) {
		// println "Entering loadNestedProperties($propRoot," +  propsDir.toString() + ")"
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

	def loadProcedure(String projectDir, String projectName, String dslFile) {
		return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
	}

	def loadProcedures(String projectDir, String projectName) {
		// Loop over the sub-directories in the procedures directory
		// and evaluate procedures if a procedure.dsl file exists

		File procsDir = new File(projectDir, 'procedures')

		if (procsDir.exists()) {
			// sort procedures alpahbetically
			procsDir.eachDir {

				File procDslFile = getObjectDSLFile(it, "procedure")
				if (procDslFile?.exists()) {
					println "Processing procedure DSL file ${procDslFile.absolutePath}"
					def proc = loadProcedure(projectDir, projectName, procDslFile.absolutePath)

					//create formal parameters using form.xml
					File formXml = new File(it, 'form.xml')
					if (formXml.exists()) {
						println "Processing form XML $formXml.absolutePath"
						buildFormalParametersFromFormXmlToProcedure(proc, formXml)
					}
				}
			}
		}
	}

	def loadPipeline(String projectDir, String projectName, String dslFile) {
		return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
	}

	def loadPipelines(String projectDir, String projectName) {
		// Loop over the sub-directories in the pipelines directory
		// and evaluate pipelines if a pipeline.dsl file exists
		// println "Entering loadPipelines for $projectDir ($projectName)"
		File pipesDir = new File(projectDir, 'pipelines')
		if (pipesDir.exists()) {
			// sort pipelines alphabetically
			def dlist=[]
			pipesDir.eachDir() {dlist << it}
			dlist.sort({it.name})
			dlist.each { fdir ->
			// pipesDir.eachDir { fdir ->
				print "looping "+ fdir.name
				File dslFile = getObjectDSLFile(fdir, "pipeline")
				if (dslFile?.exists()) {
					println "Processing pipeline DSL file ${dslFile.absolutePath}"
					def pipe = loadPipeline(projectDir, projectName, dslFile.absolutePath)
					//create formal parameters using form.xml
					File formXml = new File(fdir, 'form.xml')
					if (formXml.exists()) {
						println "Processing form XML $formXml.absolutePath"
						buildFormalParametersFromFormXmlToPipeline(pipe, formXml)
					}
				}
			}
		}
	}

	def loadService(String projectDir, String projectName, String dslFile) {
		return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
	}
	def loadServices(String projectDir, String projectName) {
		// Loop over the sub-directories in the microservices directory
		// and evaluate services if a service.dsl file exists

		File dir = new File(projectDir, 'services')
		if (dir.exists()) {
			dir.eachDir {
				File dslFile = getObjectDSLFile(it, "service")
				if (dslFile?.exists()) {
					println "Processing pipeline DSL file ${dslFile.absolutePath}"
					def pipe = loadService(projectDir, projectName, dslFile.absolutePath)
				}
			}
		}
	}

	def loadEnvironment(String projectDir, String projectName, String dslFile) {
		return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
	}
	def loadEnvironments(String projectDir, String projectName) {
		// Loop over the sub-directories in the environments directory
		// and evaluate services if a service.dsl file exists

		File dir = new File(projectDir, 'environments')
		if (dir.exists()) {
			dir.eachDir {
				File dslFile = getObjectDSLFile(it, "environment")
				if (dslFile?.exists()) {
					println "Processing environment DSL file ${dslFile.absolutePath}"
					def pipe = loadEnvironment(projectDir, projectName, dslFile.absolutePath)
				}
			}
		}
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

	def buildFormalParametersFromFormXmlToProcedure(def proc, File formXml) {

		def formElements = new XmlSlurper().parseText(formXml.text)

		procedure proc.procedureName, {

			ec_parameterForm = formXml.text
			formElements.formElement.each { formElement ->
				def expansionDeferred = formElement.expansionDeferred == "true" ? "1" : "0"
				// println "expansionDeferred: ${formElement.property}: $expansionDeferred"

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

	def buildFormalParametersFromFormXmlToPipeline(def pipe, File formXml) {

		def formElements = new XmlSlurper().parseText(formXml.text)

		pipeline pipe.pipelineName, {

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

				//setup custom editor data for each parameter
				property 'ec_customEditorData', pipelineName: pipe.pipelineName, {
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
