
process 'Deploy Application', {
  description = 'System generated process for microservice application'
  applicationName = 'app1'
  processType = 'DEPLOY'
  projectName = 'CEV-28776'

  formalParameter 'ec_Air flow-run', defaultValue: '1', {
    expansionDeferred = '1'
    type = 'checkbox'
  }

  processStep 'Air flow', {
    description = 'System generated step to invoke microservice process'
    processStepType = 'process'
    projectName = 'CEV-28776'
    submicroservice = 'Air flow'
    submicroserviceProcess = 'Deploy Microservice Process'
    useUtilityResource = '1'

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }

    property 'subservice', value: ''

    property 'subserviceProcess', value: ''
  }
}
