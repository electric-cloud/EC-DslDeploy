/*
  deployMain.groovy - deploy the top level main.groovy if it exists

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version

*/

import java.io.File
import com.electriccloud.client.groovy.ElectricFlow

ElectricFlow ef = new ElectricFlow()
def ret
File topDSLFile = new File(".", 'main.groovy')
if (topDSLFile.exists()) {
  ret=ef.evalDsl(dsl: topDSLFile.text)
  ef.setProperty(propertyName: "summary", value: "main.groovy loaded")
}
