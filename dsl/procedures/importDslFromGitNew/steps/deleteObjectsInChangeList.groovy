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
import java.io.File

//noinspection GroovyUnusedAssignment
//@BaseScript BaseObject baseScript

$[/myProject/scripts/Utils]

ElectricFlow ef = new ElectricFlow()

/// Gather change list text from a specified or default file name
println "Incremental Import: $[incrementalImport]"

def incremental = ("$[incrementalImport]" != "0")
def changeListText = "";
if (incremental) {
    def fileName = "$[incrementalImport]" == "1" ? "$[dest]/change_list.json" : "$[incrementalImport]"
    // if file exists, is not a folder and is readable...
    def changeListFile = new File(fileName)
    if (changeListFile.exists() && changeListFile.isFile()) {
        changeListText = changeListFile.text
    } else {
        println("'$fileName' may be a folder or unreadable or not exist");
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
        //println changeList
    } catch (Exception ex) {
        println "Error parsing change list text: " + ex.getMessage()
    }
}

// Process delete actions and/or summarize activity
if (deletes) {
    def countSuccess = 0
    def count = 0
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
    println ""
    println "${command}(${parameters.join(",")})"
    println ex.getMessage();
    println ""
}
success;
"""
        count++
        // command could come back blank if we were not able to determine the proper command
        if (command != "" && evaluate(groovyDsl)) {
            println "Evaluated: " + "${command}(${parameters.join(",")})"
            countSuccess++
        }
    }

    ef.setProperty(propertyName:"summary", value:"$countSuccess deletes completed of $count")
    if (countSuccess < count) {
        ef.setProperty(propertyName:"outcome", value:"warning")
    }
} else {
    if (incremental) {
        ef.setProperty(propertyName: "summary", value: "No deletes in the change list")
    } else {
        ef.setProperty(propertyName: "summary", value: "Skipped ... not an incremental import")
    }
}