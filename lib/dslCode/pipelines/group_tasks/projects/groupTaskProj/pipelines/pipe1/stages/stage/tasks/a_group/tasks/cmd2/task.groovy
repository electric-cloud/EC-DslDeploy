import java.io.File


task 'cmd2', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/pipe1/stages/stage/tasks/a_group/tasks/cmd2.cmd").text,
  ]
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
}
