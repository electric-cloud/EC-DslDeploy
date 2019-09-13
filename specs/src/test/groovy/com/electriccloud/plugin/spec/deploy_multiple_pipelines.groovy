package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.Shared

class deploy_multiple_pipelines
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "pipeline_project"

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

    // overwrite with pipeline
    def "deploy multiple project pipelines"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/deploy_multiple_pipelines/projects/pipeline_project",
            projName: 'pipeline_project'
          ]
        )""")
        then: "job completes with warning as no metadata.json for ordered children"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

        def pipelines = dslWithXmlResponse("getPipelines projectName: '$projName'")

        System.out.print(pipelines)

    }

}
