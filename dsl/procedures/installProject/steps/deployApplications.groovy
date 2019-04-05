/*
  deployApplications.groovy - Loop through the applications and invoke each individually

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
def projectDir = '$[projDir]'
def counters

project projectName, {
  counters = loadObjects("application", projectDir, "/projects/$projectName",
    [projectName: projectName, projectDir: projectDir]
  )
}

def nb=counters['application']
def summaryStr = nb? "Created $nb applications" : "No applications"
setProperty(propertyName: "summary", value: summaryStr)
return ""
