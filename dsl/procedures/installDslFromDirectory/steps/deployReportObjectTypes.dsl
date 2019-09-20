import groovy.io.FileType
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/summaryString]

def absDir='$[/myJob/CWD]'
File dir=new File(absDir, "reportObjectTypes")

if (dir.exists()) {
  def counters=loadObjects('reportObjectType', absDir)
  def nb=counters['resourcePool']
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName:"summary", value:"No reportObjectTypes")
}
