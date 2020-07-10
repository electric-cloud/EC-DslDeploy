import java.io.File


task 'task %% |', {
  actualParameter = [
    'commandToRun': new File(projectDir, "./pipelines/Verify QA %2F Notify/stages/Stage%3E1/tasks/task %% %7C.cmd").text,
  ]
  subpluginKey = 'EC-Core'
  subprocedure = 'RunCommand'
  taskType = 'COMMAND'
}
