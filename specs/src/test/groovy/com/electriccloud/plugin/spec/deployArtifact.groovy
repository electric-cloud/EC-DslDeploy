package com.electriccloud.plugin.spec
import spock.lang.*

class deployArtifact extends PluginTestHelper {
  static String commanderHome = System.getenv('COMMANDER_HOME') ?: '/opt/EC/'
  static String pName='EC-DslDeploy'
  @Shared String plugDir      // Plugin directory
  @Shared String pVersion     // plugin version
  @Shared String codeDir      // where to grab test samnple code

  def doSetupSpec() {
    plugDir = getP("/server/settings/pluginsDirectory")
    pVersion = getP("/plugins/$pName/pluginVersion")
    dsl """
      deleteArtifact(artifactName: "EC-DslDeploy:sample")
      deleteProperty(propertyName: "/server/EC-DslDeploy/date")
      deleteProject(projectName: "PIPE1")
      deleteProject(projectName: "FOO")
      deleteProject(projectName: "BAR_2")
    """
    // SAMPLE_DIR is local on deve machine
    // or plugin Dir
    codeDir=System.getenv('CODE_DIR') ?: System.getenv('COMMANDER_HOME') + "/plugins/$pName-$pVersion"
    logger.debug("CodeDir: $codeDir")
    runCommand("ls -ail $codeDir/lib/dslCode")
  }

  def doCleanupSpec() {
    dsl """
      deleteProperty(propertyName: "/server/EC-DslDeploy/date")
      deleteProject(projectName: "PIPE1")
      deleteProject(projectName: "FOO")
      deleteProject(projectName: "BAR_2")
      deleteArtifact(artifactName: "EC-DslDeploy:sample")
    """
    new AntBuilder().delete(dir:"$commanderHome/repository-data/EC-DslDeploy")
  }

  // Check sample
  def "sample upload from artifact"() {
    given: "a DSL code artifact"
      publishArtifactVersion("EC-DslDeploy:sample", '1.0.0', "$codeDir/lib/dslCode/sample")

    when: "the procedure runs"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDsl",
          actualParameter: [
            artName: "EC-DslDeploy:sample",
            artVersion: "1.0.0",
            pool: 'default',
            allNodes: 'false'
          ]
        )""")
    then: "job succeeds"
      assert result.jobId
      assert getJobProperty("outcome", result.jobId) == "success"

      // check server property exists
    then: "server property is found"
      getP("/server/EC-DslDeploy/date") == "Aug 03, 2018"

      // check SIT stage exist in UC1 pipeline in PIPE1 project
    then: "stage is found"
      def stage=dsl """
        getStage(
          projectName: "PIPE1",
          pipelineName: "UC1",
          stageName: "SIT"
        )
      """
      assert stage.stage.stageName == "SIT"

      // check step echo is of type ec-shell in FOO::BAR
     def step=dsl """
       getStep(
         projectName: "FOO",
         procedureName: "BAR",
         stepName: "echo"
       )
     """
     assert step.step.shell == "ec-perl"

      // check catalogItem BAR_2, Test Catalog
     def catItem=dsl """
       getCatalogItem(
         projectName: "BAR_2",
         catalogName: "Test Catalog",
         catalogItemName: "Service OnBoarding"
       )
     """
     assert catItem.catalogItem.catalogItemName == "Service OnBoarding"
   }
}
