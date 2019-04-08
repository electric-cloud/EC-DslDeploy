/*
  deployCatalogs.groovy - Loop through the catalogs and invoke each individually,
       including children: catalogItems

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-04  lrochette  Convert to loadObjects
*/
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/summaryString]

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir  = '$[projDir]'
def counters

project projectName, {
   counters = loadObjects('catalog', projectDir, "/projects/$projectName",
     [projectName: projectName, projectDir: projectDir]
   )
}

setProperty(propertyName: "summary", value: summaryString(counters))
return ""
