import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseProject

//noinspection GroovyUnusedAssignment
@BaseScript BaseProject baseScript

// Variables available for use in DSL code
def resourceName = '$[resName]'
def resourceDir = '$[resDir]'

resource resourceName, {
  loadResource(resourceDir, resourceName)
  loadResourceProperties(resourceDir, resourceName)
}
return ""
