import java.io.File


step 'testProcedureStep', {
  description = 'testProcedureStep description'
  command = new File(projectDir, "./procedures/testProcedure/steps/testProcedureStep.cmd").text
}
