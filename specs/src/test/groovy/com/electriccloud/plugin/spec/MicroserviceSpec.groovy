package com.electriccloud.plugin.spec

import spock.lang.Shared

class MicroserviceSpec
        extends PluginTestHelper {

    static String pName='EC-DslDeploy'
    @Shared String pVersion
    @Shared String plugDir
    static String projName="CEV-28776"


    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
    }

    /**
     * CEV-28776
     */
    def "deploy application with microservice"() {
        given: "application dsl code"
        when: "Load DSL Code"
        def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/applications/CEV-28776",
            pool: "$defaultPool"
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "check that application was created"
        def app = dsl """getApplication(
projectName: '$projName',  applicationName: 'app1')"""
        assert app

        then: "check that microservice was created"
        def microservices = dsl """getMicroservices(
projectName: '$projName', applicationName: 'app1')"""
        assert microservices?.microservice?.size == 1
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
        dsl """
      deleteResource(resourceName: "k8s res")
   """
    }

}
