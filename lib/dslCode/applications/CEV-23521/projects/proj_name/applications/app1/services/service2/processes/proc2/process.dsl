
process 'proc2', {
  processType = 'DEPLOY'

  processStep 'step1', {
    dependencyJoinType = 'and'
    notificationEnabled = '1'
    notificationTemplate = 'ec_default_manual_process_step_notification_template'
    processStepType = 'manual'
    assignee = [
      'admin',
    ]

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '1'
    }
  }

  processStep 'step2', {
    dependencyJoinType = 'and'
    notificationEnabled = '1'
    notificationTemplate = 'ec_default_manual_process_step_notification_template'
    processStepType = 'manual'
    assignee = [
      'admin',
    ]

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '1'
    }
  }

  processDependency 'step1', targetProcessStepName: 'step2'
}
