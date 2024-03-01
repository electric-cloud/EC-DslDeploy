package com.electriccloud.plugin.spec

import spock.lang.Shared

class MicroserviceSpec
        extends PluginTestHelper
{

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

    /**
     * BEE-43082
     */
    def "BEE-43082: add process as children support in microservice" ()
    {
        def dslDir = '/tmp/' + randomize('dsl')

        given: "Load microservice dsl code"
        dslFile("BEE-43082.dsl")

        when: "check that microservice process contains formal parameter p1"
        def p1 = dsl """getFormalParameter(projectName: 'BEE-43082',
                                           applicationName: 'BEE-43082',
                                           microserviceName: 'BEE-43082',
                                           processName: 'Deploy Microservice Process',
                                           formalParameterName: 'p1'
                                           )"""
        then:
        assert p1

        when: "check that microservice process contains formal parameter p2"
        def p2 = dsl """getFormalParameter(projectName: 'BEE-43082',
                                           applicationName: 'BEE-43082',
                                           microserviceName: 'BEE-43082',
                                           processName: 'Deploy Microservice Process',
                                           formalParameterName: 'p2'
                                           )"""
        then:
        assert p2

        when: "Generate DSL files"
        def result1 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "generateDslToDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            includeAllChildren: '1',
            suppressNulls: '1',
            suppressEmpty: '1',
            objectType: 'project',
            objectName: 'BEE-43082',
            httpIdleTimeout: '180'
          ]
        )""")
        then:
        assert result1.jobId
        def outcome1 = getJobProperty("outcome", result1.jobId)
        assert outcome1 == "success"

        when: "Remove formal parameter p1"
        dsl """deleteFormalParameter(projectName: 'BEE-43082',
                                     applicationName: 'BEE-43082',
                                     microserviceName: 'BEE-43082',
                                     processName: 'Deploy Microservice Process',
                                     formalParameterName: 'p1'
                                     )"""
        then: "Add formal parameter p3"
        def p3 = dsl """createFormalParameter(projectName: 'BEE-43082',
                                           applicationName: 'BEE-43082',
                                           microserviceName: 'BEE-43082',
                                           processName: 'Deploy Microservice Process',
                                           formalParameterName: 'p3'
                                           )"""
        assert p3

        when: "Load DSL Code in overwrite mode with p1 and p2 formal parameters only"
        def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        when: "check that microservice process contains formal parameter p1"
        def updatedP1 = dsl """getFormalParameter(projectName: 'BEE-43082',
                                                  applicationName: 'BEE-43082',
                                                  microserviceName: 'BEE-43082',
                                                  processName: 'Deploy Microservice Process',
                                                  formalParameterName: 'p1'
                                                  )"""
        then:
        assert updatedP1

        when: "check that microservice process contains formal parameter p2"
        def updatedP2 = dsl """getFormalParameter(projectName: 'BEE-43082',
                                                  applicationName: 'BEE-43082',
                                                  microserviceName: 'BEE-43082',
                                                  processName: 'Deploy Microservice Process',
                                                  formalParameterName: 'p2'
                                                  )"""
        then:
        assert updatedP2

        when: "check that microservice process contains just 2 formal parameters"
        def formalParams = dsl """getFormalParameters(projectName: 'BEE-43082',
                                                     applicationName: 'BEE-43082',
                                                     microserviceName: 'BEE-43082',
                                                     processName: 'Deploy Microservice Process'
                                                     )"""
        then:
        assert formalParams?.formalParameter?.size() == 2
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
        dsl """
      deleteResource(resourceName: "k8s res")
   """
    }
}
