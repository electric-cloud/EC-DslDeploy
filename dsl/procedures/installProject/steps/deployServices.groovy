/*
  deployServices.groovy - Loop through the services and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-10-17  lrochette  Initial Version
  2019-04-01  lrochette  Convert to loadObjects
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
  counters = loadObjects("service", projectDir, "/projects/$projectName",
    [
      projectName: projectName,
      projectDir: projectDir
    ]
  )
}
def svrNbr=counters['service']

def summaryStr = svrNbr? "Created $svrNbr services" : "No services"
setProperty(propertyName: "summary", value: summaryStr)
return ""
