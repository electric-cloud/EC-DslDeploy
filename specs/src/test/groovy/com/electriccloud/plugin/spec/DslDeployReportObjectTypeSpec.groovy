package com.electriccloud.plugin.spec

import spock.lang.Shared

class DslDeployReportObjectTypeSpec
    extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
  }

  def "deploy reportObjectTypes"() {

    def projectName = 'rot_project'
    def projects = [project : projectName]

    given: "the dashboard_project code"
    when: "Load DSL Code"
    def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/reportObjectTypes/deployReportObjectTypes/",
            pool: "$defaultPool"
          ]
        )""")
    then: "job completes with success"
    assert p.jobId
    assert getJobProperty("outcome", p.jobId) == "success"

    then: "verify first reportObjectType was created"
    def rot1 = dsl """getReportObjectType(reportObjectTypeName: 'rot')"""
    assert rot1?.reportObjectType

    then: "verify first reportObjectType was created"
    def rot2= dsl """getReportObjectType(reportObjectTypeName: 'rot2')"""
    assert rot2?.reportObjectType

    then: "verify report associations were created"
    def associations = dsl """getReportObjectAssociations (reportObjectTypeName: 'rot')"""
    assert associations?.reportObjectAssociation?.size == 1

    then: "verify report attributes were created"
    def attributes = dsl """getReportObjectAttributes (reportObjectTypeName: 'rot2')"""
    attributes?.reportObjectAttribute?.size == 1

    cleanup:
    deleteProjects(projects, false)
    dsl """deleteReportObjectType(reportObjectTypeName: 'rot')"""
    dsl """deleteReportObjectType(reportObjectTypeName: 'rot2')"""
   }
}
