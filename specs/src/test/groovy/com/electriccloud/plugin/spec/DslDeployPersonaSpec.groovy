package com.electriccloud.plugin.spec

import spock.lang.Shared

class DslDeployPersonaSpec
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
    }

    def "deploy presona, personaPage, personaCategory, user, group"(){

        def personaPageName1 = 'myPersonaPage1'
        def personaPageName2 = 'myPersonaPage2'
        def personaPageName3 = 'myPersonaPage3'

        def personaCategoryName1 = 'myPersonaCategory1'
        def personaCategoryName2 = 'myPersonaCategory2'
        def personaCategoryName3 = 'myPersonaCategory3'

        def personaName1 = 'vip1'
        def personaName2 = 'vip2'

        def userName = 'testUser'
        def groupName  = 'testGroup'

        given: "the top level objects code (presona, personaPage, personaCategory, user, group)"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects/",
            pool: "$defaultPool"
          ]
        )""")
        then: "job completes with warning as no metadata.json for ordered children"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "check persona pages were created"
        def personaPage1 = dsl """getPersonaPage (personaPageName: '$personaPageName1')"""
        assert personaPage1
        assert "www.cnn.com".equals(personaPage1?.personaPage?.url)

        def personaPage2 = dsl """getPersonaPage (personaPageName: '$personaPageName2')"""
        assert personaPage2
        assert "www.cnn.com".equals(personaPage2?.personaPage?.url)

        def personaPage3 = dsl """getPersonaPage (personaPageName: '$personaPageName3')"""
        assert personaPage3
        assert "www.cnn.com".equals(personaPage3?.personaPage?.url)

        then: "check persona categories were created"
        def personaCategory1 = dsl """getPersonaCategory(personaCategoryName: '$personaCategoryName1')"""
        assert personaCategory1?.personaCategory?.personaPages?.personaPage?.size == 1
        assert personaCategory1?.personaCategory?.personaPages?.personaPage?.personaPageName[0].equals('myPersonaPage1')

        def personaCategory2 = dsl """getPersonaCategory(personaCategoryName: '$personaCategoryName2')"""
        assert personaCategory2?.personaCategory?.personaPages?.personaPage?.size == 2
        assert personaCategory2?.personaCategory?.personaPages?.personaPage?.personaPageName[0].equals('myPersonaPage1')
        assert personaCategory2?.personaCategory?.personaPages?.personaPage?.personaPageName[1].equals('myPersonaPage2')

        def personaCategory3 = dsl """getPersonaCategory(personaCategoryName: '$personaCategoryName3')"""
        assert personaCategory3?.personaCategory?.personaPages?.personaPage?.size == 3
        assert personaCategory3?.personaCategory?.personaPages?.personaPage?.personaPageName[0].equals('myPersonaPage1')
        assert personaCategory3?.personaCategory?.personaPages?.personaPage?.personaPageName[1].equals('myPersonaPage2')
        assert personaCategory3?.personaCategory?.personaPages?.personaPage?.personaPageName[2].equals('myPersonaPage3')

        then: "check personas were created"
        def persona1 = dsl """getPersona(personaName : '$personaName1')"""
        assert persona1?.persona?.personaDetail?.size == 2
        assert persona1?.persona?.personaDetail[0]?.personaCategoryName.equals('myPersonaCategory1')
        assert persona1?.persona?.personaDetail[0]?.personaPages?.personaPage?.size == 1
        assert persona1?.persona?.personaDetail[0]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert persona1?.persona?.personaDetail[1]?.personaPages?.personaPage?.size == 2
        assert persona1?.persona?.personaDetail[1]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert persona1?.persona?.personaDetail[1]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")

        def persona2 = dsl """getPersona(personaName : '$personaName2')"""
        assert persona2?.persona?.personaDetail?.size == 1
        assert persona2?.persona?.personaDetail[0]?.personaCategoryName.equals('myPersonaCategory3')
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage?.size == 3
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage[2]?.personaPageName.equals("myPersonaPage3")

        then: "check user was created"
        def user = dsl """getUser(userName : '$userName')"""
        assert user?.user?.personaName.size == 2
        assert user?.user?.personaName[0].equals("vip1")
        assert user?.user?.personaName[1].equals("vip2")

        then: "check group was created"
        def group = dsl """getGroup(groupName : '$groupName')"""
        assert group?.group?.personaName.size == 2
        assert group?.group?.personaName[0].equals("vip1")
        assert group?.group?.personaName[1].equals("vip2")

        cleanup:
        dsl """deletePersona (personaName: '$personaName1')"""
        dsl """deletePersona (personaName: '$personaName2')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName1')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName2')"""
        dsl """deletePersonaCategory (personaCategoryName: '$personaCategoryName3')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName1')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName2')"""
        dsl """deletePersonaPage (personaPageName: '$personaPageName3')"""
        dsl """deleteGroup (groupName: '$groupName')"""
        dsl """deleteUser (userName: '$userName')"""

    }

}
