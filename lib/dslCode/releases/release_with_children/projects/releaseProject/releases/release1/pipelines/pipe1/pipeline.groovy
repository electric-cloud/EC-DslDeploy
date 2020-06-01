
pipeline 'pipe1', {
  description = ''
  disableMultipleActiveRuns = '0'
  disableRestart = '0'
  enabled = '1'
  overrideWorkspace = '0'
  skipStageMode = 'ENABLED'

  formalParameter 'param1', {
    expansionDeferred = '0'
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
    required = '0'
  }
}
