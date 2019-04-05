/*
  deployProcedures.groovy - Loop through the procedures and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-01  lrochette  Convert to loadObjects
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
  counters = loadObjects("procedure", projectDir, "/projects/$projectName",
    [],
    [projectName: projectName, projectDir: projectDir]
  )
}

def nb=counters['procedure']
def summaryStr = nb? "Created $nb procedures" : "No procedures"
setProperty(propertyName: "summary", value: summaryStr)
return ""
