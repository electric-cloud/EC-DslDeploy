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
    static def personaPageName1 = 'myPersonaPage1'
    static def personaPageName2 = 'myPersonaPage2'
    static def personaPageName3 = 'myPersonaPage3'

    static def personaCategoryName1 = 'myPersonaCategory1'
    static def personaCategoryName2 = 'myPersonaCategory2'
    static def personaCategoryName3 = 'myPersonaCategory3'

    static def personaName1 = 'vip1'
    static def personaName2 = 'vip2'

    static def userName = 'testUser'
    static def userName1 = 'testUser1'
    static def groupName  = 'testGroup'
    static def groupName1  = 'testGroup1'

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
            config: "this value does not matter when skipping Checkout changes, but is required",
            dest: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects",
            pathToFileList: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects/change_list_na.json",
            repoUrl: "skip_checkout_changes_step_please",
            rsrcName: "local"          
          ]
        )""")
        then: "job succeeds"
        assert runProc.jobId
        assert getJobProperty("outcome", runProc.jobId) == "success"

        then: "check users were NOT created"
        def users = dsl """getUsers(filter : 'testUser*')"""
        assert users.size() == 0

        then: "check groups were NOT created"
        def groups = dsl """getGroups(filter : 'testGroup*')"""
        assert groups.size() == 0
    }


    def "incremental import project entities with No-OP change list"() {
        given: "project with procedure and step"
        when: "Load DSL Code"
        def projects = dsl """getProjects()"""
        def projectCount = projects.size()
        def runProc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "importDslFromGitNew",
          actualParameter: [
            config: "this value does not matter when skipping Checkout changes, but is required",
            dest: "$plugDir/$pName-$pVersion/lib/dslCode/proj_with_slashes",
            pathToFileList: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects/change_list_na.json",
            repoUrl: "skip_checkout_changes_step_please",
            rsrcName: "local"          
          ]
        )""")
        then: "job succeeds"
        assert runProc.jobId
        assert getJobProperty("outcome", runProc.jobId) == "success"

        then: "check project was NOT created"
        def testProjects = dsl """getProjects()"""
        assert testProjects.size() == projectCount

    }
}
