
process 'Deploy Microservice Process', {
  description = 'System generated process for microservice deployment'
  microserviceName = 'Air flow'
  processType = 'DEPLOY'
  projectName = 'CEV-28776'

  processStep 'Retrieve Artifact', {
    description = 'System generated step to retrieve microservice definition artifact'
    processStepType = 'plugin'
    projectName = 'CEV-28776'
    subprocedure = 'Source Provider'
    subproject = '/plugins/EC-Helm/project'

    // Custom properties

    property 'subservice', value: ''

    property 'subserviceProcess', value: ''
  }

  processStep 'Deploy Microservice', {
    description = 'System generated step to deploy microservice'
    processStepType = 'plugin'
    projectName = 'CEV-28776'
    subprocedure = 'Deploy Service'
    subproject = '/plugins/EC-Helm/project'

    // Custom properties

    property 'subservice', value: ''

    property 'subserviceProcess', value: ''
  }

  processDependency 'Retrieve Artifact', targetProcessStepName: 'Deploy Microservice'
}
