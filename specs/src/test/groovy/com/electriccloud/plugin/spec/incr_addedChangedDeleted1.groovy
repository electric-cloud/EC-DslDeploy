package com.electriccloud.plugin.spec

import spock.lang.Shared

class incr_addedChangedDeleted1
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir

    // The following names must match what is contained in the test repository ...
    static String projectName = "incremental_test_project"

    static def userAddedName = 'userAdded'
    static def userToChangeName = 'userToChange'
    static def userToDeleteName = 'userToDelete'

    static def procedureAddedName = 'procedureAdded'
    static def procedureToChangeName = 'procedureToChange'
    static def procedureToDeleteName = 'procedureToDelete'

    static def propertyAddedName = 'propertyAdded'
    static def propertyToChangeName = 'propertyToChange'
    static def propertyToDeleteName = 'propertyToDelete'

    static def branch = 'incr_addedChangedDeleted1'
    static def repoURL = 'https://github.com/cloudbees/CD_EC-DslDeploy_BuildTests_Data'
    static def repoName = repoURL.tokenize("/")[-1]
    static def dataTrackerProperty = "ecreport_data_tracker"
    static def metadataProperty = "$dataTrackerProperty/EC-Git-$repoName-git_changelog"

    static def commitID1 = 'dffe784aaca096f44164406b0eb3c9755c9e7f37'
    static def commitID2 = '8c93a05539d040378350f50ad1d230870ad7bdf1'
    // ... the preceding names must match what is contained in the test repository

    static def pluginConfigurationName = 'pluginConfigurationName'

    def doSetupSpec()
    {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """deleteProject(projectName: "$projectName") """
        dsl """deleteUser (userName: '$userAddedName')"""
        dsl """deleteUser (userName: '$userToChangeName')"""
        dsl """deleteUser (userName: '$userToDeleteName')"""
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projectName)
        dsl """deleteUser (userName: '$userAddedName')"""
        dsl """deleteUser (userName: '$userToChangeName')"""
        dsl """deleteUser (userName: '$userToDeleteName')"""
    }

    def "Incremental Import: Users, Project, Procedure, Properties and commit IDs are different"() {
        given: "Users, one Project, Procedures, Properties"
        def testProject = dsl """
project '$projectName', {
    procedure '$procedureToChangeName', {
      description = 'procedure description $procedureToChangeName'
    }
    procedure '$procedureToDeleteName', {
      description = 'procedure description $procedureToDeleteName'
    }
    property '$propertyToChangeName', value: 'property value $propertyToChangeName', {expandable = '0'}
    property '$propertyToDeleteName', value: 'property value $propertyToDeleteName', {expandable = '0'}

    pluginConfiguration '$pluginConfigurationName', {
        field = [
          'authType': 'token',
          'debugLevel': '0',
          'library': 'jgit',
          'token_credential': 'token_credential',
        ]
        pluginKey = 'EC-Git'

        addCredential 'token_credential', {
          passwordRecoveryAllowed = '1'
          password='$GIT_TOKEN_CLOUDBEES_CD_PLUGINS_BOT'
        }
    }
}
"""

        //           password='${(p1+p2+p3).decodeBase64() as String}'
        dsl """createUser(userName:'$userToChangeName', fullUserName: 'full user name - $userToChangeName')"""
        dsl """createUser(userName:'$userToDeleteName', fullUserName: 'full user name - $userToDeleteName')"""

        def pluginConfiguration = dsl """getPluginConfiguration(projectName: '$projectName', pluginConfigurationName:'$pluginConfigurationName')"""
        def userAdded
        def userToChange = dsl """getUser(userName: '$userToChangeName')"""
        def userToDelete = dsl """getUser(userName: '$userToDeleteName')"""
        def procedureAdded
        def procedureToChange = dsl """getProcedure(projectName:'$projectName', procedureName:'$procedureToChangeName')"""
        def procedureToDelete = dsl """getProcedure(projectName:'$projectName', procedureName:'$procedureToDeleteName')"""
        def propertyAdded
        def propertyToChange = dsl """getProperty(projectName:'$projectName', propertyName:'$propertyToChangeName')"""
        def propertyToDelete = dsl """getProperty(projectName:'$projectName', propertyName:'$propertyToDeleteName')"""

        testProject != null
        pluginConfiguration != null

        userToChange != null
        userToDelete != null
        //userToChange.fullUserName == "full user name - userToChange"
        //println "====================>  $userToChange <===================="

        procedureToChange != null
        procedureToDelete != null
        //procedureToChange.description == "procedure description procedureToChange"
        //println "====================>  $procedureToChange <===================="


        propertyToChange != null
        propertyToDelete != null
        //propertyToChange.value = "property value propertyToChange"
        //println "====================>  $propertyToChange <===================="


        when: "Set metadata property to the first stage git commit ID"
        def metadataValue = """{"id":"$commitID1"}"""
        def propSheetCheck = dsl """
        getProperties(projectName: "/plugins/$pName/project").property.any { prop ->
            prop.propertyName == "$dataTrackerProperty"
        }
"""
        if (propSheetCheck.value == "false") {
            def tmp1 = dsl """
createProperty(projectName: "/plugins/$pName/project", propertyName: "$dataTrackerProperty", propertyType: "sheet", expandable: "1")
"""
        }
        dsl """
setProperty(propertyName: "/plugins/$pName/project/properties/$metadataProperty", value: '$metadataValue')
"""
        then:
        def metadata = dsl """getProperty(propertyName: '/plugins/$pName/project/properties/$metadataProperty')"""
        assert metadata.property.value == """$metadataValue"""

        when: "run incremental import"
        def runProc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "importDslFromGitNew",
          actualParameter: [
            config: "/projects/$projectName/ec_plugin_cfgs/$pluginConfigurationName",
            dest: "$branch",
            incrementalImport: "1",
            branch: "$branch",
            repoUrl: "$repoURL",
            rsrcName: "local"
          ]
        )""")
        then: "job succeeds"
        assert runProc.jobId
        assert getJobProperty("outcome", runProc.jobId) != "error"

        when: "Old and new objects are retrieved"
        userAdded = dsl """getUser(userName: '$userAddedName')"""
        userToChange = dsl """getUser(userName: '$userToChangeName')"""
        //userToDelete = dsl """getUser(userName: '$userToDeleteName')"""
        procedureAdded = dsl """getProcedure(projectName:'$projectName', procedureName:'$procedureAddedName')"""
        procedureToChange = dsl """getProcedure(projectName:'$projectName', procedureName:'$procedureToChangeName')"""
        //procedureToDelete = dsl """getProcedure(projectName:'$projectName', procedureName:'$procedureToDeleteName')"""
        propertyAdded = dsl """getProperty(projectName:'$projectName', propertyName:'$propertyAddedName')"""
        propertyToChange = dsl """getProperty(projectName:'$projectName', propertyName:'$propertyToChangeName')"""
        //propertyToDelete = dsl """getProperty(projectName:'$projectName', propertyName:'$propertyToDeleteName')"""

        then: "New objects and values are available"
        assert userAdded != null
        assert procedureAdded != null
        assert propertyAdded != null

        assert userToChange.user.fullUserName == "This full user name was changed"
        assert procedureToChange.procedure.description == "This procedure description changed"
        assert propertyToChange.property.value == "This property value was changed"

        when:"Search for deleted objects"
        def procedureDeletedCheck = dsl """
        getProcedures(projectName: "$projectName").procedures.any { proc ->
            (proc != null) && (proc.procedureName == "$procedureToDeleteName")
        }
        """
        def userDeletedCheck = dsl """getUsers().users.any { user -> user.userName == "$userToDeleteName"}"""

        then: "No deleted objects are found"
        assert procedureDeletedCheck.value as String == "false"
        assert userDeletedCheck.value as String == "false"

        cleanup: "Remove objects"
        dsl """deleteProject(projectName: "$projectName") """
        dsl """deleteUser (userName: '$userAddedName')"""
        dsl """deleteUser (userName: '$userToChangeName')"""
        dsl """deleteUser (userName: '$userToDeleteName')"""
    }

    def "Incremental Import: Users, Project, Procedure, Properties and SAME commit IDs"() {
        given: "Users, one Project, Procedures, Properties"
        def testProject = dsl """
project '$projectName', {
    procedure '$procedureToChangeName', {
      description = 'procedure description $procedureToChangeName'
    }
    procedure '$procedureToDeleteName', {
      description = 'procedure description $procedureToDeleteName'
    }
    property '$propertyToChangeName', value: 'property value $propertyToChangeName', {expandable = '0'}
    property '$propertyToDeleteName', value: 'property value $propertyToDeleteName', {expandable = '0'}

    pluginConfiguration '$pluginConfigurationName', {
        field = [
          'authType': 'token',
          'debugLevel': '0',
          'library': 'jgit',
          'token_credential': 'token_credential',
        ]
        pluginKey = 'EC-Git'

        addCredential 'token_credential', {
          passwordRecoveryAllowed = '1'
          password='$GIT_TOKEN_CLOUDBEES_CD_PLUGINS_BOT'
        }
    }
}
"""
        dsl """createUser(userName:'$userToChangeName', fullUserName: 'full user name - $userToChangeName')"""
        dsl """createUser(userName:'$userToDeleteName', fullUserName: 'full user name - $userToDeleteName')"""

        def pluginConfiguration = dsl """getPluginConfiguration(projectName: '$projectName', pluginConfigurationName:'$pluginConfigurationName')"""

        testProject != null
        pluginConfiguration != null

        when: "Set metadata property to the second stage git commit ID"
        def metadataValue = """{"id":"$commitID2"}"""
        def propSheetCheck = dsl """
        getProperties(projectName: "/plugins/$pName/project").property.any { prop ->
            prop.propertyName == "$dataTrackerProperty"
        }
"""
        if (propSheetCheck.value == "false") {
            def tmp1 = dsl """
createProperty(projectName: "/plugins/$pName/project", propertyName: "$dataTrackerProperty", propertyType: "sheet", expandable: "1")
"""
        }
        dsl """
setProperty(propertyName: "/plugins/$pName/project/properties/$metadataProperty", value: '$metadataValue')
"""
        then:
        def metadata = dsl """getProperty(propertyName: '/plugins/$pName/project/properties/$metadataProperty')"""
        assert metadata.property.value == """$metadataValue"""

        when: "run incremental import"
        def runProc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "importDslFromGitNew",
          actualParameter: [
            config: "/projects/$projectName/ec_plugin_cfgs/$pluginConfigurationName",
            dest: "$branch",
            incrementalImport: "1",
            branch: "$branch",
            repoUrl: "$repoURL",
            rsrcName: "local"
          ]
        )""")
        then: "job succeeds"
        assert runProc.jobId
        assert getJobProperty("outcome", runProc.jobId) == "success"
        and: "The change list is 'empty' as it should be if the commit IDs were the same"
        assert getJobProperty("change_list.json", runProc.jobId) == """{"what":"INCREMENTAL","changed":[],"added":[],"deleted":[],"renamed":{}}"""
        cleanup: "Remove objects"
        dsl """deleteProject(projectName: "$projectName") """
        dsl """deleteUser (userName: '$userAddedName')"""
        dsl """deleteUser (userName: '$userToChangeName')"""
        dsl """deleteUser (userName: '$userToDeleteName')"""
    }
}
