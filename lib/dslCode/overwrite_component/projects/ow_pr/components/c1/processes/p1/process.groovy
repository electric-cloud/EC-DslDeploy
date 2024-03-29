
process 'p1', {
  description = 'original description'
  processType = 'UNDEPLOY'
  smartUndeployEnabled = '0'
  timeLimit = '15'
  timeLimitUnits = 'seconds'
  workingDirectory  = 'tmp'


  formalParameter 'param1', {
    orderIndex = '1'
    type = 'entry'
  }

  formalParameter 'param2', defaultValue: 'test', {
    expansionDeferred = '1'
    label = 'test'
    orderIndex = '2'
    required = '1'
    type = 'entry'
  }

  processStep 'step1', {
    description = 'original description'
    dependencyJoinType = 'and'
    notificationEnabled = '1'
    notificationTemplate = 'ec_default_manual_process_step_notification_template'
    processStepType = 'manual'
    timeLimit = '10'
    timeLimitUnits = 'seconds'
    assignee = [
      'admin',
    ]

    formalParameter 'param1', {
      orderIndex = '1'
      type = 'entry'
    }

    formalParameter 'param2', defaultValue: 'test', {
      expansionDeferred = '1'
      label = 'test'
      orderIndex = '2'
      required = '1'
      type = 'entry'
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '1'
    }

    property 'property1', {

      // Custom properties

      property 'property2', value: 'test', {
        expandable = '0'
        suppressValueTracking = '1'
      }
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

    formalParameter 'param1', {
      orderIndex = '1'
      type = 'entry'
    }

    formalParameter 'param2', defaultValue: 'test', {
      expansionDeferred = '1'
      label = 'test'
      orderIndex = '2'
      required = '1'
      type = 'entry'
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '1'
    }

    property 'property1', {

      // Custom properties

      property 'property2', value: 'test', {
        expandable = '0'
        suppressValueTracking = '1'
      }
    }
  }

  processDependency 'step1', targetProcessStepName: 'step2'
}
