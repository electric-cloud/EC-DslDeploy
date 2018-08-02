import spock.lang.*
import com.electriccloud.spec.PluginSpockTestSupport

class SampleSpec extends PluginSpockTestSupport {

    def doSetupSpec() {
        dsl """
            project "MyProject", {
                procedure "MyProcedure", {
                    step "My Step", {
                        command = "echo 'hello'"
                    }
                }
            }
        """
    }

    def doCleanupSpec() {
        dsl """
            deleteProject("MyProject")
        """
    }

    def "run a procedure"() {
        when: 'the procedure runs'
            def result = dsl """
                runProcedure(
                    projectName: "MyProject",
                    procedureName: "MyProcedure"
                )
            """
        then: 'the procedure finishes'
            assert result?.jobId
            waitUntil {
                jobCompleted result.jobId
            }
    }

    def "always failing"() {
        when: 'something happens'
            println "TODO"
        then: 'assertion fails'
            assert false
    }
}
