
pipeline 'testPipeline', {
  projectName = 'triggerReleaseProject'

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  formalParameter 'param', {
    required = '1'
    type = 'entry'
  }
}
