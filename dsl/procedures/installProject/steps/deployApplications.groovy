import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def appNbr

project projectName, {
  appNbr  = loadApplications(projectDir, projectName)
}

def summaryStr = appNbr? "Created $appNbr applications" : "No applications"
setProperty(propertyName: "summary", value: summaryStr)
return ""
