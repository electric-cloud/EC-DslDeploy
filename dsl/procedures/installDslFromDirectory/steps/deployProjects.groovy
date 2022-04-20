/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version
  2018-08-30  lrochette sorting project folder alphabetically
  2022-03-17  mayasse  adding change list/incremental/partial import features

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

// Is this an incremental import?
def incremental = false
try {
    incremental = (ef.getProperty(propertyName:"/myJob/incrementalImport").property.value == "1") ? true : false
} catch (Exception ex) {
    println "Could not get incrementalImport value due to: ${ex.message}"
    incremental = false
}

println "Incremental Import: $incremental"

def changeListText = "";
if (incremental) {
//    // Gather change list text
//    def fileName = "[dest]/change_list.json"
//    // if file exists, is not a folder and is readable...
//    def changeListFile = new File(fileName)
//    if (changeListFile.exists() && changeListFile.isFile()) {
//        changeListText = changeListFile.text
//    } else {
//        println("'$fileName' may be a folder or unreadable or not exist");
//    }
    try {
        changeListText = ef.getProperty(propertyName: "/myJob/change_list.json").property.value
    } catch (Exception ex) {
        changeListText = "";
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
        if (!incremental || (
                incremental && (
                    changeListText.indexOf('"what":"INITIAL"') > -1) ||
                    changeListText.indexOf(""""projects/$basename/""") > -1)) {
            println "Processing project $basename"
            def escapedProjName = StringEscapeUtils
                .escapeJava(basename)
            def params = [new ActualParameter('projName', escapedProjName),
                          new ActualParameter('projDir', projDir
                              .absolutePath
                              .toString()
                              .replace('\\', '/')),
                          new ActualParameter('overwrite', '$[overwrite]'),
                          new ActualParameter('additionalDslArguments', '$[additionalDslArguments]'),
                          new ActualParameter('ignoreFailed', '$[ignoreFailed]'),
                          new ActualParameter('localMode', '$[localMode]'),
                          new ActualParameter('includeObjects', '''$[includeObjects]'''),
                          new ActualParameter('excludeObjects', '''$[excludeObjects]''')]
            ef
                .createJobStep(jobStepName: basename,
                    subproject: '$[/myProject]',
                    subprocedure: 'installProject',
                    actualParameters: params)
        } else {
            println("Skip importing $basename project, it is not in the change list ")
        }
    } else {
      println("Skip importing of $basename project as it's not answer " +
              "includeObjects/excludeObjects parameters")
    }
  }
} else {
  ef.setProperty(propertyName:"summary", value:" No projects")
}
