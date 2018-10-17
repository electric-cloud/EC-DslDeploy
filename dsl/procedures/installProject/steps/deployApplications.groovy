import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def appNbr

def summaryStr = ""
project projectName, {
  appNbr  = loadApplications(projectDir, projectName)
}

summaryStr += "Created:"
summaryStr += appNbr? " $appNbr applications" : ""

setProperty(propertyName: "summary", value: summaryStr)
