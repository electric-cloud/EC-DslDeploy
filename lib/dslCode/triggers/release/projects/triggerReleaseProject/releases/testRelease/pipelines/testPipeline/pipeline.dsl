
pipeline 'testPipeline', {
  projectName = 'triggerReleaseProject'
  releaseName = 'testRelease'
  templatePipelineName = 'testPipeline'
  templatePipelineProjectName = 'triggerReleaseProject'

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  formalParameter 'param', {
    required = '1'
    type = 'entry'
  }
}
