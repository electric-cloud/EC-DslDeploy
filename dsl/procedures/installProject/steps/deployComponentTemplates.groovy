import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def compNbr

project projectName, {
  compNbr  = loadComponents(projectDir, projectName)
}

def summaryStr = compNbr? "Created $compNbr component templates" : "No component templates"
setProperty(propertyName: "summary", value: summaryStr)
return ""
