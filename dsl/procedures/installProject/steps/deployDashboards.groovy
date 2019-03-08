import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def reportNbr
def dashNbr
def widgetNbr
project projectName, {
  reportNbr  = loadReports(projectDir, projectName)
  (dashNbr, widgetNbr) = loadDashboards(projectDir, projectName)
}

def summaryStr="Created:"
summaryStr += "\n  "
summaryStr += reportNbr? "$reportNbr reports" : "No reports"
summaryStr += "\n  "
summaryStr += dashNbr? "$dashNbr dashboards" : "No dashboards"
summaryStr += "\n  "
summaryStr += dwidgetNbr? "$widgetNbr widgets" : "No widgets"

setProperty(propertyName: "summary", value: summaryStr)
return ""
