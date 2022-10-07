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
        def projName = randomize('\'test\'project\'')
        args << [projectName: projName]
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
project '$projName', {
  tracked = '1'
}
""")

        cleanup:
        deleteProjects([projectName: projName], false)
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project entity and nested children entities with single quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def projName = randomize('\'test\'project\'')
        args << [projectName: projName]
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
project '$projName', {
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

        assert new File(procedureDir, "formalParameters").exists()

        // check formal parameter
        File formalParamDir = new File(procedureDir, "formalParameters/" + projName)
        assert formalParamDir.exists()

        cleanup:
        deleteProjects([projectName: projName], false)
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for user entity with single quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def userName = randomize('\'test\'user\'')
        def artName = "art:" + randomize('art_name')

        given: 'create project with single quotes in the name'
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
project '$userName', {
  email = null
  fullUserName = null
}
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
project '$projName', {
  tracked = '1'
}
""")

        cleanup:
        deleteProjects([projectName: projName], false)
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
project '$projName', {
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

        assert new File(procedureDir, "formalParameters").exists()

        // check formal parameter
        File formalParamDir = new File(procedureDir, "formalParameters/" + projName)
        assert formalParamDir.exists()

        cleanup:
        deleteProjects([projectName: projName], false)
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    def "generate DSL for user entity with double quotes in the name"()
    {

        dslDir = 'build/' + randomize('dsl_dir')
        def userName = randomize('"test"user"')
        def artName = "art:" + randomize('art_name')

        given: 'create project with single quotes in the name'
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
project '$userName', {
  email = null
  fullUserName = null
}
""")

        cleanup:
        dsl """ deleteUser(userName: "$userName") """
        dsl """ deleteArtifact(artifactName: "$artName") """
        new File(dslDir).deleteDir()
    }

    // Check import
    def "installDslFromDirectory quotes"() {
        given: "the overwrite_installDslFromDirectory code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/quotes",
            pool: 'local'
          ]
        )""")
        then: "job completes with success"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        cleanup:
        deleteProjects([projectName: "\"'test\"'"], false)
    }
}
