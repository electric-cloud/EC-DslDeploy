import java.io.File


step 'step 1', {
  command = new File(projectDir, "./procedures/A Procedure #1/steps/step 1.cmd").text
}
