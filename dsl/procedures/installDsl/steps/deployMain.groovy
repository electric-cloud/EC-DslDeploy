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

File topDSLFile = new File(".", 'main.groovy')
if (topDSLFile.exists()) {
  ef.evalDsl(dslFile: topDSLFile)
}
