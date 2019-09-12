package com.electriccloud.plugin.spec

import spock.lang.*

class overwrite_installDslFromDirectory extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "overwrite_installProject"

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """ deleteProject(projectName: "$projName") """
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
    }

    // Check sample
    def "overwrite_installDslFromDirectory test suite upload"() {
        given: "the overwrite_installDslFromDirectory code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_pipeline",
            pool: 'local'
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        when: "add content to pipeline"
        dsl """
        createStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "newStage"
        )"""

        // check master component
        then: "Check the stage is present"
        println "Checking new stage exists"
        def newStage = dsl """
        getStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "newStage"
        )"""
        assert newStage.stage.stageName == "newStage"

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_pipeline",
            pool: 'local',
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success"

        then: "stage not exists"
        println "Checking new stage is not exists"

        def getTaskResult =
                dslWithXmlResponse("""
        getStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "newStage"
        )""", null, [ignoreStatusCode: true])

        assert getTaskResult
        assert getTaskResult.contains("NoSuchStage")
    }
}
