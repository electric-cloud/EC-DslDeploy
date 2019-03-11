package com.electriccloud.plugin.spec
import spock.lang.*
import org.apache.tools.ant.BuildLogger

class NMB27865 extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  static NMB="NMB27865"

  def doSetupSpec() {
    String currentDir = new File(".").getAbsolutePath()
    new AntBuilder().copy( todir:"/tmp/$NMB" ) {
      fileset( dir:"dslCode/$NMB" )
    }
  }

  def doCleanupSpec() {
    conditionallyDeleteProject("$NMB")
  }

  // Check promotion
  def "plugin promotion"() {
    given:

    when: 'the plugin is promoted'
      def result = dsl """promotePlugin(pluginName: "$pName")"""
      def version = getP("/plugins/$pName/pluginVersion")
      def prop = getP("/plugins/$pName/project/ec_visibility")
    then:
      assert result.plugin.pluginVersion == version
      assert prop == 'pickListOnly'
  }

  // check "=" format works
  def "NMB-27865"() {
    given:

    when: "Load DSL code"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "/tmp/$NMB",
            pool: 'default'
          ]
        )""")
    then:
      assert result.jobId
      def outcome=getJobProperty("outcome", result.jobId)
      assert outcome == "success"

      // Prioperties are created properly
      println "Checking properties"
      assert getP("/projects/$NMB/Changes/C2834144/SM_Change_Approved") == 'false'
      assert getP("/projects/$NMB/Changes/C2835095/EJ_ServiceManager_EventinLastSeq") == '1'
      assert getP("/projects/$NMB/Framework/frmSvcImageTag") == '1.0.80'

      // catalogItem is created
      println "Checking catalog item"
      def item=dsl """getCatalogItem(
          projectName: "$NMB",
          catalogName: 'createKubernetesMicroservice',
          catalogItemName: 'createKubernetesMicrosesrvice'
        )"""
      assert item.catalogItem.catalogItemName == 'createKubernetesMicrosesrvice'
  }


}