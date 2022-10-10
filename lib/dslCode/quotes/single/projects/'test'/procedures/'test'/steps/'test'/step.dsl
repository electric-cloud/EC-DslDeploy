import java.io.File


step "'test'", {
  command = new File(projectDir, "./procedures/'test'/steps/'test'.cmd").text
}
