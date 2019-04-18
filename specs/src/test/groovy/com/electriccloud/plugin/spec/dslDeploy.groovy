package com.electriccloud.plugin.spec
import spock.lang.*

class dslDeploy extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir

  def doSetupSpec() {
    dsl """
      deleteProperty(propertyName: "/server/EC-DslDeploy/date")
      deleteProject(projectName: "PIPE1")
      deleteProject(projectName: "FOO")
      deleteProject(projectName: "BAR_2")
    """
  }

  def doCleanupSpec() {
    dsl """
      deleteProperty(propertyName: "/server/EC-DslDeploy/date")
      deleteProject(projectName: "PIPE1")
      deleteProject(projectName: "FOO")
      deleteProject(projectName: "BAR_2")
    """
  }

  // Check promotion
  def "plugin promotion"() {
    given: "a plugin"
    when: 'the plugin is promoted'
      def result = dsl """promotePlugin(pluginName: "$pName")"""
      pVersion = getP("/plugins/$pName/pluginVersion")
      plugDir = getP("/server/settings/pluginsDirectory")
      def prop = getP("/plugins/$pName/project/ec_visibility")
      def timeout = getP("/server/$pName/timeout")
    then:
      assert result.plugin.pluginVersion == pVersion
      assert prop == 'pickListOnly'
      assert timeout?.isInteger()
  }

  // Check procedures exist
  def "checkProcedures for EC-DslDeploy"() {
    given: "a list of procedure"
      def list= ["installDsl", "installDslFromDirectory", "installProject"]
      def res=[:]
    when: "check for existence"
      list.each { proc ->
         res[proc]= dsl """
          getProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "$proc"
          ) """
      }
    then: "they exist"
      list.each  {proc ->
        assert res[proc].procedure.procedureName == proc
      }
   }

  // Check that installProject is not visible in the step picker
  def "installProject should not be in the stepPicker"() {
    when: "getting procedure standardStepPicker"
      def prop = getP("/plugins/$pName/project/procedures/installProject/standardStepPicker")
    then: "installProject should be false"
      assert prop == "false"

    when: "getting server stepPicker property"
      def ps = dsl """
        getProperty(
          propertyName: "/server/ec_customerEditors/pickerStep/$pName - installProject"
        )
      """
    then: "installProject should not exist"
      assert !ps.property

  }

  // Check sample
  def "sample upload"() {
    given: "the sample code"
    when: "Load DSL Code"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/sample",
            pool: 'local'
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
