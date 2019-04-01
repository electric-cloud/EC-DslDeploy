import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def resName = '$[resName]'
def resDir = '$[resDir]'

resource resName, {
  loadResource(resDir, resName)
  loadResourceProperties(resDir, resName)
}
return ""
