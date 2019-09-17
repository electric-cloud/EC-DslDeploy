package com.electriccloud.plugin.spec

import spock.lang.Shared

class ReportSpec
    extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
  }

  def "deploy reports"() {

    def projectName = 'report_test'
    def projects = [project : projectName]


    given: "the report_project code"
    when: "Load DSL Code"
    def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/reports/deployReports/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
    then: "job completes with success"
    assert p.jobId
    assert getJobProperty("outcome", p.jobId) == "success"

    then: "check that reports were created"
      def reports = dsl """getReports(projectName: '$projectName' )"""
      assert reports?.report?.size == 2

    cleanup:
    deleteProjects(projects, false)
   }
}
