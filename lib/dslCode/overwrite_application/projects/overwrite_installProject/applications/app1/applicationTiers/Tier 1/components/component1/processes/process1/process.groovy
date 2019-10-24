import java.io.File


process 'proc1', {
    processType = 'DEPLOY'
    timeLimitUnits = 'minutes'

    processStep 's1', {
        actualParameter = [
                'commandToRun': new File(projectDir, "./applications/app1/applicationTiers/Tier 1/components/component1/processes/process1/processSteps/s1.cmd").text,
        ]
        alwaysRun = '0'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'command'
        subprocedure = 'RunCommand'
        subproject = '/plugins/EC-Core/project'
        useUtilityResource = '0'
    }
}