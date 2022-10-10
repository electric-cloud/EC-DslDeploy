import java.io.File


step '"test"', {
  command = new File(projectDir, "./procedures/%22test%22/steps/%22test%22.cmd").text
}
