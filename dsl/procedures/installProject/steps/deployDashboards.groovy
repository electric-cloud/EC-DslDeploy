/*
  deployDashboards.groovy - Loop through the catalogs and invoke each individually,
       including children: reports and filters

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-05  lrochette  Convert to loadObjects
*/
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir  = '$[projDir]'
def counters

project projectName, {
   counters = loadObjects('dashboard', projectDir, "/projects/$projectName",
     [projectName: projectName, projectDir: projectDir]
   )
}

def dashNbr   = counters['dashboard']
def widgetNbr = counters['widget']

def summaryStr="Created:"
summaryStr += "\n  "
summaryStr += dashNbr? "$dashNbr dashboards" : "No dashboards"
summaryStr += "\n  "
summaryStr += widgetNbr? "$widgetNbr widgets" : "No widgets"

setProperty(propertyName: "summary", value: summaryStr)
return ""
