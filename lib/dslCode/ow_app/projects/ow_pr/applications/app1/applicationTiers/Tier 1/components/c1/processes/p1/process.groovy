import java.io.File


process 'p1', {
    processType = 'DEPLOY'
    timeLimitUnits = 'minutes'

    processStep 's1', {
        actualParameter = [
                'commandToRun': new File(projectDir, "./applications/app1/applicationTiers/Tier 1/components/c1/processes/p1/processSteps/s1.cmd").text,
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