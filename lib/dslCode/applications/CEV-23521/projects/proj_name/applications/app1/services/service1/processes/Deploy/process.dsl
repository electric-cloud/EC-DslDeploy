
process 'Deploy', {
  processType = 'DEPLOY'

  processStep 'deployService', {
    errorHandling = 'failProcedure'
    processStepType = 'service'
  }
}
