/*
  deployEnvironments.groovy - Loop through the environments and invoke each
      individually. And with subObjects: clusters and environmentTiers

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-01  lrochette  Convert to loadObjects
*/
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/summaryString]

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'
def overwrite = '$[overwrite]'
def counters

project projectName, {
  counters = loadObjects('environment', projectDir, "/projects/$projectName",
     [projectName: projectName, projectDir: projectDir], overwrite
   )
}

setProperty(propertyName: "summary", value: summaryString(counters))
return ""
