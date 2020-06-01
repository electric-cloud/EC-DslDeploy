
pipeline 'pipe2', {
  description = ''
  disableMultipleActiveRuns = '0'
  disableRestart = '0'
  enabled = '1'
  overrideWorkspace = '0'
  skipStageMode = 'ENABLED'

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
    required = '0'
  }
}
