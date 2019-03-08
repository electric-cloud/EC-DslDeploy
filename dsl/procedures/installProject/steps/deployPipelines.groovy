import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def pipeNbr
def summaryStr

project projectName, {
  pipeNbr = loadPipelines(projectDir, projectName)
  println "Return pipeNbr: $pipeNbr"
  if (pipeNbr == -1) {
    println "Incorrect parsing of the pipeline file"
    transaction {
      summaryStr += "Skipping form.xml for pipeline"
      setProperty(propertyName: "outcome", value: "warning")
      setProperty(propertyName: "summary", value: "incorrect pipeline type: skipping form.xml")
    }
  }
}


if (pipeNbr != -1) {
  summaryStr = pipeNbr? "Created $pipeNbr pipelines" : "No pipelines"
}

setProperty(propertyName: "summary", value: summaryStr)
return ""
