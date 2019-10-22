import java.io.File


task 'task1', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/pipe1/stages/stage/tasks/b_group/tasks/task1.cmd").text,
  ]
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
}
