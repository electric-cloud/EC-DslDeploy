package com.electriccloud.plugin.spec
import spock.lang.*

class issue33 extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String poolName="pool33"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteResourcePool(resourcePoolName: "$poolName")
    """
  }

  def doCleanupSpec() {
    dsl """
      deleteResourcePool(resourcePoolName: "$poolName")
   """
  }

  // Check sample
  def "issue33 test suite upload"() {
    given: "the isssue33 code"
    when: "Load DSL Code"
      def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/issue33",
            pool: 'local'
          ]
        )""")
    then: "job succeeds"
      assert p.jobId
      assert getJobProperty("outcome", p.jobId) == "success"

     // check master component
     then: "resource pool exist"
      println "Checking master component"
      def rp=dsl """ getResourcePool(
        resourcePoolName: "$poolName"
        )"""
      assert rp.resourcePool.resourcePoolName == "$poolName"
  }
}
