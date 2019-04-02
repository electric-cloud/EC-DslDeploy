/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-02  lrochette Initial Version

*/
import groovy.io.FileType
import com.electriccloud.client.groovy.ElectricFlow
import java.io.File

ElectricFlow ef = new ElectricFlow()
def counter=0

File pDir=new File("post")
if (pDir.exists()) {
  pDir.eachFileMatch(~/.*\.(dsl|groovy)/) { postDslFile ->
  println "Processing post file " + postDslFile.name
   ef.evalDsl(dsl: postDslFile.text)
   counter++
}
ef.setProperty(propertyName: "summary", value: "$counter files loaded")
} else {
  ef.setProperty(propertyName:"summary", value:" No post files")
}
