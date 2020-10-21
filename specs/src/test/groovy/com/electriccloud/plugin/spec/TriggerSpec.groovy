package com.electriccloud.plugin.spec

import spock.lang.Shared

class TriggerSpec extends PluginTestHelper {

    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dslFile("webhook_plugin.dsl")
    }


    def "import release with trigger"(){

        given:
        dsl "serviceAccount 'trigger_sa'"

        when: "Load DSL Code"
        def proc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/triggers/release/",
            pool: "$defaultPool"
          ]
        )""")

        then: "job completes"
        assert proc.jobId
        assert getJobProperty("outcome", proc.jobId) == "success"

        then: "check trigger was created"
        def response = dsl "getTrigger(projectName: 'triggerReleaseProject', releaseName: 'testRelease', triggerName: 'testTrigger')"
        assert response?.trigger
        assert response.trigger.accessTokenPublicId
        assert response.trigger.pluginKey == 'webhook-plugin'
        assert response.trigger.projectName == 'triggerReleaseProject'
        assert response.trigger.quietTimeMinutes == '0'
        assert response.trigger.releaseName == 'testRelease'
        assert response.trigger.runDuplicates == '0'
        assert response.trigger.serviceAccountName == 'trigger_sa'
        assert response.trigger.triggerType == 'webhook'
        assert response.trigger.triggerEnabled == '1'
        assertParameter(response.trigger.actualParameters, 'param1', 'actParam')
        assertParameter(response.trigger.pipelineParameters, 'param2', 'entryParam')
        assertParameter(response.trigger.pluginParameters, 'pushEvent', 'true')

        cleanup:
        deleteProjects([pr: 'triggerReleaseProject'], false)
    }

    def "import release with overwrite=true"(){

        given:
        dsl "serviceAccount 'trigger_sa'"

        when: "Load DSL Code"
        def proc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/triggers/release/",
            pool: "$defaultPool"
          ]
        )""")

        then: "job completes"
        assert proc.jobId
        assert getJobProperty("outcome", proc.jobId) == "success"

        then: "check trigger was created"
        def response = dsl "getTrigger( projectName: 'triggerReleaseProject', releaseName: 'testRelease', triggerName: 'testTrigger')"
        assert response?.trigger

        when: "modify trigger parameters"
        response = dsl "modifyTrigger( projectName: 'triggerReleaseProject', releaseName: 'testRelease', triggerName: 'testTrigger', actualParameter: ['param1': 'newValue'], pipelineParameter: ['param2': 'newValue'], pluginParameter: ['pushEvent': 'false'], pluginKey: 'webhook-plugin')"
        assertParameter(response.trigger.actualParameters, 'param1', 'newValue')
        assertParameter(response.trigger.pipelineParameters, 'param2', 'newValue')
        assertParameter(response.trigger.pluginParameters, 'pushEvent', 'false')

        and: "import project with overwrite=true"
        proc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/triggers/release/",
            overwrite: 'true',
            pool: "$defaultPool"
          ]
        )""")

        then: "job completes"
        assert proc.jobId
        assert getJobProperty("outcome", proc.jobId) == "success"

        then: "check trigger was updated"
        def result = dsl "getTriggers( projectName: 'triggerReleaseProject', releaseName: 'testRelease')"
        assert result?.trigger.size == 1
        assert result.trigger[0].triggerName == 'testTrigger'
        assertParameter(result.trigger[0].actualParameters, 'param1', 'actParam')
        assertParameter(result.trigger[0].pipelineParameters, 'param2', 'entryParam')
        assertParameter(result.trigger[0].pluginParameters, 'pushEvent', 'true')

        cleanup:
        deleteProjects([pr: 'triggerReleaseProject'], false)
    }

    def "import pipeline with overwrite=true"(){

        given:
        dsl "serviceAccount 'trigger_sa'"

        when: "Load DSL Code"
        def proc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_pipeline/",
            pool: "$defaultPool"
          ]
        )""")

        then: "job completes"
        assert proc.jobId
        def outcome = getJobProperty("outcome", proc.jobId)
        assert outcome == "warning" || outcome == "success"

        when: "create trigger"
        def response = dsl "createTrigger( projectName: 'overwrite_installProject', pipelineName: 'p12', triggerName: 'testTrigger', pluginParameter: ['pushEvent': 'false'], pluginKey: 'webhook-plugin', serviceAccountName: 'trigger_sa', triggerType: 'webhook')"
        assert response?.trigger
        assert response.trigger.pluginKey == 'webhook-plugin'
        assert response.trigger.projectName == 'overwrite_installProject'
        assert response.trigger.pipelineName == 'p12'
        assert response.trigger.triggerType == 'webhook'
        assert response.trigger.triggerEnabled == '1'

        and: "import project with overwrite=true"
        proc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_pipeline/",
            overwrite: 'true',
            pool: "$defaultPool"
          ]
        )""")

        then: "job completes"
        assert proc.jobId
        def property = getJobProperty("outcome", proc.jobId)
        assert property == "warning" || property == "success"

        then: "check trigger was deleted"
        def result = dsl "getTriggers( projectName: 'overwrite_installProject', pipelineName: 'p12')"
        assert result?.trigger == null

        cleanup:
        deleteProjects([pr: 'overwrite_installProject'], false)
    }

    def void assertParameter(obj, param, value)
    {
        assert obj
        assert obj.parameterDetail?.parameterName == [param]
        assert obj.parameterDetail?.parameterValue == [value]
    }
}
