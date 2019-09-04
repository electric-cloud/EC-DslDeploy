   stage 'SIT', {
     afterStage = 'DEV'
     gate 'POST', {
       task 'SITExitGate', {
         errorHandling = 'stopOnError'
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
