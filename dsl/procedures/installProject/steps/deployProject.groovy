import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

project projectName, {
  loadProject(projectDir, projectName)
	loadProjectProperties(projectDir, projectName)
	loadProcedures(projectDir, projectName, stepsWithAttachedCredentials)
}
