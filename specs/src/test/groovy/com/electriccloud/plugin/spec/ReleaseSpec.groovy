package com.electriccloud.plugin.spec

import spock.lang.Shared

class ReleaseSpec
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    def projName = "releaseProject"
    def gceProjName = "1_gce"
    def serviceProjName = "2_applicationProject"
    def appProjName = "3_serviceProject"

    def projects = [gceProjName : gceProjName, serviceProjName: serviceProjName, appProjName: appProjName]

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """
      deleteProject(projectName: "$projName")
    """
        dslFile("maven_plugin.dsl")
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
        conditionallyDeleteProject('1_gce')
        conditionallyDeleteProject('2_applicationProject')
        conditionallyDeleteProject('3_serviceProject')
    }

    // overwrite with pipeline
    def "deploy release with children"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/releases/release_with_children/",
            pool: "$defaultPool"
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

        deleteProjects(projects, false)
    }

}
