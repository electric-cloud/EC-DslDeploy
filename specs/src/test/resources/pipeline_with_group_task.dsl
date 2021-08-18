project args.projectName, {
    pipeline 'pipeline1', {
            stage 'stage1', {
                task 'cmd1', {
                    actualParameter = [
                        'commandToRun': 'echo test1',
                    ]
                    subpluginKey = 'EC-Core'
                    subprocedure = 'RunCommand'
                    taskType = 'COMMAND'
                }
                task 'group1', {
                    taskType = 'GROUP'
                    task 'cmd2', {
                        actualParameter = [
                            'commandToRun': 'echo test2',
                        ]
                        subpluginKey = 'EC-Core'
                        subprocedure = 'RunCommand'
                        taskType = 'COMMAND'
                    }
                }

                task 'cmd3', {
                    actualParameter = [
                        'commandToRun': 'echo test3',
                    ]
                    subpluginKey = 'EC-Core'
                    subprocedure = 'RunCommand'
                    taskType = 'COMMAND'
                }
            }
    }

}