package com.electriccloud.plugin.spec
import spock.lang.*
import org.apache.tools.ant.BuildLogger

class catalogItem extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  static String jira="ECDSLDEPLOY-2"
  static String dir="/tmp/dslDeploy/catalogItem/$jira"

  def doSetupSpec() {
    dsl """ deleteProject(projectName: "$jira") """
    new AntBuilder().copy( todir:"$dir" ) {
      fileset( dir:"dslCode/$jira" )
    }
  }

  def doCleanupSpec() {
    conditionallyDeleteProject("$jira")
  }

  // check "=" format works
  def "ECDSLDPLOY-2"() {
    given: "code with catalogItem"

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

      // Check catalog exist
      println "Checking catalog"
      assert getP("/projects/$jira/catalogs/BundledRelease/description") == "for EC-DslDeploy testing"

      // catalogItem is created
      println "Checking catalog item"
      def item=dsl """getCatalogItem(
          projectName: "$jira",
          catalogName: 'BundledRelease',
          catalogItemName: 'MonthlyBundledRelease'
        )"""
      assert item.catalogItem.catalogItemName == 'MonthlyBundledRelease'
  }


}
