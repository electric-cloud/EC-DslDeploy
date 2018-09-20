import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def pipeNbr
def relNbr
def envNbr
def svrNbr
def appNbr
def reportNbr
def dashNbr
def procNbr

project projectName, {
  envNbr  = loadEnvironments(projectDir, projectName)
  appNbr  = loadApplications(projectDir, projectName)
  svrNbr  = loadServices(projectDir, projectName)
  pipeNbr = loadPipelines(projectDir, projectName)
  relNbr  = loadReleases(projectDir, projectName)
  catNbr  = loadCatalogs(projectDir, projectName)
  reportNbr  = loadReports(projectDir, projectName)
  dashNbr = loadDashboards(projectDir, projectName)
  property "deployedBy", value: "$[/myProject/projectName]"
  property "deployedWhen", value: "$[/timestamp YYYY-MM-DD hh:mm:ss]"
}

def summaryStr="Created:"
summaryStr += $relNbr?  "\n$relNbr releases" : ""
summaryStr += $pipeNbr? "\n$pipeNbr pipelines" : ""
summaryStr += $envNbr? "\n$envNbr environments" : ""
summaryStr += $appNbr? "\n$appNbr applications" : ""
summaryStr += $catNbr? "\n$catNbr catalogs" : ""
summaryStr += $reportNbr? "\n$reportNbr reports" : ""
summaryStr += $dashNbr? "\n$dashNbr dashboards" : ""
summaryStr += $svrNbr==0? "\n$svrNbr services" : ""
//summaryStr += $Nbr? "\n$Nbr " : ""
//summaryStr += $Nbr? "\n$Nbr " : ""
//summaryStr += $Nbr? "\n$Nbr " : ""

setProperty(propertyName: "summary", value: summaryStr)
