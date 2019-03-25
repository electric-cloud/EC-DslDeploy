package com.electriccloud.plugin.spec
import spock.lang.*

class victor extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String projName="SAMPLE_VAL"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteProject(projectName: "$projName")
    """
  }

  def doCleanupSpec() {
//    conditionallyDeleteProject(projName)
  }



  // Check sample
  def "victor test suite upload"() {
    given: "the victor code"
    when: "Load DSL Code"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/victor",
            pool: 'default'
          ]
        )""")
    then: "job succeeds"
      assert result.jobId
      assert getJobProperty("outcome", result.jobId) == "success"

      // check project property exists
    then: "project property is found"
      getP("/projects/$projName/projectProperty") == "123"

    // check service is found
    then: "service is found"
      def service=dsl """
        getService(
          projectName: "$projName",
          serviceName: "testService"
        )
      """
      assert service.service.serviceName == "testService"

    // check application is found
    then: "application is found"
      def app=dsl """
        getApplication(
          projectName: "$projName",
          applicationName: "testApp"
        )
      """
      assert app.application.applicationName == "testApp"

  }
}
