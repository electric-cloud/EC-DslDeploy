/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version
  2018-08-30  lrochette sorting project folder alphabetically

*/
import groovy.io.FileType
import groovy.json.StringEscapeUtils
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.apis.model.*
import com.electriccloud.client.groovy.models.ActualParameter
import java.io.File

$[/myProject/scripts/Utils]

ElectricFlow ef = new ElectricFlow()

def includeObjectsParam = '''$[includeObjects]'''
def excludeObjectsParam = '''$[excludeObjects]'''
def includeObjects = [];
if (!includeObjectsParam.isEmpty()) {
  includeObjects = includeObjectsParam.split( '\n' )
}
def excludeObjects = [];
if (!excludeObjectsParam.isEmpty()) {
  excludeObjects = excludeObjectsParam.split( '\n' )
}
File pDir=new File("projects")
if (pDir.exists()) {

  println(includeObjects)
  println(excludeObjects)
  if (!isIncluded(includeObjects, excludeObjects, "/projects")) {
    println("not included")
    return
  }

  // sort projects alpahbetically
  dlist=[]
  pDir.eachDir {dlist << it }
  dlist.sort({it.name}).each { projDir ->

    def basename = decode(projDir.getName().toString())

    if (isIncluded(includeObjects, excludeObjects, "/projects/$basename")) {

      println "Processing project $basename"
      def escapedProjName = StringEscapeUtils.escapeJava(basename)
      def params = [
              new ActualParameter('projName', escapedProjName),
              new ActualParameter('projDir', projDir.absolutePath.toString().replace('\\', '/')),
              new ActualParameter('overwrite', '$[overwrite]'),
              new ActualParameter('additionalDslArguments', '$[additionalDslArguments]'),
              new ActualParameter('ignoreFailed', '$[ignoreFailed]'),
              new ActualParameter('localMode', '$[localMode]'),
              new ActualParameter('includeObjects', '''$[includeObjects]'''),
              new ActualParameter('excludeObjects', '''$[excludeObjects]''')
      ]

      ef.createJobStep(
              jobStepName: basename,
              subproject: '$[/myProject]',
              subprocedure: 'installProject',
              actualParameters: params
      )
    } else {
      println("Skip importing of $basename project as it's not answer " +
              "includeObjects/excludeObjects parameters")
    }
  }
} else {
  ef.setProperty(propertyName:"summary", value:" No projects")
}
