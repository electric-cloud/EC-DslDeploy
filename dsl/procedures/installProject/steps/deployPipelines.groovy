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


project projectName, {
  appNbr  = loadApplications(projectDir, projectName)
  svrNbr  = loadServices(projectDir, projectName)
  pipeNbr = loadPipelines(projectDir, projectName)
  relNbr  = loadReleases(projectDir, projectName)
}

def summaryStr="Created:"
summaryStr += $relNbr?  "\n$relNbr releases" : ""
summaryStr += $pipeNbr? "\n$pipeNbr pipelines" : ""
summaryStr += $appNbr? "\n$appNbr applications" : ""
summaryStr += $svrNbr==0? "\n$svrNbr services" : ""

setProperty(propertyName: "summary", value: summaryStr)
