package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.Shared

class PipelineSpec
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
    }

    // overwrite with pipeline
    def "deploy multiple project pipelines"(){

        def projectName = 'pipeline_project'
        def projects = [project : projectName]

        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/pipelines/deploy_multiple_pipelines/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
        then: "job completes with warning as no metadata.json for ordered children"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

        then: "check number of created pipelines"
        def pipelines = dsl """getPipelines(projectName: '$projectName')"""
        assert pipelines
        assert pipelines?.pipeline?.size == 2

        cleanup:
        deleteProjects(projects, false)

    }

    def "deploy pipeline stage with gates"(){

        def projectName = 'pipeline_test'
        def projects = [project : projectName]

        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/pipelines/deploy_pipeline_gates/",
            pool: "$defaultPool"
          ]
        )""")
        then: "job completes with warning as no metadata.json for ordered children"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "check PRE gate was created"
        def preGate = dsl """getGate(projectName: '$projectName', pipelineName: 'pipeline', stageName: 'Stage 1', gateType: 'PRE')"""
        assert preGate

        then: "check PRE gate rule was created"
        def preRule = dsl """getTask(projectName: '$projectName', pipelineName: 'pipeline', stageName: 'Stage 1', gateType: 'PRE', taskName: 'preRule') """
        assert preRule

        then: "check PRE gate was created"
        def postGate = dsl """getGate(projectName: '$projectName', pipelineName: 'pipeline', stageName: 'Stage 1', gateType: 'POST')"""
        assert postGate

        then: "check POST gate rule was created"
        def postTask = dsl """getTask(projectName: '$projectName', pipelineName: 'pipeline', stageName: 'Stage 1', gateType: 'POST', taskName: 'postTask') """
        assert postTask

        cleanup:
        deleteProjects(projects, false)
    }

}
