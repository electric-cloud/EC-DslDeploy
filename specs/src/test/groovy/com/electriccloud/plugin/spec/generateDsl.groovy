package com.electriccloud.plugin.spec

import spock.lang.Ignore
import spock.lang.Shared

class generateDsl extends PluginTestHelper {
    static String pName='EC-DslDeploy'
    static String jira="CEV-19608"
    @Shared String pVersion
    @Shared String plugDir
    @Shared String dslDir

    def args = [sessionPassword: password, projectName: jira]

    def doSetupSpec() {
        dsl """ deleteProject(projectName: "$jira") """
        dslFile "generate_dsl_test_procedure.dsl"
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")


    }

    def doCleanupSpec() {
        conditionallyDeleteProject("$jira")
    }

    /**
     * includeChildrenInSameFile = true
     */
    def "generate DSL for project with procedures with includeAllChildren=true, childrenInSameFile=true"() {
        dslDir='build/dsl1'
        given: dslFile("project_with_procedure.dsl", args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "dslCode1",
            objectType: 'project',
            objectName: "$jira",
            includeAllChildren: '1',
            includeChildrenInSameFile: '1',
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode1',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode1", "1.0", dslDir)
        then:
        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        File projectFile = new File (projDir, 'project.groovy')
        assert projectFile.exists()
        assert """
project 'CEV-19608', {

  procedure 'proc1', {
    resourceName = 'test'

    step 's1', {
      command = 'echo test'
    }

    step 's2', {
      command = 'println "test";'
      shell = 'ec-perl'
    }

    step 's3', {
      command = 'print \\'test\\''
      shell = 'ec-groovy'
    }
  }

  procedure 'proc2', {

    step 's1', {
      command = 'exit 1'
    }
  }

  // Custom properties
  testProperty = 'test'
}
""".equals(projectFile.text.replace("\r\n", "\n"))

        assert !new File(projDir, "procedures").exists()
        assert !new File(projDir, "properties").exists()


        cleanup:
        deleteProjects([projectName: jira], false)
        dsl 'deleteArtifact(artifactName: "dsl:dslCode1")'
        new File(dslDir).deleteDir()
    }

    /**
     * check empty childrenInDifferentFile and default includeChildrenInSameFile (false)
     */
    def "generate DSL for project with procedures with includeAllChildren=true, all children in diff files"() {
        dslDir='build/dsl2'
        given: dslFile("project_with_procedure.dsl", args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "dslCode2",
            objectType: 'project',
            objectName: "$jira",
            includeAllChildren: '1',
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode2',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode2", "1.0", dslDir)
        then:
        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.groovy'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()

        assertFile(new File(proc1, "procedure.groovy"), """
procedure 'proc1', {
  resourceName = 'test'
}
""")
        assert new File(proc1, "steps").exists()
        assertFile(new File(proc1, "steps/s1.cmd"), "echo test")
        assertFile(new File(proc1, "steps/s2.pl"), 'println "test";')
        assertFile(new File(proc1, "steps/s3.groovy"), "print 'test'")

        //
        File s1Dir = new File(proc1, "steps/s1")
        assert s1Dir.exists()

        assertFile(new File(s1Dir, "step.groovy"),
                """import java.io.File


step 's1', {
  command = new File(projectDir, "./procedures/proc1/steps/s1.cmd").text
}
""")

        //
        File s2Dir = new File(proc1, "steps/s2")
        assert s2Dir.exists()
        assertFile(new File(s2Dir, "step.groovy"),
                """import java.io.File


step 's2', {
  command = new File(projectDir, "./procedures/proc1/steps/s2.pl").text
  shell = 'ec-perl'
}
""")
        //
        File s3Dir = new File(proc1, "steps/s3")
        assert s3Dir.exists()
        assertFile(new File(s3Dir, "step.groovy"),
                """import java.io.File


step 's3', {
  command = new File(projectDir, "./procedures/proc1/steps/s3.groovy").text
  shell = 'ec-groovy'
}
""")

        // start proc2 procedure
        File proc2 = new File (projDir, "procedures/proc2")
        assert proc2.exists()

        assertFile(new File(proc2, "procedure.groovy"), """
procedure 'proc2'
""")
        assert new File(proc2, "steps").exists()
        assertFile(new File(proc2, "steps/s1.cmd"), "exit 1")

        //
        File p2s1Dir = new File(proc2, "steps/s1")
        assert p2s1Dir.exists()

        assertFile(new File(p2s1Dir, "step.groovy"),
                """import java.io.File


step 's1', {
  command = new File(projectDir, "./procedures/proc2/steps/s1.cmd").text
}
""")

        // properties
        assert new File(projDir, "properties").exists()

        assertFile(new File(projDir, "./properties/testProperty.txt"), "test")


        cleanup:
        deleteProjects([projectName: jira], false)
        dsl 'deleteArtifact(artifactName: "dsl:dslCode2")'
        new File(dslDir).deleteDir()
    }

    /**
     * use childrenInDifferentFile='procedures.*'
     */
    def "generate DSL for project with procedures with includeAllChildren=true, all children in diff files 2"() {
        dslDir = 'build/dsl3'
        given: dslFile("project_with_procedure.dsl", args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "dslCode3",
            objectType: 'project',
            objectName: "$jira",
            includeAllChildren: '1',
            childrenInDifferentFile:'procedures.*',
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode3',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")

        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode3", "1.0", dslDir)
        then:
        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.groovy'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()

        assertFile(new File(proc1, "procedure.groovy"), """
procedure 'proc1', {
  resourceName = 'test'
}
""")
        assert new File(proc1, "steps").exists()
        assertFile(new File(proc1, "steps/s1.cmd"), "echo test")
        assertFile(new File(proc1, "steps/s2.pl"), 'println "test";')
        assertFile(new File(proc1, "steps/s3.groovy"), "print 'test'")

        //
        assert new File(proc1, "steps/s1").exists()
        assert new File(proc1, "steps/s1/step.groovy").exists()

        //
        assert new File(proc1, "steps/s2").exists()
        assert new File(proc1, "steps/s2/step.groovy").exists()

        //
        assert new File(proc1, "steps/s3").exists()
        assert new File(proc1, "steps/s3/step.groovy").exists()


        // start proc2 procedure
        File proc2 = new File (projDir, "procedures/proc2")
        assert proc2.exists()

        assertFile(new File(proc2, "procedure.groovy"), """
procedure 'proc2'
""")
        assert new File(proc2, "steps").exists()
        assertFile(new File(proc2, "steps/s1.cmd"), "exit 1")

        //
        assert new File(proc2, "steps/s1").exists()
        assert new File(proc2, "steps/s1/step.groovy").exists()

        // properties
        assert new File(projDir, "properties").exists()
        assertFile(new File(projDir, "./properties/testProperty.txt"), "test")


        cleanup:
        deleteProjects([projectName: jira], false)
        dsl 'deleteArtifact(artifactName: "dsl:dslCode3")'
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project with procedures with includeAllChildren=true, procedures in diff files"() {
        dslDir = 'build/dsl4'
        given: dslFile("project_with_procedure.dsl", args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "dslCode4",
            objectType: 'project',
            objectName: "$jira",
            includeAllChildren: '1',
            childrenInDifferentFile: 'procedures',
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode4',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
         
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode4", "1.0", dslDir)
        then:
        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.groovy'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()

        assertFile(new File(proc1, "procedure.groovy"),
                """import java.io.File


procedure 'proc1', {
  resourceName = 'test'

  step 's1', {
    command = new File(projectDir, "./procedures/proc1/steps/s1.cmd").text
  }

  step 's2', {
    command = new File(projectDir, "./procedures/proc1/steps/s2.pl").text
    shell = 'ec-perl'
  }

  step 's3', {
    command = new File(projectDir, "./procedures/proc1/steps/s3.groovy").text
    shell = 'ec-groovy'
  }
}
""")
        assert new File(proc1, "steps").exists()
        assertFile(new File(proc1, "steps/s1.cmd"), "echo test")
        assertFile(new File(proc1, "steps/s2.pl"), 'println "test";')
        assertFile(new File(proc1, "steps/s3.groovy"), "print 'test'")

        //
        assert !new File(proc1, "steps/s1").exists()
        assert !new File(proc1, "steps/s2").exists()
        assert !new File(proc1, "steps/s3").exists()


        // start proc2 procedure
        File proc2 = new File (projDir, "procedures/proc2")
        assert proc2.exists()

        assertFile(new File(proc2, "procedure.groovy"),
                """import java.io.File


procedure 'proc2', {

  step 's1', {
    command = new File(projectDir, "./procedures/proc2/steps/s1.cmd").text
  }
}
""")
        assert new File(proc2, "steps").exists()
        assertFile(new File(proc2, "steps/s1.cmd"), "exit 1")

        //
        assert !new File(proc2, "steps/s1").exists()

        // properties
        assert new File(projDir, "properties").exists()
        assertFile(new File(projDir, "./properties/testProperty.txt"), "test")


        cleanup:
        deleteProjects([projectName: jira], false)
        dsl 'deleteArtifact(artifactName: "dsl:dslCode4")'
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project with procedures - no read privilege on proc2"() {
        dslDir = 'build/dsl5'
        given: dslFile("project_with_procedure.dsl", args)
        dsl """createAclEntry (projectName: '$pName-$pVersion',
                          principalType: 'user', principalName: 'limited_user',
                          readPrivilege: 'allow', modifyPrivilege: 'allow', executePrivilege: 'allow')"""

        when: 'run generate Dsl procedure'
        assertLogin("limited_user", "changeme")
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "dslDir5",
            objectType: 'project',
            objectName: "$jira",
            includeAllChildren: '1',
            childrenInDifferentFile: 'procedures',
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode5',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode5", "1.0", dslDir)
        then:

        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.groovy'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()
        assert new File(proc1, "procedure.groovy").exists()


        // proc2 procedure
        assert !new File (projDir, "procedures/proc2").exists()


        // properties
        assert new File(projDir, "properties").exists()
        assertFile(new File(projDir, "./properties/testProperty.txt"), "test")


        cleanup:
        assertLogin("admin", password)
        deleteProjects([projectName: jira], false)
        dsl """deleteAclEntry (projectName: '$pName-$pVersion',
                          principalType: 'user', principalName: 'limited_user')"""
        dsl 'deleteArtifact(artifactName: "dsl:dslCode5")'
        new File(dslDir).deleteDir()
    }


    /**
     * includeChildren is not provided, includeAllChildren = false
     * result: dsl for project only + project properties in file/directory structure
     * @return
     */
    def "generate DSL for project with procedure, application and pipeline - includeChildren is empty"() {
        dslDir = 'build/dsl6'
        given: dslFile("project_with_app_procedure_pipeline.dsl", args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'project',
            objectName: "$jira",
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode6',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"
        //
        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode6", "1.0", dslDir)
        then:

        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.groovy'), """
project 'CEV-19608'
""")
        assert !new File(projDir, "procedures").exists()
        assert !new File(projDir, "pipelines").exists()
        assert !new File(projDir, "applications").exists()

        // properties
        assert new File(projDir, "properties").exists()
        assert new File(projDir, "properties/testSheet").exists()
        assertFile(new File(projDir, "./properties/testSheet/testProperty.txt"), "test project")


        cleanup:
        deleteProjects([projectName: jira], false)
        dsl 'deleteArtifact(artifactName: "dsl:dslCode6")'
        new File(dslDir).deleteDir()
    }


    /**
     * includeChildren='applications, pipelines'
     * result: application and pipelines, and all nested objects in different files. procedure is absent
     * @return
     */
    def "generate DSL for project with procedure, application and pipeline - include applications and pipelines"() {
        dslDir = 'build/dsl7'
        given: dslFile("project_with_app_procedure_pipeline.dsl", args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "dslDir7",
            objectType: 'project',
            objectName: "$jira",
            suppressDefaults: '1',
            suppressParent: '1',
            includeChildren: 'applications, pipelines',
            artifactName: 'dsl:dslCode7',
            artifactVersionVersion: '1.0',
            runResourceName: '$defaultPool'
          ]
        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        // retrieve artifact
        retrieveArtifactVersion("dsl:dslCode7", "1.0", dslDir)
        then:
        //
        File projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.groovy'), """
project 'CEV-19608'
""")
        assert !new File(projDir, "procedures").exists()
        assert new File(projDir, "pipelines").exists()

        // check pipeline1
        File pipeDir = new File (projDir, "pipelines/pipeline1")
        assert pipeDir.exists()
        assertFile(new File (pipeDir, "pipeline.groovy"), """
pipeline 'pipeline1', {

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }
}
""")
        File stagesDir =  new File(pipeDir, "stages")
        assert stagesDir.exists()

        //metadata file
        assertFile(new File(stagesDir, 'metadata.json'), '{"order":["stage1"]}')

        //
        File stageDir = new File(pipeDir, "stages/stage1")
        assert stageDir.exists()

        //
        assertFile(new File(stageDir, "stage.groovy"), """
stage 'stage1'
""")
        assert new File(stageDir, "gates").exists()
        File tasksDir = new File(stageDir, "tasks")
        assert tasksDir.exists()
        //metadata file
        assertFile(new File(tasksDir, 'metadata.json'), '{"order":["cmd"]}')
        //
        assertFile(new File(stageDir, "tasks/cmd.cmd"), "echo test")

        File taskDir = new File (stageDir, "tasks/cmd")
        assert taskDir.exists()
        assertFile(new File(taskDir, "task.groovy"), """import java.io.File


task 'cmd', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/pipeline1/stages/stage1/tasks/cmd.cmd").text,
  ]
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
}
""")
        assert new File(taskDir, "properties").exists()
        assertFile(new File(taskDir, "properties/task_testProperty.txt"), "test task")
        //
        assert new File(stageDir, "properties").exists()
        assertFile(new File(stageDir, "properties/stage_testProperty.txt"), "test stage")
        //
        assert new File(pipeDir, "properties").exists()
        assertFile(new File(pipeDir, "properties/pipeline_testProperty.txt"), "test pipeline")

        // check application
        assert new File(projDir, "applications").exists()

        File appDir = new File(projDir, "applications/app1")
        assert appDir.exists()
        assertFile(new File(appDir, "application.groovy"), """
application 'app1'
""")
        assert new File(appDir, "applicationTiers").exists()

        // applicationTier
        File appTierDir = new File(appDir, "applicationTiers/Tier 1")
        assert appTierDir.exists()
        assertFile(new File(appTierDir, "applicationTier.groovy"), """
applicationTier 'Tier 1'
""")
        assert new File(appTierDir, "components").exists()
        // component
        File compDir = new File(appTierDir, "components/component1")
        assert compDir.exists()
        assertFile(new File(compDir, "component.groovy"), """
component 'component1', {
  pluginKey = 'EC-Artifact'
}
""")
        assert new File(compDir, "properties").exists()
        assert new File(compDir, "processes").exists()

        // process
        File processDir = new File(compDir, "processes/component_process")
        assert processDir.exists()
        File compProcessFile = new File (processDir, "process.groovy")
        assert compProcessFile.exists()
        String compProcessDsl = compProcessFile.text
        assert compProcessDsl.contains("import java.io.File")
        assert compProcessDsl.contains("new File(projectDir")

        assertFile(new File (processDir, "processSteps/cmd.cmd"), "echo 'component process step'")
        //no processStep directory
        assert !new File(processDir, "processSteps/cmd").exists()

        // check application  process
        assert new File(appDir, "processes").exists()
        File appProcDir = new File(appDir, "processes/app_process")
        assert appProcDir.exists()
        File appProcFile = new File(appProcDir, "process.groovy")
        assert appProcFile.exists()
        String appProcDsl = appProcFile.text
        assert appProcDsl.contains("import java.io.File")
        assert appProcDsl.contains("new File(projectDir")

        assertFile(new File (appProcDir, "processSteps/cmd.groovy"), "print 'application process step'")
        // no process step dir
        assert !new File(appProcDir, "processSteps/cmd").exists()

        // application properties
        assert new File(appDir, "properties")

        // project properties
        assert new File(projDir, "properties").exists()
        assert new File(projDir, "properties/testSheet").exists()
        assertFile(new File(projDir, "./properties/testSheet/testProperty.txt"), "test project")


        cleanup:
        deleteProjects([projectName: jira], false)
        dsl 'deleteArtifact(artifactName: "dsl:dslCode7")'
        new File(dslDir).deleteDir()
    }

    private void assertFile(File file, String content) {
        assert file.exists()
        assert content.equals(file.text.replace("\r\n", "\n"))
    }

}
