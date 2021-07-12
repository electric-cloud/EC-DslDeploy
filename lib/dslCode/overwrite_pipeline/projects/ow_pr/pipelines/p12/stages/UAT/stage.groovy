  stage 'UAT', {
    afterStage = 'SIT'

    gate 'POST', {
      task 'UATExitGate', {
        gateType = 'POST'
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_gate_task_notification_template'
        taskType = 'APPROVAL'
        approver = [
          'admin'
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
