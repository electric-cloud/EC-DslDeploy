package com.electriccloud.plugin.spec

import spock.lang.Shared

class incr_addedChangedDeleted_personaProcedureProperty
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "incr_project_ACD"

    static def personaCategoryAdd = 'myPersonaCategoryAdd'
    static def personaCategoryChange = 'myPersonaCategoryChange'
    static def personaCategoryDelete = 'myPersonaCategoryDelete'

    static def procedureAdd = 'myProcedureAdd'
    static def procedureChange = 'myProcedureChange'
    static def procedureDelete = 'myProcedureDelete'

    static def propertyAdd = 'myPropertyAdd'
    static def propertyChange = 'myPropertyChange'
    static def propertyDelete = 'myPropertyDelete'


    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """deleteProject(projectName: "$projName") """
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryAdd')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryChange')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryDelete')"""
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryAdd')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryChange')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryDelete')"""
    }

//    def "incremental import non-project entities with Add, Change, Delete change list"() {
//        given: "Some non-project and project entities"
//        dsl """personaCategory '$personaCategoryChange', {description='Changeable description'}"""
//        dsl """personaCategory '$personaCategoryDelete', {description='Deletable description'}"""
//        dsl """
//project '$projName', {
//    procedure '$procedureChange', {
//        description='Changeable description'
//        property '$propertyChange', value: 'Changeable value', {description="Changeable description"}
//        property '$propertyDelete', value: 'Deletable value', {description="Deletable description"}
//    }
//    procedure '$procedureDelete', {description='Deleteable description'}
//}
//"""
//        when: ""
//        def runProc = runProcedureDsl("""
//        runProcedure(
//          projectName: "/plugins/$pName/project",
//          procedureName: "importDslFromGitNew",
//          actualParameter: [
//            config: "this value does not matter when skipping Checkout changes, but is required",
//            dest: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects",
//            incrementalImport: "1",
//            repoUrl: "skip_checkout_changes_step_please",
//            rsrcName: "local"
//          ]
//        )""")
//        then: "job succeeds"
//        assert runProc.jobId
//        assert getJobProperty("outcome", runProc.jobId) == "success"
//
//        then: "check users were NOT created"
//        def users = dsl """getUsers(filter : 'vip*')"""
//        assert users.size() == 0
//
//        then: "check groups were NOT created"
//        def groups = dsl """getGroups(filter : 'testGroup*')"""
//        assert groups.size() == 0
//
//    }


//    def "incremental import project entities with No-OP change list"() {
//        given: "project with procedure and step"
//        when: "Load DSL Code"
//        def projects = dsl """getProjects()"""
//        def projectCount = projects.size()
//        def runProc = runProcedureDsl("""
//        runProcedure(
//          projectName: "/plugins/$pName/project",
//          procedureName: "importDslFromGitNew",
//          actualParameter: [
//            config: "this value does not matter when skipping Checkout changes, but is required",
//            dest: "$plugDir/$pName-$pVersion/lib/dslCode/proj_with_slashes",
//            incrementalImport: "1",
//            repoUrl: "skip_checkout_changes_step_please",
//            rsrcName: "local"
//          ]
//        )""")
//        then: "job succeeds"
//        assert runProc.jobId
//        assert getJobProperty("outcome", runProc.jobId) == "success"
//
//        then: "check project was NOT created"
//        def testProjects = dsl """getProjects()"""
//        assert testProjects.size() == projectCount
//
//    }
}
