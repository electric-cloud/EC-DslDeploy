package com.electriccloud.plugin.spec

import spock.lang.Shared

import java.util.regex.Matcher
import java.util.regex.Pattern

class incr_importDslFromGitNew_nochanges extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "incr_project"
    def personaPageName1 = 'myPersonaPage1'
    def personaPageName2 = 'myPersonaPage2'
    def personaPageName3 = 'myPersonaPage3'

    def personaCategoryName1 = 'myPersonaCategory1'
    def personaCategoryName2 = 'myPersonaCategory2'
    def personaCategoryName3 = 'myPersonaCategory3'

    def personaName1 = 'vip1'
    def personaName2 = 'vip2'

    def userName = 'testUser'
    def userName1 = 'testUser1'
    def groupName  = 'testGroup'
    def groupName1  = 'testGroup1'

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """deleteProject(projectName: "$projName") """
        dsl """deletePersona (personaName: '$personaName1')"""
        dsl """deletePersona (personaName: '$personaName2')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName1')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName2')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName3')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName1')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName2')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName3')"""
        dsl """deleteGroup (groupName: '$groupName')"""
        dsl """deleteGroup (groupName: '$groupName1')"""
        dsl """deleteUser (userName: '$userName')"""
        dsl """deleteUser (userName: '$userName1')"""
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
        dsl """deletePersona (personaName: '$personaName1')"""
        dsl """deletePersona (personaName: '$personaName2')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName1')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName2')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName3')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName1')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName2')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName3')"""
        dsl """deleteGroup (groupName: '$groupName')"""
        dsl """deleteGroup (groupName: '$groupName1')"""
        dsl """deleteUser (userName: '$userName')"""
        dsl """deleteUser (userName: '$userName1')"""
    }

    def "incremental import non-project entities with No-OP change list"() {
        given: "top_objects: groups, personaCategories, personaPages, personas, users"
        when: "Load DSL Code"
        def runProc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "importDslFromGitNew",
          actualParameter: [
            config: "this value doies not matter but is required",
            dest: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects",
            pathToFileList: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects/change_list_na",
            repoUrl: "skip_checkout_changes_step_please",
            rsrcName: "local"          
          ]
        )""")
        then: "job succeeds"
        assert runProc.jobId
        assert getJobProperty("outcome", runProc.jobId) == "success"

        then: "check users were NOT created"
        def users = dsl """getUsers(filter : 'vip*')"""
        assert users.size() == 0

        then: "check groups were NOT created"
        def groups = dsl """getGroups(filter : 'testGroup*')"""
        assert groups.size() == 0

    }

}
