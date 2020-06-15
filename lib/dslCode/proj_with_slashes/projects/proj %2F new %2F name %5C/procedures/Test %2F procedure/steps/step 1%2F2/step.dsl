import java.io.File


step 'step 1/2', {
  command = new File(projectDir, "./procedures/Test %2F procedure/steps/step 1%2F2.cmd").text
}
