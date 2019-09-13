package com.electriccloud.plugin.spec

import spock.lang.Shared

class deployPipeline
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

    def "deploy pipeline stages with metadata.json"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/CEV-23073_metadata/projects/CEV-23073_metadata",
            projName: 'pipeline_project'
          ]
        )""")
        then: "job succeeded"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        def stages = dsl "getStages projectName: '$projName', pipelineName: 'pipeline1'"

        assert stages.stage.size() == 3

        def devStage = stages.stage.find{it.stageName == 'dev'}
        assert devStage.index == '0'

        def qeStage = stages.stage.find{it.stageName == 'qe'}
        assert qeStage.index == '1'

        def prodStage = stages.stage.find{it.stageName == 'prod'}
        assert prodStage.index == '2'
    }

    def "deploy pipeline stages without metadata.json"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/CEV-23073_no_metadata/projects/CEV-23073_no_metadata",
            projName: 'pipeline_project2'
          ]
        )""")
        then: "job completed with warnings"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

        def stages = dsl "getStages projectName: 'pipeline_project2', pipelineName: 'pipeline1'"

        assert stages.stage.size() == 3

        def devStage = stages.stage.find{it.stageName == 'dev'}
        assert devStage.index == '0'

        def qeStage = stages.stage.find{it.stageName == 'qe'}
        assert qeStage.index == '2'

        def prodStage = stages.stage.find{it.stageName == 'prod'}
        assert prodStage.index == '1'
    }

}
