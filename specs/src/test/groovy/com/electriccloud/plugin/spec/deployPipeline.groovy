package com.electriccloud.plugin.spec

import spock.lang.Shared

class deployPipeline
        extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "pipeline_project"

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """
      deleteProject(projectName: "$projName")
    """
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
    }

    def "deploy pipeline stages with metadata.json"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/CEV-23073_metadata/projects/CEV-23073_metadata",
            projName: 'pipeline_project'
          ]
        )""")
        then: "job succeeded"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        def stages = dsl "getStages projectName: '$projName', pipelineName: 'pipeline1'"

        assert stages.stage.size() == 3

        def devStage = stages.stage.find{it.stageName == 'dev'}
        assert devStage.index == '0'

        def qeStage = stages.stage.find{it.stageName == 'qe'}
        assert qeStage.index == '1'

        def prodStage = stages.stage.find{it.stageName == 'prod'}
        assert prodStage.index == '2'
    }

    def "deploy pipeline stages without metadata.json"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/CEV-23073_no_metadata/projects/CEV-23073_no_metadata",
            projName: 'pipeline_project2'
          ]
        )""")
        then: "job completed with warnings"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

        def stages = dsl "getStages projectName: 'pipeline_project2', pipelineName: 'pipeline1'"

        assert stages.stage.size() == 3

        def devStage = stages.stage.find{it.stageName == 'dev'}
        assert devStage.index == '0'

        def qeStage = stages.stage.find{it.stageName == 'qe'}
        assert qeStage.index == '2'

        def prodStage = stages.stage.find{it.stageName == 'prod'}
        assert prodStage.index == '1'
    }


    def "deploy pipeline group tasks"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/pipelines/group_tasks/projects/groupTaskProj",
            projName: 'pipeline_group_proj'
          ]
        )""")
        then: "job succeeded"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        def stages = dsl "getStages projectName: 'pipeline_group_proj', pipelineName: 'pipe1'"

        assert stages.stage.size() == 1

        def devStage = stages.stage.find{it.stageName == 'stage'}
        assert devStage.index == '0'

        def tasks = dsl "getTasks projectName: 'pipeline_group_proj', pipelineName: 'pipe1', stageName: 'stage'"

        assert tasks
        assert tasks.task.size() == 2

        def group1 = tasks.task.find{it.taskName=='b_group'}
        def group2 =  tasks.task.find{it.taskName=='a_group'}
        assert group1.index < group2.index
        //
        def group1Subtasks = group1.task
        assert group1Subtasks.size() == 2
        //
        def group1ManualTask = group1Subtasks.find{it.taskName == 'manual1'}
        def group1CmdTask = group1Subtasks.find{it.taskName == 'task1'}
        assert group1CmdTask.index < group1ManualTask.index
        assert group1CmdTask.actualParameters.parameterDetail[0].parameterValue =='echo test'

        //
        def group2Subtasks = group2.task
        assert group2Subtasks.size() == 2
        def group2ManualTask = group2Subtasks.find{it.taskName == 'manual2'}
        def group2CmdTask = group2Subtasks.find{it.taskName == 'cmd2'}
        assert group2CmdTask.index > group2ManualTask.index
        assert group2CmdTask.actualParameters.parameterDetail[0].parameterValue =='sleep 1'
    }

    def "deploy pipeline group tasks - dsl files"()
    {
        given: "the pipeline_project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/pipelines/group_tasks_dsl/projects/groupTaskProj",
            projName: 'pipeline_group_proj'
          ]
        )""")
        then: "job succeeded"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        def stages = dsl "getStages projectName: 'pipeline_group_proj', pipelineName: 'pipe1'"

        assert stages.stage.size() == 1

        def devStage = stages.stage.find{it.stageName == 'stage'}
        assert devStage.index == '0'

        def tasks = dsl "getTasks projectName: 'pipeline_group_proj', pipelineName: 'pipe1', stageName: 'stage'"

        assert tasks
        assert tasks.task.size() == 2

        def group1 = tasks.task.find{it.taskName=='b_group'}
        def group2 =  tasks.task.find{it.taskName=='a_group'}
        assert group1.index < group2.index
        //
        def group1Subtasks = group1.task
        assert group1Subtasks.size() == 2
        //
        def group1ManualTask = group1Subtasks.find{it.taskName == 'manual1'}
        def group1CmdTask = group1Subtasks.find{it.taskName == 'task1'}
        assert group1CmdTask.index < group1ManualTask.index
        assert group1CmdTask.actualParameters.parameterDetail[0].parameterValue =='echo test'

        //
        def group2Subtasks = group2.task
        assert group2Subtasks.size() == 2
        def group2ManualTask = group2Subtasks.find{it.taskName == 'manual2'}
        def group2CmdTask = group2Subtasks.find{it.taskName == 'cmd2'}
        assert group2CmdTask.index > group2ManualTask.index
        assert group2CmdTask.actualParameters.parameterDetail[0].parameterValue =='sleep 1'
    }

}
