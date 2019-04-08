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
      deleteResource(resourceName: "res12")
    """
  }

  def doCleanupSpec() {
    conditionallyDeleteProject(projName)
    dsl """
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

     // check master component
     then: "master component exist"
      println "Checking master component"
      def mc=dsl """ getComponent(
        projectName: "$projName",
        applicationName: null,
        componentName: "master12"
        )"""
      assert mc.component.componentName == "master12"

    // check environment is found
    then: "environment is found"
      def env=dsl """
        getEnvironment(
          projectName: "$projName",
          environmentName: "testEnv12"
        )"""
      assert env.environment.environmentName == "testEnv12"
      assert env.environment.description     == "val12"
    // check environmentTier is found
    then: "environmentTier is found"
      def envT=dsl """
        getEnvironmentTier(
          projectName: "$projName",
          environmentName: "testEnv12",
          environmentTierName: "Tier12",
        )"""
      assert envT.environmentTier.environmentTierName == "Tier12"
    // Check for cluster


    // check release is found
    then: "release is found"
      def rel=dsl """
        getRelease(
          projectName: "$projName",
          releaseName: "testRelease12"
        )"""
      assert rel.release.releaseName == "testRelease12"
      assert rel.release.plannedEndTime =~ /2019-04-04T/

    // check pipeline is found
    then: "pipeline is found"
      def pipe=dsl """
        getPipeline(
          projectName: "$projName",
          pipelineName: "p12"
        )"""
      assert pipe.pipeline.pipelineName == "p12"
    // check stage is #3
    then: "stage is in right position"
      def st=dsl """
        getStage(
           projectName: "$projName",
           pipelineName: "p12",
           stageName: 'UAT',
        )"""
      assert st.stage.index == "3"
   // Check task exists
    then: "task is found"
      def task=dsl """
        getTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: 'DEV',
          taskName: 'Start'
        )"""
      assert task.task.taskName == "Start"
      assert task.task.taskType == "MANUAL"

   }
}
