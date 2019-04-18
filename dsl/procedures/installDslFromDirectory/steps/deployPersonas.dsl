/*
  deployPersonas.groovy - Loop through the personas and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-02  lrochette Initial Version
*/

import groovy.io.FileType
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

$[/myProject/scripts/summaryString]

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

File dir=new File('$[directory]', "personas")

if (dir.exists()) {
  def counters=loadObjects('persona', '$[directory]')
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName:"summary", value:"No personas")
}
