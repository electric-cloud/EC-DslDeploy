import java.io.File


task 'cmd', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/pipeline/stages/Stage 1/tasks/cmd.cmd").text,
  ]
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
}
