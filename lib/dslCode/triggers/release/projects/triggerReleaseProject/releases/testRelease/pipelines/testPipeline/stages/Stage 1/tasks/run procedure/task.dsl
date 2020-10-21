
task 'run procedure', {
  actualParameter = [
    'entry-param': '$[param]',
  ]
  projectName = 'triggerReleaseProject'
  subprocedure = 'testProc'
  subproject = 'triggerReleaseProject'
  taskType = 'PROCEDURE'

  attachParameter {
    formalParameterName = '/projects/triggerReleaseProject/releases/testRelease/pipelines/testPipeline/formalParameters/param'
  }
}
