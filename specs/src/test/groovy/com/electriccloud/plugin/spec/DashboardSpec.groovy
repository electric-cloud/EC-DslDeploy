package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.Shared

class DashboardSpec
    extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
  }

  def "deploy dashboards"() {

    def projectName = 'bashboard_project'
    def projects = [project : projectName]

    given: "the dashboard_project code"
    when: "Load DSL Code"
    def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/dashboards/deployDashboards/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
    then: "job completes with success"
    assert p.jobId
    assert getJobProperty("outcome", p.jobId) == "success"

    then: "verify dashboard was created"
    def dashboard = dsl """getDashboard(projectName: '$projectName', dashboardName: 'qe dashboard name1')"""
    assert dashboard

    then: "verify reporting filters were created"
    def repFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'qe dashboard name1')"""
    assert repFilters
    assert repFilters?.reportingFilter?.size == 1

    then: "verify widgets were created"
    def widgets = dsl """getWidgets(projectName: '$projectName', dashboardName: 'qe dashboard name1')"""
    assert widgets
    assert widgets?.widget?.size == 2

    then: "verify widget reproting filters were created"
    def widgetRepoFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'qe dashboard name1', widgetName: 'qe_widget_name1')"""
    assert widgetRepoFilters
    assert widgetRepoFilters?.reportingFilter?.size == 1

    then: "verify widget filter overrides were created"
    def widgFilterOverride = dsl"""getWidgetFilterOverrides(projectName: '$projectName', dashboardName: 'qe dashboard name1', widgetName: 'qe_widget_name1')"""
    assert widgFilterOverride
    assert widgFilterOverride?.widgetFilterOverride?.size == 1

    cleanup:
    deleteProjects(projects, false)
   }

  def "deploy dashboards - dsl files"() {

    def projectName = 'bashboard_project'
    def projects = [project : projectName]

    given: "the dashboard_project code"
    when: "Load DSL Code"
    def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/dashboards/deployDashboardsDsl/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
    then: "job completes with success"
    assert p.jobId
    assert getJobProperty("outcome", p.jobId) == "success"

    then: "verify dashboard was created"
    def dashboard = dsl """getDashboard(projectName: '$projectName', dashboardName: 'qe dashboard name1')"""
    assert dashboard

    then: "verify reporting filters were created"
    def repFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'qe dashboard name1')"""
    assert repFilters
    assert repFilters?.reportingFilter?.size == 1

    then: "verify widgets were created"
    def widgets = dsl """getWidgets(projectName: '$projectName', dashboardName: 'qe dashboard name1')"""
    assert widgets
    assert widgets?.widget?.size == 2

    then: "verify widget reproting filters were created"
    def widgetRepoFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'qe dashboard name1', widgetName: 'qe_widget_name1')"""
    assert widgetRepoFilters
    assert widgetRepoFilters?.reportingFilter?.size == 1

    then: "verify widget filter overrides were created"
    def widgFilterOverride = dsl"""getWidgetFilterOverrides(projectName: '$projectName', dashboardName: 'qe dashboard name1', widgetName: 'qe_widget_name1')"""
    assert widgFilterOverride
    assert widgFilterOverride?.widgetFilterOverride?.size == 1

    cleanup:
    deleteProjects(projects, false)
  }
}
