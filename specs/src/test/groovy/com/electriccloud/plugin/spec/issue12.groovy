package com.electriccloud.plugin.spec
import spock.lang.*

class issue12 extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String projName="issue12"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteProject(projectName: "$projName")
      deletePersona(personaName: "serviceDeveloper")
      deleteResource(resourceName: "res12")
    """
  }

  def doCleanupSpec() {
    conditionallyDeleteProject(projName)
    dsl """
      deletePersona(personaName: "serviceDeveloper")
      deleteResource(resourceName: "res12")
   """
  }

  // Check sample
  def "issue12 test suite upload"() {
    given: "the isssue12 code"
    when: "Load DSL Code"
      def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/issue12",
            pool: 'default'
          ]
        )""")
    then: "job succeeds"
      assert p.jobId
      assert getJobProperty("outcome", p.jobId) == "success"

     // Issue #2 - persona
    then: "persona is found"
      def pa=dsl """getPersona(personaName: 'serviceDeveloper')"""
      assert pa.persona.homePageName == 'Microservice Deployments'

    // check resource is found
    then: "resource is found"
      def rsc=dsl """
        getResource(
          resourceName: "res12"
        )"""
      assert rsc.resource.resourceName == "res12"
      assert rsc.resource.hostName == 'doesnotexist12'
      assert getP("/resources/res12/prop1") =~ /val12\s+/

   }
}
