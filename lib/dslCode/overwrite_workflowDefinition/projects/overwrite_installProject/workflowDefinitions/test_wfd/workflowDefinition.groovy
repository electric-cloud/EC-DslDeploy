
workflowDefinition 'test_wfd', {
  description = 'original description'
  workflowNameTemplate = 'original template'

  stateDefinition 'start', {
    description = 'original description'
    startable = '0'
    subprocedure = 'Set Property'
    subproject = 'EC-Examples'
    substartingState = ''
    subworkflowDefinition = ''
    actualParameter 'property', 'foo'
    actualParameter 'value', 'foo'

    emailNotifier 'testNotifier', {
      condition = ''
      configName = ''
      destinations = 'a@a.a'
      eventType = 'onEnter'
      formattingTemplate = '''Subject: ElectricFlow notification

Message body goes here.'''
    }

    formalParameter 'testParameter', defaultValue: '', {
      type = 'entry'
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'testParameter', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    testStartProperty = ''
  }

  stateDefinition 'finish', {
    startable = '0'
    subprocedure = 'Set Property'
    subproject = 'EC-Examples'
    substartingState = ''
    subworkflowDefinition = ''
    actualParameter 'property', 'foo'
    actualParameter 'value', 'foo'

    emailNotifier 'testFinishNotifier', {
      condition = ''
      configName = ''
      destinations = 'a@a.com'
      eventType = 'onEnter'
      formattingTemplate = '''Subject: ElectricFlow notification

Message body goes here.'''
    }

    formalParameter 'testFinishParameter', defaultValue: '', {
      type = 'entry'
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'testFinishParameter', {

          // Custom properties
          formType = 'standard'
        }

        property 'testStartParameter', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
    testFinishProperty = ''
  }

  transitionDefinition 'transition1', {
    description = 'original description'
    condition = '1'
    stateDefinitionName = 'start'
    targetState = 'finish'
    trigger = 'manual'

    // Custom properties
    testTransitionProperty = ''
  }
}
