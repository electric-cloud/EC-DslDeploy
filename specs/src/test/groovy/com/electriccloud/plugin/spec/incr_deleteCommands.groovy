package com.electriccloud.plugin.spec

import spock.lang.Shared

class incr_deleteCommands
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "incr_project_deletes"
    static def personaPageName1 = 'myPersonaPage1'


    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """deleteProject(projectName: "$projName") """

    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
    }

    def "increment import of only delete commands"() {
        given: "A project"
        when: "create a project"
        dsl """createProject(projectName: "$projName")"""
        then: "project exists"
        def project = dsl """getProject(projectName: "$projName")"""
        assert project != null

        when: "Run incremental import with a change list full of deletes"
        def runProc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "importDslFromGitNew",
          actualParameter: [
            config: "this value does not matter when skipping Checkout changes, but is required",
            dest: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects",
            pathToFileList: "$plugDir/$pName-$pVersion/lib/dslCode/incremental_ChangeLists/change_list_deletes.json",
            repoUrl: "skip_checkout_changes_step_please",
            rsrcName: "local"
          ]
        )""")
        then: "job succeeds"
        assert runProc.jobId
        assert getJobProperty("outcome", runProc.jobId) == "success"

    }

}
