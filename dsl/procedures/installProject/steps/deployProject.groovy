import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def pipeNbr
def relNbr=0
def envNbr=0
def svrNbr=0
def appNbr=0
def reportNbr=0
def dashNbr=0
def procNbr=0

project projectName, {
  loadProject(projectDir, projectName)
  loadProjectProperties(projectDir, projectName)
  procNbr = loadProcedures(projectDir, projectName)
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
summaryStr += $procNbr? "\n$procNbr procedures" : ""
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
