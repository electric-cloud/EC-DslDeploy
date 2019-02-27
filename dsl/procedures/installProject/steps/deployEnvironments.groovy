import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'

def envNbr
def clusterNbr

project projectName, {
  println "Process Environments in $projectDir"
  (envNbr, clusterNbr)  = loadEnvironments(projectDir, projectName)
}

def summaryStr="Created:\n  "
summaryStr += envNbr? "$envNbr environments" : "no environment"
summaryStr += "\n  "
summaryStr += clusterNbr? "$clusterNbr clusters" : "no cluster"

setProperty(propertyName: "summary", value: summaryStr)
return ""
