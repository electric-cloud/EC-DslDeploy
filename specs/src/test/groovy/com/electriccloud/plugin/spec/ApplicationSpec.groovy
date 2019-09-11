package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.Shared

@Ignore("ignore until latest server is not deployer")
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

  // Check sample
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
      def mappings = dsl """getTierMaps(projectName: 'dslTestProject', applicationName: 'app1')"""
      assert mappings?.tierMap?.size == 3

    then: "check that application env template tier maps were created"
      def envMappings = dsl """getEnvironmentTemplateTierMaps(projectName: 'dslTestProject', applicationName: 'app1')"""
      assert envMappings?.environmentTemplateTierMap?.size == 3
   }
}
