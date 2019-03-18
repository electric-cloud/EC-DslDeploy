package com.electriccloud.plugin.spec
import spock.lang.*

class dslDeploy extends PluginTestHelper {
  static String pName='EC-DslDeploy'

  def doSetupSpec() {
  }

  def doCleanupSpec() {
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
        println "Checking $proc"
        assert res[proc].procedure.procedureName == proc
      }
  }


}
