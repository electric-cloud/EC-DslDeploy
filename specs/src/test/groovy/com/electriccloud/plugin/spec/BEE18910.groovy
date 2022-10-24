package com.electriccloud.plugin.spec

import spock.lang.*

class NMB27865
      extends PluginTestHelper
{

  static String pName='EC-DslDeploy'
  static String jira="BEE18910"

  @Shared String pVersion
  @Shared String plugDir

  def doSetupSpec()
  {
    dsl """ deleteProject(projectName: "$jira") """
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
  }

  def doCleanupSpec()
  {
    conditionallyDeleteProject("$jira")
  }

  // check that details for property and property sheet are imported
  def "property details import"()
  {
    given:

    when: "Load DSL code"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/$jira",
            pool: "$defaultPool"
          ]
        )""")
    then:
      assert result.jobId
      def outcome=getJobProperty("outcome", result.jobId)
      assert outcome == "success"

      // Properties are created properly
      println "Checking properties"
      def prop1 = dsl "getProperty propertyName: '/projects/$jira/prop1'"
      assert prop1
      assert prop1.property.value == "prop1"
      assert prop1.property.description == "prop1"

      def propSheet1 = dsl "getProperty propertyName: '/projects/$jira/propSheet1'"
      assert propSheet1
      assert propSheet1.property.description == "propSheet1"
  }
}
