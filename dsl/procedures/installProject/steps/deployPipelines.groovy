/*
  deployPipelines.groovy - Loop through the pipelines and invoke each
      individually, including subObjects like stages and tasks

  Copyright 2019 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2019-04-01  lrochette  Convert to loadObjects
*/
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'
def counters


project projectName, {
  counters = loadObjects("pipeline", projectDir, "/projects/$projectName",
    [projectName: projectName, projectDir: projectDir]
  )
}

def pNbr = counters['pipeline']
def sNbr = counters['stage']
def tNbr = counters['task']

def summaryStr="Created:"
summaryStr += " \n  "
summaryStr += pNbr? "$pNbr pipelines"  : "no pipelines"
summaryStr += sNbr? "\n  $sNbr stages" : ""
summaryStr += tNbr? "\n  $tNbr tasks"  : ""

setProperty(propertyName: "summary", value: summaryStr)
return ""
