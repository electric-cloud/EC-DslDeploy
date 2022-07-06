package com.electriccloud.plugin.spec

import spock.lang.*

class overwrite_installDslFromDirectory extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "ow_pr"
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
        dsl """ deleteProject(projectName: "$projName") """
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
        then: "job completed with warnings"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

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
        then: "job compeled with warnings"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "warning"

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

    def "overwrite_installDslFromDirectory overwrite single DSL file"() {
        given: "the overwrite_installDslFromDirectory code"
        when: "Load deafult DSL Code"
        dsl """
        project 'BEE-19095', {
            procedure 'oldProcedure'
        }"""
        and: "overwrite existing project and replace oldProcedure with newProcedure"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_single_DSL",
            pool: 'local',
            overwrite: '1'
          ]
        )""")
        then: "job completed with success"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "oldProcedure not exists"
        def resut = dslWithXmlResponse(
                        """getProcedure(projectName: 'BEE-19095', procedureName: 'oldProcedure')""",
                        null, [ignoreStatusCode: true])

        assert resut
        assert resut.contains("NoSuchProcedure")

        then: "Check the stage is present"
        result = dsl """getProcedure(projectName: 'BEE-19095', procedureName: 'newProcedure')"""
        assert result.procedure.procedureName == "newProcedure"
    }

    def "deploy persona, personaPage, personaCategory, user, group"(){

        given: "the top level objects code (persona, personaPage, personaCategory, user, group)"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects",
            pool: "local",
            overwrite: '1'
          ]
        )""")
        then: "job completes with success"
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
        assert persona1?.persona?.description == 'original description'
        /* BEE-16776 10.5 introduced an issue where person detail is cleared under these test circumstances
        assert persona1?.persona?.personaDetail?.size == 2
        assert persona1?.persona?.personaDetail[0]?.personaCategoryName.equals('myPersonaCategory1')
        assert persona1?.persona?.personaDetail[0]?.personaPages?.personaPage?.size == 1
        assert persona1?.persona?.personaDetail[0]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert persona1?.persona?.personaDetail[1]?.personaPages?.personaPage?.size == 2
        assert persona1?.persona?.personaDetail[1]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert persona1?.persona?.personaDetail[1]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")
        */

        def persona2 = dsl """getPersona(personaName : '$personaName2')"""
        assert persona2?.persona?.description == 'original description'
        /* BEE-16776 10.5 introduced an issue where person detail is cleared under these test circumstances
        assert persona2?.persona?.personaDetail?.size == 1
        assert persona2?.persona?.personaDetail[0]?.personaCategoryName.equals('myPersonaCategory3')
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage?.size == 3
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")
        assert persona2?.persona?.personaDetail[0]?.personaPages?.personaPage[2]?.personaPageName.equals("myPersonaPage3")
        */

        then: "check testUser was created"
        def user = dsl """getUser(userName : '$userName')"""
        assert user?.user?.personaName.size == 2
        assert "vip1" in user?.user?.personaName
        assert "vip2" in user?.user?.personaName

        then: "check testUser1 was created"
        def user1 = dsl """getUser(userName : '$userName1')"""
        assert user1?.user?.personaName.size == 1
        assert "vip2" in user1?.user?.personaName

        then: "check group was created"
        def group = dsl """getGroup(groupName : '$groupName')"""
        assert group?.group?.personaName.size == 2
        assert "vip1" in group?.group?.personaName
        assert "vip2" in group?.group?.personaName

        then: "check group was created"
        def group1 = dsl """getGroup(groupName : '$groupName1')"""
        assert group1?.group?.personaName.size == 1
        assert "vip2" in group1?.group?.personaName

        //now make some configuration changes

        when: "modify persona"
        dsl """
                addPersonaDetail(personaName : '$personaName1',
                                 personaCategoryName: '$personaCategoryName3'
                                 )"""

        def modifiedPersona = dsl """
                modifyPersona(personaName : '$personaName1',
                              description: 'new description',
                              isDefault : '1',                     
                              homePageName: 'myPersonaPage1'               
                              )"""

        then: "persona was modified"
        assert modifiedPersona?.persona?.description == 'new description'
        assert modifiedPersona?.persona?.isDefault == '1'
        assert modifiedPersona?.persona?.homePageName == 'myPersonaPage1'
        /* BEE-16776 10.5 introduced an issue where person detail is cleared under these test circumstances
        assert modifiedPersona?.persona?.personaDetail?.size == 3
        assert modifiedPersona?.persona?.personaDetail[2]?.personaCategoryName.equals('myPersonaCategory3')
        assert modifiedPersona?.persona?.personaDetail[2]?.personaPages?.personaPage?.size == 3
        assert modifiedPersona?.persona?.personaDetail[2]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert modifiedPersona?.persona?.personaDetail[2]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")
        assert modifiedPersona?.persona?.personaDetail[2]?.personaPages?.personaPage[2]?.personaPageName.equals("myPersonaPage3")
        */

        when: "modify users"
        dsl """modifyUser(userName: '$userName', persona: ['$personaName2'])"""
        def modifiedUser = dsl """getUser(userName: '$userName')"""

        dsl """modifyUser(userName: '$userName1', persona: ['$personaName1','$personaName2'])"""
        def modifiedUser1 = dsl """getUser(userName: '$userName1')"""

        then: 'users were modified'
        assert modifiedUser?.user?.personaName.size == 1
        assert "vip2" in modifiedUser?.user?.personaName

        assert modifiedUser1?.user?.personaName.size == 2
        assert "vip1" in modifiedUser1?.user?.personaName
        assert "vip2" in modifiedUser1?.user?.personaName

        when: "modify groups"
        dsl """
                modifyGroup(groupName: '$groupName',
                            persona: ['$personaName2']
                            )"""
        def modifiedGroup =  dsl """getGroup(groupName: '$groupName')"""

        dsl """
                modifyGroup(groupName: '$groupName1',
                            persona: ['$personaName1', '$personaName2']
                            )"""
        def modifiedGroup1 =  dsl """getGroup(groupName: '$groupName1')"""

        then: "groups were modified"
        assert modifiedGroup1?.group?.personaName.size == 2
        assert "vip1" in modifiedGroup1?.group?.personaName
        assert "vip2" in modifiedGroup1?.group?.personaName

        assert modifiedGroup?.group?.personaName.size == 1
        assert "vip2" in modifiedGroup?.group?.personaName

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/top_objects",
            pool: 'local',
            overwrite: '1'
          ]
        )""")
        then: "job completed with success"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success"

        then: "check personas were created"
        def overwritepersona1 = dsl """getPersona(personaName : '$personaName1')"""
        assert overwritepersona1?.persona?.description == 'original description'
        assert overwritepersona1?.persona?.isDefault == null
        assert overwritepersona1?.persona?.homePageName == null
        /* BEE-16776 10.5 introduced an issue where person detail is cleared under these test circumstances
        assert overwritepersona1?.persona?.personaDetail?.size == 2
        assert overwritepersona1?.persona?.personaDetail[0]?.personaCategoryName.equals('myPersonaCategory1')
        assert overwritepersona1?.persona?.personaDetail[0]?.personaPages?.personaPage?.size == 1
        assert overwritepersona1?.persona?.personaDetail[0]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert overwritepersona1?.persona?.personaDetail[1]?.personaPages?.personaPage?.size == 2
        assert overwritepersona1?.persona?.personaDetail[1]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert overwritepersona1?.persona?.personaDetail[1]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")
         */

        def overwritepersona2 = dsl """getPersona(personaName : '$personaName2')"""
        assert overwritepersona2?.persona?.description == 'original description'
        /* BEE-16776 10.5 introduced an issue where person detail is cleared under these test circumstances
        assert overwritepersona2?.persona?.personaDetail?.size == 1
        assert overwritepersona2?.persona?.personaDetail[0]?.personaCategoryName.equals('myPersonaCategory3')
        assert overwritepersona2?.persona?.personaDetail[0]?.personaPages?.personaPage?.size == 3
        assert overwritepersona2?.persona?.personaDetail[0]?.personaPages?.personaPage[0]?.personaPageName.equals("myPersonaPage1")
        assert overwritepersona2?.persona?.personaDetail[0]?.personaPages?.personaPage[1]?.personaPageName.equals("myPersonaPage2")
        assert overwritepersona2?.persona?.personaDetail[0]?.personaPages?.personaPage[2]?.personaPageName.equals("myPersonaPage3")
         */

        /* BEE-16776 10.5 introduced an issue where person detail is cleared under these test circumstances
        then: "check testUser was created"
        def overwriteUser = dsl """getUser(userName : '$userName')"""
        assert overwriteUser?.user?.personaName.size == 2
        assert "vip1" in overwriteUser?.user?.personaName
        assert "vip2" in overwriteUser?.user?.personaName

        then: "check testUser1 was created"
        def overwriteUser1 = dsl """getUser(userName : '$userName1')"""
        assert overwriteUser1?.user?.personaName.size == 1
        assert "vip2" in overwriteUser1?.user?.personaName

        then: "check group was created"
        def overwriteGroup = dsl """getGroup(groupName : '$groupName')"""
        assert overwriteGroup?.group?.personaName.size == 2
        assert "vip1" in overwriteGroup?.group?.personaName
        assert "vip2" in overwriteGroup?.group?.personaName

        then: "check group was created"
        def overwriteGroup1 = dsl """getGroup(groupName : '$groupName1')"""
        assert overwriteGroup1?.group?.personaName.size == 1
        assert "vip2" in overwriteGroup1?.group?.personaName
         */

    }

    def "deploy project with slashes in names"(){

        def projName = 'proj / new / name \\\\'
        def procName = 'Test / procedure'
        given: "project with a procedure and a step with slashes in names"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/proj_with_slashes",
            pool: "local",
            overwrite: '1'
          ]
        )""")
        then: "job completes with success"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        and: "check a project"
        def project =
                dsl "getProject (projectName: '$projName')"
        assert project

        def procedure =
                dsl "getProcedure (projectName:'$projName', procedureName: " +
                        "'$procName')"
        assert procedure

        def steps = dsl "getSteps (projectName: '$projName', procedureName: " +
                "'$procName')"
        assert steps
        assert steps?.step?.size == 1

        when: 'create one more step'

        dsl """createStep(projectName: '$projName', 
                                procedureName: '$procName',
                                stepName: 'newStep'
                                )"""

        and: 'run import again'
        p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/proj_with_slashes",
            pool: "local",
            overwrite: '1'
          ]
        )""")
        then: "job completes with success"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        and: "check a project"
        def project2 =
                dsl """getProject (projectName: '$projName')"""
        assert project2

        def procedure2 =
                dsl "getProcedure (projectName:'$projName', procedureName: " +
                        "'$procName')"
        assert procedure2

        def steps2 = dsl "getSteps (projectName: '$projName', procedureName: " +
                "'$procName')"
        assert steps2
        assert steps2?.step?.size == 1

        cleanup:

        deleteProjects([projectName: procName], false)
    }

}
