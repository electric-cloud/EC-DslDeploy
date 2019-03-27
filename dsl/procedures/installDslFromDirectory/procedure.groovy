import java.io.File

def procName = 'installDslFromDirectory'
procedure procName,
  resourceName: '$[pool]',
{
  step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

  step 'deployResources',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResources.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

  step 'deployProjects',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjects.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'
}
