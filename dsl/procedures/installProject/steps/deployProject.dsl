/*
  deployProject.groovy - Invoke the project.groovy
    The looping is done outside to help with error management
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
def counter

project projectName, {
  counter=loadProject(projectDir, projectName)
  loadProjectProperties(projectDir, projectName)
  loadProjectAcls(projectDir, projectName)
}

if (counter == 0) {
  setProperty(propertyName: "summary", value: "No project.groovy found")
  setProperty(propertyName: "outcome", value: "warning")
}
return ""
