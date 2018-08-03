import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = args.projectName
def projectDir = getProperty("/projects/$projectName/projectDir").value

//List of procedure steps to which the project configuration credentials need to be attached
// ** steps with attached credentials
def stepsWithAttachedCredentials = [
		/*[
			procedureName: 'Procedure Name',
			stepName: 'step that needs the credentials to be attached'
		 ],*/
	]
// ** end steps with attached credentials

project projectName, {

	loadProjectProperties(projectDir, projectName)
	loadProcedures(projectDir, projectName, stepsWithAttachedCredentials)
	//project configuration metadata
	property 'ec_config', {
		form = '$[' + "/projects/${projectName}/procedures/CreateConfiguration/ec_parameterForm]"
		property 'fields', {
			property 'desc', {
				property 'label', value: 'Description'
				property 'order', value: '1'
			}
		}
	}

}
