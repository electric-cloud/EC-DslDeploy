import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def reportNbr
def dashNbr
def catNbr

project projectName, {
  catNbr  = loadCatalogs(projectDir, projectName)
  reportNbr  = loadReports(projectDir, projectName)
  dashNbr = loadDashboards(projectDir, projectName)
}

def summaryStr="Created:"
summaryStr += catNbr? "\n$catNbr catalogs" : ""
summaryStr += reportNbr? "\n$reportNbr reports" : ""
summaryStr += dashNbr? "\n$dashNbr dashboards" : ""
//summaryStr += $Nbr? "\n$Nbr " : ""
//summaryStr += $Nbr? "\n$Nbr " : ""
//summaryStr += $Nbr? "\n$Nbr " : ""

setProperty(propertyName: "summary", value: summaryStr)
