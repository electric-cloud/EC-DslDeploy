package com.electriccloud.plugin.spec

import spock.lang.Shared

class ServiceAccountSpec
    extends PluginTestHelper {

  static String pName='EC-DslDeploy'

  @Shared String pVersion
  @Shared String plugDir

  static String serviceAccountName = 'testServiceAccount'

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")

  }

  def doCleanupSpec() {
    dsl """deleteServiceAccount (serviceAccountName: '$serviceAccountName')"""
  }

  // Service account tests
  def "import service account"() {
    given: "service account"
      dsl """createServiceAccount (serviceAccountName: '$serviceAccountName')"""

    when: "Load DSL Code"
      def response= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/serviceAccounts/testServiceAccount",
            pool: "$defaultPool"
          ]
        )""")
    then: "job succeeds"
      assert response.jobId
      assert getJobProperty("outcome", response.jobId) == "success"

    then: "check that service account is created"
      def result = dsl """getServiceAccount (serviceAccountName: '$serviceAccountName')"""
      assert result?.serviceAccount?.size == 1
   }
}
