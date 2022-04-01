/*
  renameInChangeList.groovy - Loop through a change list and perform modify/rename actions on objects
    Renames are accomplished by using modify{Object} and employing the "newName" parameter.
    So the first part of the command identifies the object by its "old" name.
    Then the "newName" parameters is set to the "new" name.

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
// Parse the change list checking for files which should be renamed
def renames = false
def changeList = [:]
def jsonSlurp = new groovy.json.JsonSlurper()
if ("$changeListText" != "") {
    try {
        changeList = jsonSlurp.parseText(changeListText)
        if (changeList instanceof Map && changeList.what == "INCREMENTAL" && changeList.renamed instanceof Map && changeList.renamed.size() > 0) {
            renames = true
        }
        println changeList
    } catch (Exception ex) {
        println "Error parsing change list text: " + ex.getMessage()
    }
}

// Process rename actions and/or summarize activity
if (renames) {
    def countSuccess = 0
    def count = 0
    changeList.renamed.each {oldFilePath, newFilePath ->
        def command = pathToModifyCommand(oldFilePath as String)
        def parameters = pathToParameterList(oldFilePath as String)
        def newName = pathToObjectName(newFilePath as String)
        parameters.add("newName:'" + newName + "'")
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

    ef.setProperty(propertyName:"summary", value:"$countSuccess renames completed of $count")
    if (countSuccess < count) {
        ef.setProperty(propertyName:"outcome", value:"warning")
    }
} else {
    if (incremental) {
        ef.setProperty(propertyName: "summary", value: "No renames in the change list")
    } else {
        ef.setProperty(propertyName: "summary", value: "Skipped ... not an incremental import")
    }
}