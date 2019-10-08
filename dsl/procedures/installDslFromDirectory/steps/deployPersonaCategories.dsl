/*
  personaCategories.groovy - Loop through the persona categories and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-02  lrochette Initial Version
*/

import groovy.io.FileType
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/summaryString]

def absDir='$[/myJob/CWD]'
def overwrite = '$[overwrite]'
File dir=new File(absDir, "personaCategories")

if (dir.exists()) {
  def counters=loadObjects('personaCategory', absDir, "/", [:], overwrite, true)
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName:"summary", value:"No persona categories")
}
