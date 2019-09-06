
task 'manual', {
  description = ''
  advancedMode = '0'
  allowOutOfOrderRun = '0'
  allowSkip = '0'
  alwaysRun = '0'
  disableFailure = '0'
  enabled = '1'
  errorHandling = 'stopOnError'
  insertRollingDeployManualStep = '0'
  notificationEnabled = '1'
  notificationTemplate = 'ec_default_pipeline_manual_task_notification_templat                                                                                                                                  e'
  resourceName = ''
  skippable = '0'
  subproject = 'foo3'
  taskType = 'MANUAL'
  useApproverAcl = '0'
  waitForPlannedStartDate = '0'
  approver = [
    'admin',
  ]

  formalParameter 'p1', {
    expansionDeferred = '0'
    orderIndex = '1'
    required = '1'
    type = 'project'
  }

  formalParameter 'p2', {
    expansionDeferred = '0'
    orderIndex = '2'
    projectFormalParameterName = 'p1'
    required = '1'
    type = 'pipeline'
  }
}
