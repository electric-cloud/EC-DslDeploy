
project args.projectName, {

  procedure args.procedureName, {

    step args.procStepName, {
      command = 'echo Procedure is completed'
    }
  }

  pipeline args.pipelineName, {

    formalParameter 'ec_stagesToRun', {
      expansionDeferred = '1'
    }

    stage args.pipeStageName, {
      pipelineName = args.pipelineName

      gate 'PRE'

      gate 'POST'

      task args.pipeTaskName, {
        actualParameter = [
          'commandToRun': 'echo task %1 completed',
        ]
        subpluginKey = 'EC-Core'
        subprocedure = 'RunCommand'
        taskType = 'COMMAND'
      }
    }
  }
}
