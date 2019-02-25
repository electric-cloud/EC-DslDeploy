import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir  = '$[projDir]'

def catNbr
def itemNbr

project projectName, {
  (catNbr, itemNbr)  = loadCatalogs(projectDir, projectName)
}

def summaryStr="Created:"
summaryStr += catNbr?  "\n $catNbr catalogs" : "No catalogs"
summaryStr += itemNbr? "\n $itemNbr catalog items" : "No catalog items"

setProperty(propertyName: "summary", value: summaryStr)
