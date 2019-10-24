package com.electriccloud.plugin.spec

import spock.lang.Shared

class ApplicationSpec
    extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String projName="applicationDeployment"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteProject(projectName: "$projName")
      deleteResource(resourceName: "test1")
      deleteResource(resourceName: "test2")
      deleteResource(resourceName: "test3")
      deleteResource(resourceName: "test4")
      deleteResource(resourceName: "test5")
      deleteResource(resourceName: "test6")
    """
  }

  def doCleanupSpec() {
    conditionallyDeleteProject(projName)
    dsl """
      deleteResource(resourceName: "test1")
      deleteResource(resourceName: "test2")
      deleteResource(resourceName: "test3")
      deleteResource(resourceName: "test4")
      deleteResource(resourceName: "test5")
      deleteResource(resourceName: "test6")
   """
  }

  // CEV-23521
  def "deploy application with tierMaps and environmentTemplateTierMaps"() {
    given: "application dsl code"
    when: "Load DSL Code"
      def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/applications/tierMaps_envTemplTierMaps",
            pool: "$defaultPool"
          ]
        )""")
    then: "job succeeds"
      assert p.jobId
      assert getJobProperty("outcome", p.jobId) == "success"

    then: "check that environmentTempaltes were created"
      def envTemplates = dsl """getEnvironmentTemplates(projectName: '$projName' )"""
      assert envTemplates?.environmentTemplate?.size == 3

    then: "check that application env template tier maps were created"
      def mappings = dsl """getTierMaps(projectName: '$projName', applicationName: 'app1')"""
      assert mappings?.tierMap?.size == 3

    then: "check that application env template tier maps were created"
      def envMappings = dsl """getEnvironmentTemplateTierMaps(projectName: '$projName', applicationName: 'app1')"""
      assert envMappings?.environmentTemplateTierMap?.size == 3
   }

  //
  def "deploy application with all types of children and overwrite mode"() {
    projName = 'proj_name'
    given: "application dsl code"
    when: "Load DSL Code"
    def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/applications/CEV-23521",
            pool: "$defaultPool"
          ]
        )""")
    then: "job succeeds"
    assert p.jobId
    assert getJobProperty("outcome", p.jobId) == "success"

    then: "check that environmentTempaltes were created"
    def envTemplates = dsl """getEnvironmentTemplates(projectName: '$projName' )"""
    assert envTemplates?.environmentTemplate?.size == 1

    then: "check that application env template tier maps were created"
    def mappings = dsl """getTierMaps(projectName: '$projName', applicationName: 'app1')"""
    assert mappings?.tierMap?.size == 1

    then: "check that application env template tier maps were created"
    def envMappings = dsl """getEnvironmentTemplateTierMaps(projectName: '$projName', applicationName: 'app1')"""
    assert envMappings?.environmentTemplateTierMap?.size == 1

    then: "check service count"
    def services = dsl """getServices(projectName: '$projName', applicationName: 'app1')"""
    assert services?.service?.size == 2

    then: "check process count"
    def processes = dsl """getProcesses(projectName: '$projName', applicationName: 'app1')"""
    assert processes?.process?.size == 2

    then: "check snapshots count"
    def snapshots = dsl """getSnapshots(projectName: '$projName', applicationName: 'app1')"""
    assert snapshots?.snapshot?.size == 2

    when: "Load DSL Code"
    def p2= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/applications/CEV-23521",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
    then: "job succeeds"
    assert p2.jobId
    assert getJobProperty("outcome", p2.jobId) == "success"
  }
}
