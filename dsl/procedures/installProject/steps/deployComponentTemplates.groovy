import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def compNbr

def summaryStr = ""
project projectName, {
  compNbr  = loadComponents(projectDir, projectName)
}

summaryStr += "Created:"
summaryStr += compNbr? " $compNbr component templates" : ""

setProperty(propertyName: "summary", value: summaryStr)
