/*
  deleteObjectsInChangeList.groovy - Loop through a change list and perform delete actions on objects

  Copyright 2022 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2022-03-17  mayasse Initial Version

*/
import groovy.io.FileType
import groovy.json.StringEscapeUtils
import groovy.transform.BaseScript
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.apis.model.*
import com.electriccloud.client.groovy.models.ActualParameter
//import com.electriccloud.commander.dsl.util.BaseObject
import java.io.File

//noinspection GroovyUnusedAssignment
//@BaseScript BaseObject baseScript

$[/myProject/scripts/Utils]

ElectricFlow ef = new ElectricFlow()

// Gather change list text from either a property name or a filename
println "pathToFileList      : $[pathToFileList]"
println "propertyWithFileList: $[propertyWithFileList]"
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

// Parse the change list checking for files which should be deleted
def deletes = false
def changeList = [:]
def jsonSlurp = new groovy.json.JsonSlurper()
if ("$changeListText" != "") {
    try {
        changeList = jsonSlurp.parseText(changeListText)
        if (changeList instanceof Map && changeList.what == "INCREMENTAL" && changeList.deleted instanceof List && changeList.deleted.size() > 0) {
            deletes = true
        }
    } catch (Exception ex) {
        println "Error parsing change list text: " + ex.getMessage()
    }
}

// Process delete actions and/or summarize activity
if (deletes) {
    def countSuccess = 0
    changeList.deleted.each {filePathToDelete ->
        def command = pathToDeleteCommand(filePathToDelete as String)
        def parameters = pathToParameterList(filePathToDelete as String)
        def groovyDsl = """
import com.electriccloud.client.groovy.ElectricFlow;
import com.electriccloud.client.groovy.models.*;
ElectricFlow ef = new ElectricFlow();
def success = false;
try {
    ef.${command}(${parameters.join(",")});
    success = true;
} catch (Exception ex) {
    println "${command}(${parameters.join(",")})"
    println ex.getMessage();
}
success;
"""
        // command could come back blank if we were not able to determine the proper command
        if (command != "" && evaluate(groovyDsl)) {
            println "Evaluate: " + groovyDsl
            countSuccess++
        }
    }

    ef.setProperty(propertyName:"summary", value:"$countSuccess deletes issued")
} else {
    ef.setProperty(propertyName:"summary", value:"No deletes in the change list")
}