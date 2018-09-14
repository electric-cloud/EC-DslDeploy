import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def pipeNbr=0
project projectName, {
  loadProject(projectDir, projectName)
  loadProjectProperties(projectDir, projectName)
  loadProcedures(projectDir, projectName)
  loadEnvironments(projectDir, projectName)
  loadServices(projectDir, projectName)
  loadPipelines(projectDir, projectName)

  property "deployedBy", value: "$[/myProject/projectName]"
  property "deployedWhen", value: "$[/timestamp YYYY-MM-DD hh:mm:ss]"
}
setProperty(propertyName: "summary", value: "$pipeNbr pipelines created")
