package com.electriccloud.plugin.spec
import spock.lang.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import org.apache.tools.ant.BuildLogger

class NMB27865 extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  static NMB="NMB27865"

  def doSetupSpec() {
    String currentDir = new File(".").getAbsolutePath()
    println ("Current: $currentDir")
    //   Files.copy(Paths.get("dslCode/$NMB"), Paths.get("/tmp/$NMB"), StandardCopyOption.REPLACE_EXISTING)
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
      def version = dsl """getProperty("/plugins/$pName/pluginVersion")"""
      def prop = dsl """getProperty("/plugins/$pName/project/ec_visibility")"""
    then:
      assert result.plugin.pluginVersion == version.property.value
      assert prop.property.value == 'pickListOnly'
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
      def outcome=getJobProperty("/myJob/outcome", result.jobId)
      assert outcome == "success"

      assert getProjectProperty("/projects/pName/Changes/C2834144/SM_Change_Approved") == 'false'
      assert getProjectProperty("/projects/pName/Changes/C2835095/EJ_ServiceManager_EventinLastSeq") == '1'
      assert getProjectProperty("/projects/pName/Framework/C2835095/frmSvcImageTag") == '1.0.80'

  }


}
