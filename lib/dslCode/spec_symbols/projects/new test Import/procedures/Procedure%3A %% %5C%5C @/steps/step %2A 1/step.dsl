import java.io.File


step 'step * 1', {
  command = new File(projectDir, "./procedures/Procedure%3a %% %5C%5C @/steps/step %2A 1.cmd").text
}
