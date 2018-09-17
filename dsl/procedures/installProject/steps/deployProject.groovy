import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def pipeNbr=0
def relNbr=0
def envNbr=0
def envSvr
project projectName, {
  loadProject(projectDir, projectName)
  loadProjectProperties(projectDir, projectName)
  loadProcedures(projectDir, projectName)
  envNbr  = loadEnvironments(projectDir, projectName)
  envSvr  = loadServices(projectDir, projectName)
  pipeNbr = loadPipelines(projectDir, projectName)
  relNbr  = loadReleases(projectDir, projectName)

  property "deployedBy", value: "$[/myProject/projectName]"
  property "deployedWhen", value: "$[/timestamp YYYY-MM-DD hh:mm:ss]"
}
setProperty(propertyName: "summary", value: """Created:
$relNbr releases
$pipeNbr pipelines
$envNbr environments
$envSvr services""")
//setProperty(propertyName: "summary", value: "$relNbr releases created\n$pipeNbr pipelines created")
