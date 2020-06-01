import java.io.File


step 'step # 1', {
  command = new File(projectDir, "./procedures/A Procedure %% @5C@5C @/steps/step # 1.cmd").text
}
