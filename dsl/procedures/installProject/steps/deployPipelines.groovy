import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def pipeNbr
def relNbr
def svrNbr
def appNbr

def summaryStr = ""
project projectName, {
  appNbr  = loadApplications(projectDir, projectName)
  svrNbr  = loadServices(projectDir, projectName)
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
  relNbr  = loadReleases(projectDir, projectName)
}

summaryStr += "Created:"
summaryStr += relNbr?  "\n$relNbr releases" : ""

if (pipeNbr!= -1) {
  summaryStr += pipeNbr? "\n$pipeNbr pipelines" : ""
}
summaryStr += appNbr? "\n$appNbr applications" : ""
summaryStr += svrNbr? "\n$svrNbr services" : ""

setProperty(propertyName: "summary", value: summaryStr)
