package com.electriccloud.plugin.spec
import spock.lang.*

class victor extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String projName="SAMPLE_VAL"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteProject(projectName: "$projName")
      deleteProject(projectName: "POST_VICTOR")
      deleteResource(resourceName: "res457")
      deletePersona(personaName: "victorP")
    """
  }

  def doCleanupSpec() {
    conditionallyDeleteProject(projName)
    conditionallyDeleteProject("POST_VICTOR")
    dsl """
      deleteResource(resourceName: "res457")
      deletePersona(personaName: "victorP")
   """
  }

  // Check sample
  def "victor test suite upload"() {
    given: "the victor code"
    when: "Load DSL Code"
      def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/victor",
            pool: 'local'
          ]
        )""")
    then: "job succeeds"
      assert p.jobId
      assert getJobProperty("outcome", p.jobId) == "success"

    // check project property exists
    when: "checking project properties"
      def prop1=getP("/projects/$projName/projectProperty")
      def prop2=getP("/projects/$projName/prop1")
    then: "project properties are found"
      assert prop1  == "123"    // from project.groovy
      assert  prop2 =~ /Hello world\s+/    // from properties/

    // check application is found
    then: "application is found"
      def app=dsl """
        getApplication(
          projectName: "$projName",
          applicationName: "testApp"
        )"""
      assert app.application.applicationName == "testApp"

    // Check for dashboard
    then: "dashboard is found"
      def dash= dsl """
        getDashboard(
          projectName: "$projName",
          dashboardName: 'testDashboard',
        )"""
      assert dash.dashboard.dashboardName == "testDashboard"
      assert dash.dashboard.description   == "val"
    // Check for reportingFilter
    then: "reportingFilter is found"
      def rf= dsl """
        getReportingFilter(
          projectName: "$projName",
          dashboardName: 'testDashboard',
          reportingFilterName: 'DateFilter'
        )"""
      assert rf.reportingFilter.reportingFilterName == "DateFilter"
      assert rf.reportingFilter.type                == "DATE"
    // Check for widget
    then: "widget is found"
      def w= dsl """
        getWidget(
          projectName: "$projName",
          dashboardName: 'testDashboard',
          widgetName: 'testWidget'
        )"""
      assert w.widget.widgetName == "testWidget"

    // check environment is found
    then: "environment is found"
      def env=dsl """
        getEnvironment(
          projectName: "$projName",
          environmentName: "testEnv"
        )"""
      assert env.environment.environmentName == "testEnv"
      assert env.environment.description     == "val"
    // check environmentTier is found
    then: "environmentTier is found"
      def envT=dsl """
        getEnvironmentTier(
          projectName: "$projName",
          environmentName: "testEnv",
          environmentTierName: "Tier 1",
        )"""
      assert envT.environmentTier.environmentTierName == "Tier 1"
    // Check for cluster
    then: "cluster is found"
      def cl = dsl """
        getCluster(
          projectName: "$projName",
          environmentName: "testEnv",
          clusterName: 'testCluster',
        )"""
      assert cl.cluster.clusterName == "testCluster"
      assert cl.cluster.description == "val"

    // check procedure is found
    then: "procedure is found"
      def proc=dsl """
        getProcedure(
          projectName: "$projName",
          procedureName: "testProcedure"
        )"""
      assert proc.procedure.procedureName == "testProcedure"
    // check step is found
    then: "step is found"
      def st=dsl """
        getStep(
          projectName: "$projName",
          procedureName: "testProcedure",
          stepName: 'echo'
        )"""
      assert st.step.shell == "ec-perl"

    // check report is found
    then: "report is found"
      def rep=dsl """
        getReport(
          projectName: "$projName",
          reportName: "testReport"
        )"""
      assert rep.report.reportName == "testReport"
      assert rep.report.reportObjectTypeName == 'job'

    // check resource is found
    then: "resource is found"
      def rsc=dsl """
        getResource(
          resourceName: "res457"
        )"""
      assert rsc.resource.resourceName == "res457"
      assert rsc.resource.hostName == 'doesnotexist'
      assert getP("/resources/res457/prop1") =~ /val23456\s+/
      assert getP("/resources/res457/level1/level2/level3/prop123") =~ /res3-level3\s+/

    // check service is found
    then: "service is found"
      def serv=dsl """
        getService(
          projectName: "$projName",
          serviceName: "testService"
        )"""
      assert serv.service.serviceName == "testService"

    // Issue #10 - POST
    then: "POST project is found"
      def pp=dsl """ getProject(projectName: "POST_VICTOR") """
      assert pp.project.projectName == "POST_VICTOR"

    // Issue #2 - persona
    // then: "persona is found"
    //   def pa=dsl """getPersona(personaName: 'victorP')"""
    //   assert pa.persona.personaName == 'victorP'

     // Check catalog exist
     then: "catalog exist"
       println "Checking catalog"
       assert getP("/projects/$projName/catalogs/testCatalog1/description") == "val"

     // catalogItem is created
     then: "catalogItem exist"
       println "Checking catalog item"
       def item=dsl """getCatalogItem(
           projectName: "$projName",
           catalogName: 'testCatalog1',
           catalogItemName: 'testCatalogItem'
         )"""
       assert item.catalogItem.description == 'val'

   }

}
