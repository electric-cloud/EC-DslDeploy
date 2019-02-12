import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def procNbr

project projectName, {
  loadProject(projectDir, projectName)
  loadProjectProperties(projectDir, projectName)
  procNbr = loadProcedures(projectDir, projectName)
  property "deployedBy", value: "$[/myProject/projectName]"
  property "deployedWhen", value: "$[/timestamp YYYY-MM-DD hh:mm:ss]"
}

def summaryStr = $procNbr? "Created $procNbr procedures" : "No procedures"
setProperty(propertyName: "summary", value: summaryStr)
