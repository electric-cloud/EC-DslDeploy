import java.io.File


step 'testStep', {
  command = new File(projectDir, "./procedures/testProc/steps/testStep.cmd").text
  projectName = 'triggerReleaseProject'

  attachParameter {
    formalParameterName = '/projects/triggerReleaseProject/procedures/testProc/formalParameters/entry-param'
  }
}
