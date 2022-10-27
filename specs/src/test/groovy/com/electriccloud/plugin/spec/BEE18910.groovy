package com.electriccloud.plugin.spec

import spock.lang.*

class BEE18910
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

  def "round trip for complex project with properties"() {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load complex project with properies from single DSL file"
    dslFile("complex_project.dsl")

    when: "Generate DSL files"
    def result1 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "generateDslToDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            includeAllChildren: '1',
            suppressNulls: '1',
            objectType: 'project',
            objectName: 'proj_name'
          ]
        )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Import DSL files"
    def result2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'proj_name', c1: 'comp_name1', c2: 'comp_name2', p1: 'proc_name1', p2: 'proc_name2'])
    new File(dslDir).deleteDir()
  }
}
