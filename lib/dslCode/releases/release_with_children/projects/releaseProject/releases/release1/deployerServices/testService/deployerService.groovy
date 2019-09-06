deployerService 'testService', {
    enforceDependencies = '0'
    errorHandling = 'stopOnError'
    orderIndex = '2'
    processName = 'testService_process'
    serviceProjectName = '3_serviceProject'

    deployerConfiguration 'dc3', {
      environmentName = 'myEnv'
      environmentProjectName = '3_serviceProject'
      insertRollingDeployManualStep = '0'
      processName = 'testService_process'
      skipDeploy = '0'
      stageName = 'pipe1stage'
      actualParameter 'procedureParam', 'ls'
    }

    deployerConfiguration 'dc4', {
      environmentName = 'myEnv'
      environmentProjectName = '3_serviceProject'
      insertRollingDeployManualStep = '0'
      processName = 'testService_process'
      skipDeploy = '0'
      stageName = 'pipe2stage'
      actualParameter 'procedureParam', 'ls'
    }
  }