def loadrelease(String projectDir, String projectName, String dslFile) {
  return evalInlineDsl(dslFile, [projectName: projectName, projectDir: projectDir])
}
def loadreleases(String projectDir, String projectName) {
  // Loop over the sub-directories in the releases directory
  // and evaluate services if a service.dsl file exists
  println "Entering loadreleases for $projectDir ($projectName)"

  File dir = new File(projectDir, 'releases')
  if (dir.exists()) {
    dir.eachDir {
      File dslFile = getObjectDSLFile(it, "release")
      if (dslFile?.exists()) {
        println "Processing release DSL file ${dslFile.absolutePath}"
        def pipe = loadrelease(projectDir, projectName, dslFile.absolutePath)
      }
    }
  }
}
