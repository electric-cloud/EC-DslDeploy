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
def counter=0
File topDslDir = new File(".")

topDslDir.eachFileMatch(~/.*\.(dsl|groovy)/) { topDslFile ->
    println "Loading " + topDslFile.getName()
    ef.evalDsl(dsl: topDslFile.text)
    counter++
}
ef.setProperty(propertyName: "summary", value: "$counter files loaded")
