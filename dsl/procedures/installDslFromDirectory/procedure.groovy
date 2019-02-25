import java.io.File

procedure 'installDslFromDirectory',
  resourceName: '$[pool]',
{

// code is shared with other procedure
def procName = 'installDsl'

  step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

  step 'deployProjects',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjects.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

}
