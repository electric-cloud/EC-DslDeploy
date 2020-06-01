import java.io.File

def procName =  'generateDslToDirectory'
procedure procName,
  {
  jobNameTemplate = 'generate-dsl-to-directory-$[jobId]'
  resourceName= '$[pool]'

  step 'generateDsl', {
    command = new File(pluginDir, "dsl/procedures/$procName/steps/generateDsl.groovy").text
    resourceName = '$[pool]'
    shell = 'ec-groovy'
  }
}