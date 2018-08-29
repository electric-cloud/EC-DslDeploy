def pipeName='UC1'
pipeline pipeName, {

  stage 'DEV', {
    task 'Start', {
      errorHandling = 'stopOnError'
      notificationEnabled = '1'
      notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
      taskType = 'MANUAL'
      approver = [
        'lrochette'
      ]
    }
  }

  stage 'SIT', {
    gate 'POST', {
      task 'SITExitGate', {
        errorHandling = 'stopOnError'
        gateType = 'POST'
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_gate_task_notification_template'
        taskType = 'APPROVAL'
        approver = [
          'lrochette'
        ]
      }
    }

    task 'JA1 Deploy', {
      environmentName = 'SIT'
      errorHandling = 'stopOnError'
      subapplication = 'tomcat_app'
      subprocess = 'Deploy'
      taskProcessType = 'APPLICATION'
      taskType = 'PROCESS'
    }

    task 'DEV-JA2 Deploy', {
      environmentName = 'SIT'
      subapplication = 'tomcat_app2'
      subprocess = 'Deploy'
      taskProcessType = 'APPLICATION'
      taskType = 'PROCESS'
    }

    task 'JA3 Deploy', {
      environmentName = 'SIT'
      subapplication = 'tomcat_app3'
      subprocess = 'Deploy'
      taskProcessType = 'APPLICATION'
      taskType = 'PROCESS'
    }
  }

  stage 'UAT', {

    gate 'POST', {
      task 'UATExitGate', {
        gateType = 'POST'
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_gate_task_notification_template'
        taskType = 'APPROVAL'
        approver = [
          'lrochette'
        ]
      }
    }

    task 'JA1 Deploy', {
      environmentName = 'UAT'
      subapplication = 'tomcat_app'
      subprocess = 'Deploy'
      taskProcessType = 'APPLICATION'
      taskType = 'PROCESS'
    }
    task 'JA2 Deploy', {
      environmentName = 'UAT'
      subapplication = 'tomcat_app2'
      subprocess = 'Deploy'
      taskProcessType = 'APPLICATION'
      taskType = 'PROCESS'
    }
    task 'JA3 Deploy', {
      environmentName = 'UAT'
      subapplication = 'tomcat_app3'
      subprocess = 'Deploy'
      taskProcessType = 'APPLICATION'
      taskType = 'PROCESS'
    }
  }
}
