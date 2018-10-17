import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def svrNbr

def summaryStr = ""
project projectName, {
  svrNbr  = loadServices(projectDir, projectName)
}

summaryStr += "Created:"
summaryStr += svrNbr? " $svrNbr services" : ""

setProperty(propertyName: "summary", value: summaryStr)
