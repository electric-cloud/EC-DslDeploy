release 'testRelease', {
  description = 'val'
  tags = ["arg1", "arg2"]
  plannedEndDate = '2019-04-04'
  plannedStartDate = '2019-03-21'

  pipeline 'pipeline_testRelease', {
    disableRestart = '0'
    enabled = '1'
    overrideWorkspace = '0'
    pipelineRunNameTemplate = null
    releaseName = 'testRelease'
    skipStageMode = 'ENABLED'
    templatePipelineName = null
    templatePipelineProjectName = null
    type = null
    workspaceName = null

    formalParameter 'ec_stagesToRun', defaultValue: null, {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = null
    }

    stage 'Stage 1', {
      colorCode = null
      completionType = 'auto'
      condition = null
      duration = null
      parallelToPrevious = null
      pipelineName = 'pipeline_testRelease'
      plannedEndDate = null
      plannedStartDate = null
      precondition = null
      resourceName = null
      waitForPlannedStartDate = '0'

      gate 'PRE', {
        condition = null
        precondition = null
      }

      gate 'POST', {
        condition = null
        precondition = null
      }
    }
  }
}