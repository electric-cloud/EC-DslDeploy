release args.relName,
  plannedStartDate: args.startDate,
  plannedEndDtae: args.endDate,
  projectName: "UC4",
{
  property "locked", value: "false"

  pipeline args.relName, {
    stage 'init', {
      task "Open Change Request",
        taskType: 'PROCEDURE',
        subproject: 'UC4',
        subprocedure: 'Open Change Request'
    }   // Init stage

   stage "uat", {
      task "DEPLOYER",
         taskType: "GROUP",
         groupRunType:"parallel"
      task "Functional Tests"
      task "Regression Tests"

      task "Update Change",
         taskType: 'MANUAL',
         errorHandling: 'stopOnError',
         description: 'Update change and proceed to impl prep phase',
         instruction: 'Update Change',
         notificationEnabled: "0",
         notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
         approver: [
           'admin',
         ]
      task "Approve Release",
         taskType: 'MANUAL',
         errorHandling: 'stopOnError',
         description: 'Disassociate not ready pipelines and approve release',
         instruction: 'Disassociate not read pipelines and approve release',
         notificationEnabled: "0",
         notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
         approver: [
           'admin',
         ]
    }

    stage 'preprod',
      afterStage: 'uat',
    {
      gate 'PRE', {
        task 'Approve to Pre Production',
         description: 'Gate to let the application pipelines go to PreProd',
         errorHandling: 'stopOnError',
         taskType: 'APPROVAL',
         notificationEnabled: "0",
         notificationTemplate: 'ec_default_gate_task_notification_template',
         approver: [
           'admin',
         ]
      }

      task "Lock Release",
         description: 'Set the property blocking the apps to join',
         taskType: 'COMMAND',
         subpluginKey: "EC-Core",
         subprocedure: "runCommand",
         actualParameter: [
           commandToRun: 'ectool setProperty /myRelease/locked --value true'
         ]
      task "DEPLOYER",
         taskType: "GROUP",
         groupRunType:"parallel",
         afterTask: "Lock Release"
         
      task "Release Associated Changes",
         description: 'Release Pipeline release associated chagnes on Pre-prod date',
         taskType: 'COMMAND',
         subpluginKey: "EC-Core",
         subprocedure: "runCommand",
         actualParameter: [
           commandToRun: 'echo placeholder step'
         ]
       task "Block Impacted Domains",
         taskType: 'MANUAL',
         errorHandling: 'stopOnError',
         description: 'Block impacted domains or environment form unauthorized changes',
         instruction: 'Block impacted domains or environment form unauthorized changes',
         notificationEnabled: "0",
         notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
         approver: [
           'admin',
         ]
         task "Functional Tests", {
            groupName = 'Testing'
            errorHandling = 'stopOnError'
            taskType = 'COMMAND'
            subpluginKey = "EC-Core"
            subprocedure = "runCommand"
            actualParameter = [
              commandToRun: 'echo run functional tests'
            ]
          }
          task "Regression Tests", {
            groupName = 'Testing'
            errorHandling = 'stopOnError'
            taskType = 'COMMAND'
            subpluginKey = "EC-Core"
            subprocedure = "runCommand"
            actualParameter = [
              commandToRun: 'echo run regression tests'
            ]
          }

        task "Retest Defect Fixes",
          taskType: 'MANUAL',
          errorHandling: 'stopOnError',
          instruction: 'Retest of Defect Fixes',
          notificationEnabled: "0",
          notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
          approver: [
            'admin',
          ]
        task "Monitor Pipeline Testing",
          taskType: 'COMMAND',
          subpluginKey: "EC-Core",
          subprocedure: "runCommand",
          actualParameter: [
            commandToRun: 'echo monitoring pipline testing progress'
          ]

    }

    stage 'production',
      afterStage: 'preprod',
    {
      gate 'PRE', {
        task 'Approve Deploy to Production',
         description: 'Gate to approve promotion to production',
         errorHandling: 'stopOnError',
         taskType: 'APPROVAL',
         notificationEnabled: "0",
         notificationTemplate: 'ec_default_gate_task_notification_template',
         approver: [
           'admin',
         ]
       }
       task "Close Prep Phase",
        description: 'Close impl-prep phase, request area approval and notify helpdesk',
        taskType: 'COMMAND',
        subpluginKey: "EC-Core",
        subprocedure: "runCommand",
        actualParameter: [
         commandToRun: 'echo notify helpdesk'
        ]
      task "Provide Area Approval",
        taskType: 'MANUAL',
        errorHandling: 'stopOnError',
        instruction: 'TL Review and Provide Area Approval',
        notificationEnabled: "0",
        notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
        approver: [
          'admin',
        ]
      task "Execute Go/No-Go Meeting",
        taskType: 'MANUAL',
        errorHandling: 'stopOnError',
        instruction: 'Complete Go/No-go meeting/email',
        notificationEnabled: "0",
        notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
        approver: [
          'admin',
        ]
      task "Request Promotion to Production",
        taskType: 'MANUAL',
        errorHandling: 'stopOnError',
        instruction: 'Manually request promotion to prod',
        notificationEnabled: "0",
        notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
        approver: [
          'admin',
        ]
      task "Change Meeting approval",
        taskType: 'MANUAL',
        errorHandling: 'stopOnError',
        instruction: 'Attend change meeting & obtain prod approval for all associated changes',
        notificationEnabled: "0",
        notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
        approver: [
          'admin',
        ]
      task "Start execution phase",
        taskType: 'MANUAL',
        errorHandling: 'stopOnError',
        instruction: 'SVC & SVP captures approval to proceed to execution phase',
        notificationEnabled: "0",
        notificationTemplate: 'ec_default_pipeline_manual_task_notification_template',
        approver: [
          'admin',
        ]
      task "Initate Mainframe Pipeline",
        taskType: 'COMMAND',
        description: 'Initiate mainframe individual pipelines',
        subpluginKey: "EC-Core",
        subprocedure: "runCommand",
        actualParameter: [
          commandToRun: 'echo placeholder for now to start Mainframe pipeline(s)'
        ]

       task "Update SVC Change",
          taskType: 'COMMAND',
          description: 'Update SVC Change for night 1 and release Pre-Prod domain restriction',
          subpluginKey: "EC-Core",
          subprocedure: "runCommand",
          actualParameter: [
            commandToRun: 'echo updating SVC change for night 1'
          ]
       task "Update SVC Change",
          taskType: 'COMMAND',
          description: 'Update SVC Change for night 2',
          subpluginKey: "EC-Core",
          subprocedure: "runCommand",
          actualParameter: [
            commandToRun: 'echo updating SVC change for night 2'
          ]

      task "Close Change Request",
        taskType: 'PROCEDURE',
        subproject: 'UC4',
        subprocedure: 'Open Change Request'

    }
    stage 'postmortem'
  }
}
