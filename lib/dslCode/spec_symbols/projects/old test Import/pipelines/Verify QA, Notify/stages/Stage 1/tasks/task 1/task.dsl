import java.io.File


task 'task 1', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/Verify QA, Notify/stages/Stage 1/tasks/task 1.cmd").text,
  ]
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
}
