package com.electriccloud.plugin.spec

import spock.lang.Shared

class PluginConfigurationSpec extends PluginTestHelper {

    static String pName='EC-DslDeploy'
    @Shared String pVersion
    @Shared String plugDir
    static String projName="pluginConfProj"


    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
    }

    /**
     * CEV-28776
     */
    def "deploy project with plugin configuration"() {
        given: "application dsl code"
        when: "Load DSL Code"
        def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/plugin_conf",
            pool: "$defaultPool"
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "check that plugin configurations were created"
        def pluginConf1 = dsl """getPluginConfiguration(
projectName: '$projName',  pluginConfigurationName: 'git-config1')"""
        assert pluginConf1

        def pluginConf2 = dsl """getPluginConfiguration(
projectName: '$projName',  pluginConfigurationName: 'git-config2')"""
        assert pluginConf2

        when: 'modify description for config1'
        dsl """modifyPluginConfiguration(projectName: '$projName', 
 pluginConfigurationName: 'git-config1', description: 'test')"""

        and: 'run import with overwrite=true'
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/plugin_conf",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success"

        and:
        def pluginConf11 = dsl """getPluginConfiguration(
projectName: '$projName',  pluginConfigurationName: 'git-config1')"""
        assert pluginConf11
        assert pluginConf11.description == null || pluginConf11.description == ''
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)

    }
}
