import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir  = '$[projDir]'

def catNbr

project projectName, {
  catNbr  = loadCatalogs(projectDir, projectName)
}

def summaryStr = catNbr? "Created $catNbr catalogs" : "No catalogs"
setProperty(propertyName: "summary", value: summaryStr)
