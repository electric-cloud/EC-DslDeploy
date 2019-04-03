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

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

File persDir=new File('$[directory]', "personas")

if (persDir.exists()) {
  def counter=loadObjects('persona', '$[directory]')
  setProperty(propertyName:"summary", value:" $counter personas")
} else {
  setProperty(propertyName:"summary", value:" No personas")
}
