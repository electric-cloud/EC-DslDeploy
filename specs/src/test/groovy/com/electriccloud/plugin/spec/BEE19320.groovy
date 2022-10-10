package com.electriccloud.plugin.spec

import spock.lang.Shared

class BEE19320 extends PluginTestHelper
{

    static String pName = 'EC-DslDeploy'
    static String jira = "BEE-19320"

    @Shared String pVersion
    @Shared String plugDir
    @Shared String dslDir

    def args = [sessionPassword: password, projectName: jira]

    def doSetupSpec()
    {
        dsl """ deleteProject(projectName: "$jira") """
        dslFile "generate_dsl_test_procedure.dsl"
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
    }

    def doCleanupSpec()
    {
        conditionallyDeleteProject("$jira")
    }

    def "generate DSL for project entity with single quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def projName = randomize("'test'project'")
        args << [projectName: projName]
        def escapedProjName = projName.replace("'", "\\\'").replace("\"", "\\\"")
        def artName = "art:" + randomize('art_name')

        given: 'create project with single quotes in the name'
        dsl """ createProject(projectName: "$projName") """

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'project',
            objectName: "$projName",
            includeAllChildren: '1',
            artifactName: "$artName",
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when: "retrieve artifact"
        retrieveArtifactVersion("$artName", "1.0", dslDir)
        then:
        File projDir = new File (dslDir, "projects/" + projName)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.dsl'), """
project '$escapedProjName', {
  tracked = '1'
}
""")

        cleanup:
        dsl """ deleteProject(projectName: "$projName") """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project entity and nested children entities with single quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def projName = randomize("'test'project'")
        args << [projectName: projName]
        def escapedProjName = projName.replace("'", "\\\'").replace("\"", "\\\"")
        def artName = "art:" + randomize('art_name')

        given: 'create project with single quotes in the name'
        dsl """ createProject(projectName: "$projName") """
        dsl """ createProcedure(projectName: "$projName", procedureName: "$projName") """
        dsl """ createStep(projectName: "$projName", procedureName: "$projName", stepName: "$projName") """
        dsl """ createFormalParameter(projectName: "$projName", procedureName: "$projName", formalParameterName: "$projName") """

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'project',
            objectName: "$projName",
            includeAllChildren: '1',
            artifactName: "$artName",
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when: "retrieve artifact"
        retrieveArtifactVersion("$artName", "1.0", dslDir)
        then:
        File projDir = new File (dslDir, "projects/" + projName)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.dsl'), """
project '$escapedProjName', {
  tracked = '1'
}
""")

        assert new File(projDir, "procedures").exists()

        // check procedure
        File procedureDir = new File (projDir, "procedures/" + projName)
        assert procedureDir.exists()

        assert new File(procedureDir, "steps").exists()

        // check step
        File stepDir =  new File(procedureDir, "steps/" + projName)
        assert stepDir.exists()

        cleanup:
        dsl """ deleteProject(projectName: "$projName") """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for user entity with single quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def userName = randomize("'test'user'")
        def escapedUserName = userName.replace("'", "\\\'").replace("\"", "\\\"")
        def artName = "art:" + randomize('art_name')

        given: 'create user with single quotes in the name'
        dsl """ createUser(userName: "$userName") """

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'user',
            objectName: "$userName",
            includeAllChildren: '1',
            artifactName: "$artName",
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when: "retrieve artifact"
        retrieveArtifactVersion("$artName", "1.0", dslDir)
        then:
        File userDir = new File (dslDir, "users/" + userName)
        assert userDir.exists()

        assertFile(new File(userDir, 'user.dsl'), """
user '$escapedUserName'
""")

        cleanup:
        dsl """ deleteUser(userName: "$userName") """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project with double quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def projName = randomize('"test"project"')
        args << [projectName: projName]
        def artName = "art:" + randomize('art_name')

        given: 'create project with double quotes in the name'
        dsl """ createProject(projectName: '$projName') """

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'project',
            objectName: '$projName',
            includeAllChildren: '1',
            artifactName: "$artName",
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when: "retrieve artifact"
        retrieveArtifactVersion("$artName", "1.0", dslDir)
        then:
        File projDir = new File (dslDir, "projects/" + encode(projName))
        assert projDir.exists()

        assertFile(new File(projDir, 'project.dsl'), """
project '$projName', {
  tracked = '1'
}
""")

        cleanup:
        dsl """ deleteProject(projectName: '$projName') """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project entity and nested children entities with double quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def projName = randomize('"test"project"')
        args << [projectName: projName]
        def artName = "art:" + randomize('art_name')

        given: 'create project with single quotes in the name'
        dsl """ createProject(projectName: '$projName') """
        dsl """ createProcedure(projectName: '$projName', procedureName: '$projName') """
        dsl """ createStep(projectName: '$projName', procedureName: '$projName', stepName: '$projName') """
        dsl """ createFormalParameter(projectName: '$projName', procedureName: '$projName', formalParameterName: '$projName') """

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'project',
            objectName: '$projName',
            includeAllChildren: '1',
            artifactName: "$artName",
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when: "retrieve artifact"
        retrieveArtifactVersion("$artName", "1.0", dslDir)
        then:
        File projDir = new File (dslDir, "projects/" + encode(projName))
        assert projDir.exists()

        assertFile(new File(projDir, 'project.dsl'), """
project '$projName', {
  tracked = '1'
}
""")

        assert new File(projDir, "procedures").exists()

        // check procedure
        File procedureDir = new File (projDir, "procedures/" + encode(projName))
        assert procedureDir.exists()

        assert new File(procedureDir, "steps").exists()

        // check step
        File stepDir =  new File(procedureDir, "steps/" + encode(projName))
        assert stepDir.exists()

        cleanup:
        dsl """ deleteProject(projectName: '$projName') """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for user entity with double quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def userName = randomize('"test"user"')
        def artName = "art:" + randomize('art_name')

        given: 'create user with single quotes in the name'
        dsl """ createUser(userName: '$userName') """

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'user',
            objectName: '$userName',
            includeAllChildren: '1',
            artifactName: "$artName",
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when: "retrieve artifact"
        retrieveArtifactVersion("$artName", "1.0", dslDir)
        then:
        File userDir = new File (dslDir, "users/" + encode(userName))
        assert userDir.exists()

        assertFile(new File(userDir, 'user.dsl'), """
user '$userName'
""")

        cleanup:
        dsl """ deleteUser(userName: '$userName') """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    // Check import
    def "installDslFromDirectory double quotes"() {
        given: "the overwrite_installDslFromDirectory code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/quotes/double",
            pool: 'local'
          ]
        )""")
        then: "job completes with success"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        cleanup:
        dsl """ deleteProject(projectName: '"test"') """
    }

    def "installDslFromDirectory single quotes"() {
        given: "the overwrite_installDslFromDirectory code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/quotes/single",
            pool: 'local'
          ]
        )""")
        then: "job completes with success"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        cleanup:
        dsl """ deleteProject(projectName: "'test'") """
    }

    private static String encode(String arg)
    {
        Map<String, String> ENCODE_MAP = [
                "/": "%2F", "\\": "%5C", ":": "%3A", "*": "%2A", "?": "%3F", "\"": "%22",
                "<": "%3C", ">": "%3E", "|": "%7C"
        ] as HashMap
        String result = arg
        ENCODE_MAP.each {key, value ->
            result = result.replace(key, value)
        }
        return result
    }

    private void assertFile(File file, String content) {
        assert file.exists()
        assert content.equals(file.text.replace("\r\n", "\n"))
    }

    private void assertAcl(File file, String content) {
        File aclFolder = new File (file, "acls")
        assert aclFolder

        File aclFile = aclFolder.listFiles().find()
        assert aclFile && aclFile.isFile() && aclFile.name == "acl.dsl"

        assertFile(aclFile, content)
    }
}
