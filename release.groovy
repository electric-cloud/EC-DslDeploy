def loadRelease(String projectDir, String projectName, String dslFile) {
  return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
}
def loadReleases(String projectDir, String projectName) {
  // Loop over the sub-directories in the releases directory
  // and evaluate reelases if a release.dsl file exists
  println "Entering loadReleases for $projectDir ($projectName)"

  File dir = new File(projectDir, 'releases')
  if (dir.exists()) {
    dir.eachDir() {
      File dslFile = getObjectDSLFile(it, "release")
      if (dslFile?.exists()) {
        println "Processing release DSL file ${dslFile.absolutePath}"
        def pipe = loadRelease(projectDir, projectName, dslFile.absolutePath)

      }
    }
  }
}
