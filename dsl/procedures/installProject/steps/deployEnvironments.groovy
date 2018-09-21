import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def envNbr


project projectName, {
  envNbr  = loadEnvironments(projectDir, projectName)
}

def summaryStr="Created:"
summaryStr += envNbr? "$envNbr environments" : ""

setProperty(propertyName: "summary", value: summaryStr)
