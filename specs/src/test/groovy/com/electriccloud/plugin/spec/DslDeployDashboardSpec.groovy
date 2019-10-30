package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.Shared

class DslDeployDashboardSpec
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

  def "deploy dashboards with overwrite=true"() {

    def projectName = 'proj1'
    def projects = [project : projectName]

    given: "the dashboard_project code"
    when: "Load DSL Code"
    def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/dashboards/deployDashboards_overwrite/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
    then: "job completes with success"
    assert p.jobId
    assert getJobProperty("outcome", p.jobId) == "success"

    then: "verify dashboard was created"
    def dashboard = dsl """getDashboard(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert dashboard
    assert dashboard.dashboard.description == 'Application Deployments Dashboard'

    then: "verify reporting filters were created"
    def repFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert repFilters
    assert repFilters?.reportingFilter?.size == 2
    //
    def repFilter1 = dsl "getReportingFilter(projectName: '$projectName', dashboardName: 'Application Deployments', reportingFilterName: 'ApplicationFilter')"
    assert repFilter1
    assert repFilter1.reportingFilter.description == 'Filter deployments by application id.'
    //
    def repFilter2 = dsl "getReportingFilter(projectName: '$projectName', dashboardName: 'Application Deployments', reportingFilterName: 'DateFilter')"
    assert repFilter2
    assert !repFilter2.reportingFilter.description

    then: "verify widgets were created"
    def widgets = dsl """getWidgets(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert widgets
    assert widgets?.widget?.size == 2

    def widget1 = dsl "getWidget(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'AverageDeploymentDuration')"
    assert widget1
    assert !widget1.widget.description

    def widget2 = dsl "getWidget(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"
    assert widget2
    assert widget2.widget.description == 'Breakdown of deployments by outcome over time'

    then: "verify widget reproting filters were created"
    def widgetRepoFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"""
    assert widgetRepoFilters
    assert widgetRepoFilters?.reportingFilter?.size == 1
    def widgetRepoFilter = widgetRepoFilters?.reportingFilter[0]
    assert widgetRepoFilter.description == 'widget repo filter'

    then: "verify widget filter overrides were created"
    def widgFilterOverride = dsl"""getWidgetFilterOverrides(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"""
    assert widgFilterOverride
    assert widgFilterOverride?.widgetFilterOverride?.size == 1
    def widgFilterOverrideId = widgFilterOverride?.widgetFilterOverride?.widgetFilterOverrideId

    //
    when: 'modify data'
    dsl """
    project '$projectName', {
      
      dashboard 'Application Deployments', {
            description = 'new'
            
            reportingFilter 'ApplicationFilter', {
              description = 'new'
            }
            
            reportingFilter 'DateFilter', {
              description = 'new'
            }
            
            reportingFilter 'new', {
              type = 'APPLICATION'
              operator = 'IN'
            }
            
            widget 'AverageDeploymentDuration', {
               description = 'new'
               column
            }
            
            widget 'DeploymentsFrequency', {
               description = 'new'
               
               reportingFilter 'repoFilter', {
                 description='new'
                 type = 'APPLICATION'
                 operator = 'IN'
               } 

                
               reportingFilter 'new2', {
                 type = 'APPLICATION'
                 operator = 'IN'
               }
               
               widgetFilterOverride 'DateFilter', {
                  ignoreFilter = '1'
               }

                   
            }
            
            widget 'new'

      }


  }
"""

    then: 'check child counts'
    def newRepFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert newRepFilters
    assert newRepFilters?.reportingFilter?.size == 3
    //
    def newWidgets = dsl """getWidgets(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert newWidgets
    assert newWidgets?.widget?.size == 3
    //
    def newWidgetRepoFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"""
    assert newWidgetRepoFilters
    assert newWidgetRepoFilters?.reportingFilter?.size == 2
    //
    def newWidgFilterOverride = dsl"""getWidgetFilterOverrides(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"""
    assert newWidgFilterOverride
    assert newWidgFilterOverride?.widgetFilterOverride?.size == 2
    //
    when: "Load DSL Code in overwrite"
    def res = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/dashboards/deployDashboards_overwrite/projects/$projectName",
            projName: '$projectName',
            overwrite: '1'
          ]
        )""")
    then: "job completes with success"
    assert res.jobId
    assert getJobProperty("outcome", res.jobId) == "success"

    then: 'check results'
    def cleanedDashboard = dsl """getDashboard(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert cleanedDashboard
    assert cleanedDashboard.dashboard.description == 'Application Deployments Dashboard'

    then: "verify reporting filters were created"
    def cleanedRepFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert cleanedRepFilters
    assert cleanedRepFilters?.reportingFilter?.size == 2
    //
    def cleanedRepFilter1 = dsl "getReportingFilter(projectName: '$projectName', dashboardName: 'Application Deployments', reportingFilterName: 'ApplicationFilter')"
    assert cleanedRepFilter1
    assert cleanedRepFilter1.reportingFilter.description == 'Filter deployments by application id.'
    //
    def cleanedRepFilter2 = dsl "getReportingFilter(projectName: '$projectName', dashboardName: 'Application Deployments', reportingFilterName: 'DateFilter')"
    assert cleanedRepFilter2
    assert cleanedRepFilter2.reportingFilter.description == ''

    then: "verify widgets were created"
    def cleanedWidgets = dsl """getWidgets(projectName: '$projectName', dashboardName: 'Application Deployments')"""
    assert cleanedWidgets
    assert cleanedWidgets?.widget?.size == 2

    def cleanedWidget1 = dsl "getWidget(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'AverageDeploymentDuration')"
    assert cleanedWidget1
    assert cleanedWidget1.widget.description == ''

    def cleanedWidget2 = dsl "getWidget(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"
    assert cleanedWidget2
    assert cleanedWidget2.widget.description == 'Breakdown of deployments by outcome over time'

    then: "verify widget reproting filters were created"
    def cleanedWidgetRepoFilters = dsl """getReportingFilters(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"""
    assert cleanedWidgetRepoFilters
    assert cleanedWidgetRepoFilters?.reportingFilter?.size == 1
    def cleanedWidgetRepoFilter = cleanedWidgetRepoFilters?.reportingFilter[0]
    assert cleanedWidgetRepoFilter.description == 'widget repo filter'

    then: "verify widget filter overrides were created"
    def cleanedWidgFilterOverride = dsl"""getWidgetFilterOverrides(projectName: '$projectName', dashboardName: 'Application Deployments', widgetName: 'DeploymentsFrequency')"""
    assert cleanedWidgFilterOverride
    assert cleanedWidgFilterOverride?.widgetFilterOverride?.size == 1
    assert cleanedWidgFilterOverride?.widgetFilterOverride?.widgetFilterOverrideId == widgFilterOverrideId

    cleanup:
    deleteProjects(projects, false)
  }
}
