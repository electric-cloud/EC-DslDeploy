 deployerApplication 'myApp', {
    applicationProjectName = '2_applicationProject'
    enforceDependencies = '0'
    errorHandling = 'stopOnError'
    orderIndex = '1'
    processName = 'myApp_process'
    smartDeploy = '1'
    stageArtifacts = '0'
	
	deployerConfiguration 'dc1', {
      environmentName = 'myEnv2'
      environmentProjectName = '2_applicationProject'
      insertRollingDeployManualStep = '0'
      processName = 'myApp_process'
      skipDeploy = '0'
      stageName = 'pipe1stage'
    }
	
    deployerConfiguration 'dc2', {
      environmentName = 'myEnv2'
      environmentProjectName = '2_applicationProject'
      insertRollingDeployManualStep = '0'
      processName = 'myApp_process'
      skipDeploy = '0'
      stageName = 'pipe2stage'
    }
}