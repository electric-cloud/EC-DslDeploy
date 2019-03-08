import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def svrNbr

project projectName, {
  svrNbr  = loadServices(projectDir, projectName)
}

def summaryStr = svrNbr? "Created $svrNbr services" : "No services"
setProperty(propertyName: "summary", value: summaryStr)
return ""
