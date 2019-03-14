package com.electriccloud.plugin.spec
import spock.lang.*
import org.apache.tools.ant.BuildLogger

class NMB27865 extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  static String jira="NMB27865"
  static String dir="/tmp/dslDeploy/syntax/$jira"

  def doSetupSpec() {
    new AntBuilder().copy( todir:"$dir" ) {
      fileset( dir:"dslCode/$jira" )
    }
  }

  def doCleanupSpec() {
    conditionallyDeleteProject("$jira")
  }

  // check "=" format works
  def "NMB-27865 alternate syntax"() {
    given:

    when: "Load DSL code"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dir",
            pool: 'default'
          ]
        )""")
    then:
      assert result.jobId
      def outcome=getJobProperty("outcome", result.jobId)
      assert outcome == "success"

      // Prioperties are created properly
      println "Checking properties"
      assert getP("/projects/$jira/Changes/C2834144/SM_Change_Approved") == 'false'
      assert getP("/projects/$jira/Changes/C2835095/EJ_ServiceManager_EventinLastSeq") == '1'
      assert getP("/projects/$jira/Framework/frmSvcImageTag") == '1.0.80'

      // catalogItem is created
      println "Checking catalog item"
      def item=dsl """getCatalogItem(
          projectName: "$jira",
          catalogName: 'createKubernetesMicroservice',
          catalogItemName: 'createKubernetesMicrosesrvice'
        )"""
      assert item.catalogItem.catalogItemName == 'createKubernetesMicrosesrvice'
  }


}
