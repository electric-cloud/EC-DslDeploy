import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

// Variables available for use in DSL code
def persName = '$[persName]'
def persDir = '$[persDir]'

persona persName, {
  loadObject("persona", persDir, persName, [:])
  loadObjectProperties("persona", persDir, persName)
}
return ""
