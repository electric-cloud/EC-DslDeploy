import java.io.File


task 'task2', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/pipeline1/stages/dev/tasks/task2.pl").text,
    'shellToUse': 'cb-perl',
  ]
  advancedMode = '0'
  allowOutOfOrderRun = '0'
  alwaysRun = '0'
  enabled = '1'
  errorHandling = 'stopOnError'
  insertRollingDeployManualStep = '0'
  skippable = '0'
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
  useApproverAcl = '0'
  waitForPlannedStartDate = '0'
}
