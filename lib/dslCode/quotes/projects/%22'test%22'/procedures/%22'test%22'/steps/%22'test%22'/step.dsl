import java.io.File


step '%22\'test%22\'', {
  command = new File(projectDir, "./procedures/%22'test%22'/steps/%22'test%22'.cmd").text
}
