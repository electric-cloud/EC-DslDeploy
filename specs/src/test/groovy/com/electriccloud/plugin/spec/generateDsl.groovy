package com.electriccloud.plugin.spec

import groovy.json.StringEscapeUtils
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

        File projectFile = new File (projDir, 'project.dsl')
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

        assertFile(new File(projDir, 'project.dsl'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()

        assertFile(new File(proc1, "procedure.dsl"), """
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

        assertFile(new File(s1Dir, "step.dsl"),
                """import java.io.File


step 's1', {
  command = new File(projectDir, "./procedures/proc1/steps/s1.cmd").text
}
""")

        //
        File s2Dir = new File(proc1, "steps/s2")
        assert s2Dir.exists()
        assertFile(new File(s2Dir, "step.dsl"),
                """import java.io.File


step 's2', {
  command = new File(projectDir, "./procedures/proc1/steps/s2.pl").text
  shell = 'ec-perl'
}
""")
        //
        File s3Dir = new File(proc1, "steps/s3")
        assert s3Dir.exists()
        assertFile(new File(s3Dir, "step.dsl"),
                """import java.io.File


step 's3', {
  command = new File(projectDir, "./procedures/proc1/steps/s3.groovy").text
  shell = 'ec-groovy'
}
""")

        // start proc2 procedure
        File proc2 = new File (projDir, "procedures/proc2")
        assert proc2.exists()

        assertFile(new File(proc2, "procedure.dsl"), """
procedure 'proc2'
""")
        assert new File(proc2, "steps").exists()
        assertFile(new File(proc2, "steps/s1.cmd"), "exit 1")

        //
        File p2s1Dir = new File(proc2, "steps/s1")
        assert p2s1Dir.exists()

        assertFile(new File(p2s1Dir, "step.dsl"),
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

        assertFile(new File(projDir, 'project.dsl'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()

        assertFile(new File(proc1, "procedure.dsl"), """
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
        assert new File(proc1, "steps/s1/step.dsl").exists()

        //
        assert new File(proc1, "steps/s2").exists()
        assert new File(proc1, "steps/s2/step.dsl").exists()

        //
        assert new File(proc1, "steps/s3").exists()
        assert new File(proc1, "steps/s3/step.dsl").exists()


        // start proc2 procedure
        File proc2 = new File (projDir, "procedures/proc2")
        assert proc2.exists()

        assertFile(new File(proc2, "procedure.dsl"), """
procedure 'proc2'
""")
        assert new File(proc2, "steps").exists()
        assertFile(new File(proc2, "steps/s1.cmd"), "exit 1")

        //
        assert new File(proc2, "steps/s1").exists()
        assert new File(proc2, "steps/s1/step.dsl").exists()

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

        assertFile(new File(projDir, 'project.dsl'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()

        assertFile(new File(proc1, "procedure.dsl"),
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

        assertFile(new File(proc2, "procedure.dsl"),
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

        assertFile(new File(projDir, 'project.dsl'), """
project 'CEV-19608'
""")
        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()
        assert new File(proc1, "procedure.dsl").exists()


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

        assertFile(new File(projDir, 'project.dsl'), """
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

        assertFile(new File(projDir, 'project.dsl'), """
project 'CEV-19608'
""")
        assert !new File(projDir, "procedures").exists()
        assert new File(projDir, "pipelines").exists()

        // check pipeline1
        File pipeDir = new File (projDir, "pipelines/pipeline1")
        assert pipeDir.exists()
        assertFile(new File (pipeDir, "pipeline.dsl"), """
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
        assertFile(new File(stageDir, "stage.dsl"), """
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
        assertFile(new File(taskDir, "task.dsl"), """import java.io.File


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
        assertFile(new File(appDir, "application.dsl"), """
application 'app1'
""")
        assert new File(appDir, "applicationTiers").exists()

        // applicationTier
        File appTierDir = new File(appDir, "applicationTiers/Tier 1")
        assert appTierDir.exists()
        assertFile(new File(appTierDir, "applicationTier.dsl"), """
applicationTier 'Tier 1'
""")
        assert new File(appTierDir, "components").exists()
        // component
        File compDir = new File(appTierDir, "components/component1")
        assert compDir.exists()
        assertFile(new File(compDir, "component.dsl"), """
component 'component1', {
  pluginKey = 'EC-Artifact'
}
""")
        assert new File(compDir, "properties").exists()
        assert new File(compDir, "processes").exists()

        // process
        File processDir = new File(compDir, "processes/component_process")
        assert processDir.exists()
        File compProcessFile = new File (processDir, "process.dsl")
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
        File appProcFile = new File(appProcDir, "process.dsl")
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

    def "generate DSL for project with procedures with ACL in different file"() {
        dslDir = 'build/dsl8'
        File projDir
        File appDir

        def aclContent = '''
acl {
  inheriting = '1'

  aclEntry 'user', principalName: 'limited_user', {
    changePermissionsPrivilege = 'inherit'
    executePrivilege = 'allow'
    modifyPrivilege = 'allow'
    readPrivilege = 'allow'
  }
}
'''

        given: dslFile("project_with_app_procedure_pipeline.dsl", args)

        dsl """createAclEntry (projectName: '$pName-$pVersion',
                          principalType: 'user', principalName: 'limited_user',
                          readPrivilege: 'allow', modifyPrivilege: 'allow', executePrivilege: 'allow')"""

        dsl """createAclEntry (projectName: '$jira',  procedureName: 'proc1',
                          principalType: 'user', principalName: 'limited_user',
                          readPrivilege: 'allow', modifyPrivilege: 'allow', executePrivilege: 'allow')"""

        dsl """createAclEntry (projectName: '$jira',  applicationName: 'app1',  processName: 'app_process',
                          principalType: 'user', principalName: 'limited_user',
                          readPrivilege: 'allow', modifyPrivilege: 'allow', executePrivilege: 'allow')"""

        dsl """breakAclInheritance (projectName: '$jira', applicationName: 'app1', applicationTierName: 'Tier 1')"""


        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
        runProcedure(
          projectName: "generateDslTestProject",
          procedureName: "generateDslAndPublish",
          actualParameter: [
            directory: "$dslDir",
            objectType: 'project',
            objectName: "$jira",
            includeAllChildren: '1',
            includeAcls: '1',
            includeAclsInDifferentFile: '1',
            suppressDefaults: '1',
            suppressParent: '1',
            artifactName: 'dsl:dslCode8',
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
        retrieveArtifactVersion("dsl:dslCode8", "1.0", dslDir)

        and: "check project and procedure ACL"

        //
        projDir = new File (dslDir, "projects/" + jira)
        assert projDir.exists()

        assertFile(new File(projDir, 'project.dsl'), """
project 'CEV-19608'
""")

        def aclContent1 = '''
acl {
  inheriting = '1'

  aclEntry 'user', principalName: 'project: CEV-19608', {
    changePermissionsPrivilege = 'allow'
    executePrivilege = 'allow'
    modifyPrivilege = 'allow'
    readPrivilege = 'allow'
  }
}
'''
        assertAcl(projDir, aclContent1)

        assert new File(projDir, "procedures").exists()

        // start proc1 procedure
        File proc1 = new File (projDir, "procedures/proc1")
        assert proc1.exists()
        assert new File(proc1, "procedure.dsl").exists()

        assertAcl(proc1, aclContent)

        and: "check application tier ACL"

        appDir = new File(projDir, "applications/app1")
        assert appDir.exists()
        assertFile(new File(appDir, "application.dsl"), """
application 'app1'
""")
        assert new File(appDir, "applicationTiers").exists()

        // applicationTier
        File appTierDir = new File(appDir, "applicationTiers/Tier 1")
        assert appTierDir.exists()

        def aclContent2 = '''
acl {
  inheriting = '0\'
}
'''
        assertAcl(appTierDir, aclContent2)

        then: "check application process ACL"

        assert new File(appDir, "processes").exists()
        File appProcDir = new File(appDir, "processes/app_process")
        assert appProcDir.exists()
        assertAcl(appProcDir, aclContent)

        cleanup:
        assertLogin("admin", password)
        deleteProjects([projectName: jira], false)
        dsl """deleteAclEntry (projectName: '$pName-$pVersion',
                          principalType: 'user', principalName: 'limited_user')"""
        dsl 'deleteArtifact(artifactName: "dsl:dslCode8")'
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project with AclOwner equal 0"() {
        dslDir = 'build/dsl-NMB-29220'
        def projName = 'NMB-29220'
        def appName = 'TestApp'
        def tierMapName = 'TierMap1'
        args << [projectName: projName, tierMapName: tierMapName, appName: appName]
        File projDir
        File appDir

        given:
        dslFile("NMB-29220.dsl", args)
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
                            includeAcls: '1',
                            includeAclsInDifferentFile: '1',
                            suppressDefaults: '1',
                            suppressParent: '1',
                            artifactName: 'dsl:NMB-29220',
                            artifactVersionVersion: '1.0',
                            runResourceName: '$defaultPool'
                          ]
                        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        retrieveArtifactVersion("dsl:NMB-29220", "1.0", dslDir)
        projDir = new File (dslDir, "projects/" + projName)

        and: "check project"
        assert projDir.exists()
        assertFile(new File(projDir, 'project.dsl'), "\nproject '$projName'\n")

        and:"check directories were created"
        appDir = new File(projDir, "applications/$appName")
        assert appDir.exists()
        assert new File(appDir, "acls").exists()
        assert new File(appDir, "tierMaps").exists()

        then: "check tierMap ACLs were NOT created"
        assert new File(appDir, "tierMaps/$tierMapName").exists()
        assert ! new File(appDir, "tierMaps/$tierMapName/acl").exists()

        cleanup:
        deleteProjects([projectName: projName], false)
        dsl("deleteArtifact(artifactName: 'dsl:NMB-29220')")
        new File(dslDir).deleteDir()
    }

    def "run procedure with EC-DslDeploy generateDslToDirectory procedure step"() {
        def projectName = randomize('test_project')
        def procedureName = randomize("test_procedure")

        def projects = [projectName: projectName]
        def procedures = [procedureName: procedureName]
        args << projects
        args << procedures

        def jobId
        def response

        given:
        dslFile ('ec_dslDeploy_procedure.dsl', args)

        when: "run procedure"
        response = dsl "runProcedure projectName: '$projectName', procedureName: '$procedureName'"

        then: "job gets created"
        waitUntil {
            assert response?.jobId
            jobId = response?.jobId
            assert jobId: 'jobId should be present'
        }

        and: "job should complete"
        jobCompleted jobId


        cleanup:
        deleteProjects(projects, false)
    }

    def "generate DSL for project with special symbols in names"() {
        dslDir = 'build/spec_symbols'
        def projName = 'new test Export'
        def procName = 'A Procedure % \\ @'
        def procStepName = 'step # 1'
        def pipelineName = 'Verify QA / Notify'
        def pipeStageName = 'Stage 1'
        def pipeTaskName = 'task %1'
        args << [projectName: projName,
                 procedureName: procName,
                 procStepName: procStepName,
                 pipelineName: pipelineName,
                 pipeStageName: pipeStageName,
                 pipeTaskName: pipeTaskName]
        def artifactName = 'dsl:spec_symbols'
        File projDir

        given:
        dslFile("spec_symbols.dsl", args)
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
                            includeAcls: '0',
                            includeAclsInDifferentFile: '0',
                            suppressDefaults: '1',
                            suppressParent: '1',
                            artifactName: "$artifactName",
                            artifactVersionVersion: '1.0',
                            runResourceName: '$defaultPool'
                          ]
                        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        retrieveArtifactVersion("$artifactName", "1.0", dslDir)
        projDir = new File (dslDir, "projects/" + encode(projName))

        and: "check project was created"
        assert projDir.exists()
        assertFile(new File(projDir, 'project.dsl'), "\nproject '$projName'\n")

        and:"check procedures directory were created"
        def encProcName = encode(procName)
        def procDir = new File(projDir, "procedures/"  + encProcName)
        assert procDir.exists()
        assertFile(new File(procDir, 'procedure.dsl'), "\nprocedure 'A Procedure % \\\\ @'\n")
        def procStepDir = new File(procDir, 'steps')
        assert procStepDir.exists()
        def encStepName = encode(procStepName)
        def stepDir = new File(procStepDir, encStepName)
        assert stepDir.exists()
        assertFile(new File(stepDir, 'step.dsl'),
            "import java.io.File\n\n\n"
                + "step '$procStepName', {\n"
                + "  command = new File(projectDir, \"./procedures/$encProcName/steps/$encStepName" + ".cmd\").text\n}\n")
        assertFile(new File(procStepDir, encode(procStepName) + '.cmd'),
            'echo Procedure is completed')

        then:"check pipelines directories were created"
        def encPipeName = encode(pipelineName)
        def pipeDir = new File(projDir, "pipelines/"  + encPipeName)
        assert pipeDir.exists()
        assertFile(new File(pipeDir, 'pipeline.dsl'), "\n" +
            "pipeline '$pipelineName', {\n\n"
            + "  formalParameter 'ec_stagesToRun', {\n"
            + "    expansionDeferred = '1'\n"
            + "  }"
            + "\n}\n")
        def encStageName = encode(pipeStageName)
        def pipeStageDir = new File(pipeDir, "stages/"  + encStageName)
        assert pipeStageDir.exists()
        assertFile(new File(pipeStageDir, 'stage.dsl'), "\nstage '$pipeStageName'\n")
        def encTaskName = encode(pipeTaskName)
        def taskDir = new File(pipeStageDir, "tasks/" + encTaskName)
        assert taskDir.exists()
        assertFile(new File(taskDir, 'task.dsl'),
            'import java.io.File\n\n\n'
                + "task '$pipeTaskName', {\n"
                + '  actualParameter = [\n'
                + "    \'commandToRun\': new File(projectDir, \"./pipelines/$encPipeName/stages/$encStageName/tasks/$encTaskName" + ".cmd\").text,\n  ]\n"
                + '  subpluginKey = \'EC-Core\'\n'
                + '  subprocedure = \'RunCommand\'\n'
                + '  taskType = \'COMMAND\'\n' + '}\n')
        assertFile(new File(pipeStageDir, "tasks/" + encTaskName + ".cmd"),
            'echo task %1 completed')

        cleanup:
        deleteProjects([projectName: projName], false)
        dsl("deleteArtifact(artifactName: '$artifactName')")
        new File(dslDir).deleteDir()
    }

    def "generate DSL for project with special symbol in project names"() {
        dslDir = 'build/proj_spec_symbols'
        def projName = 'proj | new > name \\'
        def procName = 'Procedure: Verify QA / Notify'
        def procStepName = 'step 1/2'
        args << [projectName: projName,
                 procedureName: procName,
                 procStepName: procStepName]
        def artifactName = 'dsl:proj_spec_symbols'
        File projDir

        given:
        dslFile("proj_spec_symbols.dsl", args)
        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
                        runProcedure(
                          projectName: "generateDslTestProject",
                          procedureName: "generateDslAndPublish",
                          actualParameter: [
                            directory: "$dslDir",
                            objectType: 'project',
                            objectName: "proj | new > name \\\\\\\\",
                            includeAllChildren: '1',
                            includeAcls: '0',
                            includeAclsInDifferentFile: '0',
                            suppressDefaults: '1',
                            suppressParent: '1',
                            artifactName: "$artifactName",
                            artifactVersionVersion: '1.0',
                            runResourceName: '$defaultPool'
                          ]
                        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        retrieveArtifactVersion("$artifactName", "1.0", dslDir)
        projDir = new File (dslDir, "projects/" + encode(projName))

        and: "check project was created"
        assert projDir.exists()
        assertFile(new File(projDir, 'project.dsl'),
                        "\nproject 'proj | new > name \\\\'\n")

        then:"check procedures directory were created"
        def encProcName = encode(procName)
        def procDir = new File(projDir, "procedures/"  + encProcName)
        assert procDir.exists()
        assertFile(new File(procDir, 'procedure.dsl'), "\nprocedure '$procName'\n")
        def procStepDir = new File(procDir, 'steps')
        assert procStepDir.exists()
        def encStepName = encode(procStepName)
        def stepDir = new File(procStepDir, encStepName)
        assert stepDir.exists()
        assertFile(new File(stepDir, 'step.dsl'),
                "import java.io.File\n\n\n"
                        + "step '$procStepName', {\n"
                        + "  command = new File(projectDir, \"./procedures/$encProcName/steps/$encStepName" + ".cmd\").text\n}\n")
        assertFile(new File(procStepDir, encode(procStepName) + '.cmd'),
                'echo Procedure is completed')

        cleanup:
        deleteProjects([
                projectName: StringEscapeUtils.escapeJava(projName)], false)
        dsl("deleteArtifact(artifactName: '$artifactName')")
        new File(dslDir).deleteDir()
    }

    def 'generate DSL for trigger application'() {

        dslDir = 'build/application-trigger'
        def environmentProjectName = randomize('env_project')
        def projectName = randomize('test_project')
        def serviceAccount = randomize('sa')
        def artifactName = 'dsl:trigger-app'
        File projDir, appDir

        def projects = [environmentProject: environmentProjectName,
                        applicationProject: projectName,
                        serviceAccount: serviceAccount]

        args << projects

        given:
        dslFile ('webhook_plugin.dsl')
        dslFile ('trigger/app_with_env_template_mapping.dsl', args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
                        runProcedure(
                          projectName: "generateDslTestProject",
                          procedureName: "generateDslAndPublish",
                          actualParameter: [
                            directory: "$dslDir",
                            objectType: 'project',
                            objectName: "$projectName",
                            includeAllChildren: '1',
                            includeAcls: '0',
                            includeAclsInDifferentFile: '0',
                            suppressDefaults: '1',
                            suppressParent: '0',
                            artifactName: "$artifactName",
                            artifactVersionVersion: '1.0',
                            runResourceName: '$defaultPool'
                          ]
                        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        retrieveArtifactVersion("$artifactName", "1.0", dslDir)
        projDir = new File (dslDir, "projects/$projectName")

        and: "check project was created"
        assert projDir.exists()
        assertFile(new File(projDir, 'project.dsl'), "\nproject '$projectName'\n")

        and:"check application directory were created"
        appDir = new File(projDir, "applications/testApp")
        assert appDir.exists()
        assertFile(new File(appDir, 'application.dsl'), """
application 'testApp', {
  projectName = '$projectName'
}
""")

        then:"check trigger directories were created"
        def triggerDir = new File(appDir, "triggers/app-webhook")
        assert triggerDir.exists()
        assertFile(new File(triggerDir, 'trigger.dsl'), """
trigger 'app-webhook', {
  actualParameter = [
    'param1': 'paramValue',
  ]
  applicationName = 'testApp'
  environmentName = 'test_env_1'
  environmentTemplateName = 'testEnvTemplate'
  pluginKey = 'webhook-plugin'
  pluginParameter = [
    'pushEvent': 'true',
  ]
  processName = 'testApp_process'
  projectName = '$projectName'
  quietTimeMinutes = '0'
  runDuplicates = '1'
  serviceAccountName = '$serviceAccount'
  tierResourceCount = [
    'testEnvTemplateTier': '1',
  ]
  triggerType = 'webhook'
}
""")

        cleanup:
        deleteProjects(projects, false)
        dsl("deleteArtifact(artifactName: '$artifactName')")
        new File(dslDir).deleteDir()
    }

    def "generate DSL for trigger application with suppressParent=true"() {

        dslDir = 'build/application-trigger'
        def environmentProjectName = randomize('env_project')
        def projectName = randomize('test_project')
        def serviceAccount = randomize('sa')
        def artifactName = 'dsl:trigger-app'
        File projDir, appDir

        def projects = [environmentProject: environmentProjectName,
                        applicationProject: projectName,
                        serviceAccount: serviceAccount]

        args << projects

        given:
        dslFile ('webhook_plugin.dsl')
        dslFile ('trigger/app_with_env_template_mapping.dsl', args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
                        runProcedure(
                          projectName: "generateDslTestProject",
                          procedureName: "generateDslAndPublish",
                          actualParameter: [
                            directory: "$dslDir",
                            objectType: 'project',
                            objectName: "$projectName",
                            includeAllChildren: '1',
                            includeAcls: '0',
                            includeAclsInDifferentFile: '0',
                            suppressDefaults: '1',
                            suppressParent: '1',
                            artifactName: "$artifactName",
                            artifactVersionVersion: '1.0',
                            runResourceName: '$defaultPool'
                          ]
                        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        retrieveArtifactVersion("$artifactName", "1.0", dslDir)
        projDir = new File (dslDir, "projects/$projectName")

        and: "check project was created"
        assert projDir.exists()
        assertFile(new File(projDir, 'project.dsl'), "\nproject '$projectName'\n")

        and:"check application directory were created"
        appDir = new File(projDir, "applications/testApp")
        assert appDir.exists()
        assertFile(new File(appDir, 'application.dsl'), "\napplication 'testApp'\n")

        then:"check trigger directories were created"
        def triggerDir = new File(appDir, "triggers/app-webhook")
        assert triggerDir.exists()
        assertFile(new File(triggerDir, 'trigger.dsl'), """
trigger 'app-webhook', {
  actualParameter = [
    'param1': 'paramValue',
  ]
  environmentName = 'test_env_1'
  environmentTemplateName = 'testEnvTemplate'
  pluginKey = 'webhook-plugin'
  pluginParameter = [
    'pushEvent': 'true',
  ]
  quietTimeMinutes = '0'
  runDuplicates = '1'
  serviceAccountName = '$serviceAccount'
  tierResourceCount = [
    'testEnvTemplateTier': '1',
  ]
  triggerType = 'webhook'
}
""")

        cleanup:
        deleteProjects(projects, false)
        dsl("deleteArtifact(artifactName: '$artifactName')")
        new File(dslDir).deleteDir()
    }

    def "generate DSL for credentials"() {

        dslDir = 'build/credentials'
        def projectName = randomize('cred_project')
        def artifactName = 'dsl:cred-app'
        File projDir

        def projects = [project: projectName]

        args << projects

        given:
        dslFile ('credentials.dsl', args)

        when: 'run generate Dsl procedure'
        def result= runProcedureDsl("""
                        runProcedure(
                          projectName: "generateDslTestProject",
                          procedureName: "generateDslAndPublish",
                          actualParameter: [
                            directory: "$dslDir",
                            objectType: 'project',
                            objectName: "$projectName",
                            includeAllChildren: '1',
                            includeAcls: '0',
                            includeAclsInDifferentFile: '0',
                            suppressDefaults: '1',
                            suppressNulls: '1',
                            artifactName: "$artifactName",
                            artifactVersionVersion: '1.0',
                            runResourceName: '$defaultPool'
                          ]
                        )""")
        then:
        assert result.jobId
        def outcome=getJobProperty("outcome", result.jobId)
        assert outcome == "success"

        when:
        retrieveArtifactVersion("$artifactName", "1.0", dslDir)
        projDir = new File (dslDir, "projects/$projectName")

        and: "check project was created"
        assert projDir.exists()
        assertFile(new File(projDir, 'project.dsl'), "\nproject '$projectName'\n")

        and:"check credential provider directory was created"
        def provDir = new File(projDir, "credentialProviders/testCredProvider")
        assert provDir.exists()
        assertFile(new File(provDir, 'credentialProvider.dsl'), """
credentialProvider 'testCredProvider', {
  projectName = '$projectName'
  providerType = 'HASHICORP'
  secretEnginePath = '/path'
  secretEngineType = 'KV2'
  serverUrl = 'http://localhost:1234'
}
""")

        then:"check credentials' directories were created"
        def credDir1 = new File(projDir, "credentials/externalCred")
        assert credDir1.exists()
        assertFile(new File(credDir1, 'credential.dsl'), """
credential 'externalCred', userName: 'testUser', {
  credentialProviderName = 'testCredProvider'
  credentialProviderProjectName = '$projectName'
  credentialType = 'EXTERNAL'
  projectName = '$projectName'
  secretPath = 'db/secret'
}
""")
        def credDir2 = new File(projDir, "credentials/localCred")
        assert credDir2.exists()
        assertFile(new File(credDir2, 'credential.dsl'), """
credential 'localCred', userName: 'localUser', {
  credentialType = 'LOCAL'
  projectName = '$projectName'
}
""")

        cleanup:
        deleteProjects(projects, false)
        dsl("deleteArtifact(artifactName: '$artifactName')")
        new File(dslDir).deleteDir()
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
