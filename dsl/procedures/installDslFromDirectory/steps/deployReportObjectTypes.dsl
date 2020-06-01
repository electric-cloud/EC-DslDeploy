import groovy.io.FileType
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/summaryString]

def absDir='$[/myJob/CWD]'
def overwrite = '$[overwrite]'
File dir=new File(absDir, "reportObjectTypes")

if (dir.exists()) {
  def counters=loadObjects('reportObjectType', absDir, "/", [:], overwrite, true)
  def nb=counters['reportObjectType']
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName:"summary", value:"No reportObjectTypes")
}
