package com.electriccloud.plugin.spec

import spock.lang.Shared

class EnvironmentSpec
    extends PluginTestHelper
{

  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String projName="testProject"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteProject(projectName: "$projName")
    """
  }

  def doCleanupSpec() {
    conditionallyDeleteProject(projName)
  }

  def "BEE-35400: deploy application with tierMaps and environmentTemplateTierMaps"() {
    given: "application dsl code"
    dsl """
project '$projName', {
  environment 'testEnvironment',{
      reservation 'testReservation', {
      beginDate = '2019-09-09T10:00'
      blackout = '1'
      endDate = '2019-09-09T11:00'
      timeZone = 'Europe/Kiev'
    }
  }
}
"""

      when: "Generate DSL files"
      def result1 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "generateDslToDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            includeAllChildren: '1',
            suppressNulls: '1',
            objectType: 'project',
            objectName: '$projName',
            httpIdleTimeout: '180'
          ]
        )""")
      then:
      assert result1.jobId
      def outcome1 = getJobProperty("outcome", result1.jobId)
      assert outcome1 == "success"
      and: "remove whole project"
      deleteProjects([projectName: projName], false)

      when: "Import DSL files"
      def result2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
      then:
      assert result2.jobId
      def outcome2 = getJobProperty("outcome", result2.jobId)
      assert outcome2 == "success"

    then: "check that environment was created"
      def envs = dsl """getEnvironments(projectName: '$projName' )"""
      assert envs?.environment?.size == 1

    then: "check that environment reservation was created"
      def reservs = dsl """getReservations(projectName: '$projName', environmentName: 'testEnvironment')"""
      assert reservs?.reservation?.size == 3
   }
}
