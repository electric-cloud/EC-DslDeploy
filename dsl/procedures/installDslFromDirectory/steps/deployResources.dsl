/*
  deployResources.groovy - Loop through the resources and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-03-27  lrochette  Initial Version
  2019-04-03  lrochette  Convert to deployObject
*/

import groovy.io.FileType
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/summaryString]

def absDir='$[/myJob/CWD]'
def overwrite = '$[overwrite]'
File resDir=new File(absDir, 'resources')

if (resDir.exists()) {
  def counters=loadObjects('resource', absDir, "/", [:], overwrite, true)
  def nb=counters['resource']
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName:"summary", value:"No resources")
}
