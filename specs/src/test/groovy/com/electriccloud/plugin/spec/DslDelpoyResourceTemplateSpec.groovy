package com.electriccloud.plugin.spec

import spock.lang.Shared

class DslDelpoyResourceTemplateSpec
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dslFile("maven_plugin.dsl")
    }

    // overwrite with pipeline
    def "deploy multiple resource templates"(){

        def projectName = 'rt'
        def projects = [project : projectName]

        given: "the schedule project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/resourceTemplates/deployResourceTemplates/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
        then: "job completes successfully"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "verify first resource template was creared"
        def resTempl1 = dsl """getResourceTemplate(projectName: '$projectName', resourceTemplateName: 't1')"""
        assert resTempl1

        then: "verify second resource template was creared"
        def resTempl2 = dsl """getResourceTemplate(projectName: '$projectName', resourceTemplateName: 't2')"""
        assert resTempl2

        cleanup:
        deleteProjects(projects, false)

    }
}
