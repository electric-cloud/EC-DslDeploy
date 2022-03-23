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

println "EC-DslDeploy / Procedure: installDslFromDirectory / Step: deployProjects"
println "pathToFileList      : $[pathToFileList]"
println "propertyWithFileList: $[propertyWithFileList]"

// Gather change list text from either a property name or a filename
def changeListText = "";
// Is there a property named to hold a change list
if ('$[propertyWithFileList]'.size() > 0) {
    try {
        changeListText = getProperty("$[propertyWithFileList]").value
    } catch (Exception ex) {
        println("${ex.message}")
    }
}
// Is there a file path to a change list file
if ('$[pathToFileList]'.size() > 0) {
    // if file exists, is not a folder and is readable...
    def changeListFile = new File('$[pathToFileList]')
    if (changeListFile.exists() && changeListFile.isFile()) {
        changeListText = changeListFile.text
    } else {
        println("'$[pathToFileList]' may be a folder or unreadable or not exist");
    }
}
println("changeListText      : '$changeListText'");

File pDir=new File("projects")
if (pDir.exists()) {

  //println("includeObjects    : $includeObjects")
  //println("excludeObjects    : $excludeObjects")
  if (!isIncluded(includeObjects, excludeObjects, "/projects")) {
    println("not included")
    return
  }

  // sort projects alphabetically
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
              new ActualParameter('excludeObjects', '''$[excludeObjects]'''),
              new ActualParameter('pathToFileList', '''$[pathToFileList]'''),
              new ActualParameter('propertyWithFileList', '''$[propertyWithFileList]''')
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
