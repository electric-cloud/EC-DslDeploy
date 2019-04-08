import java.io.File

def procName = 'installDslFromDirectory'
def dslShell = 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

procedure procName,
  resourceName: '$[pool]',
{
  step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

  step 'deployTags',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployTags.groovy").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployUsers',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployUsers.groovy").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployPersonas',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPersonas.groovy").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployResources',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResources.groovy").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployResourcePools',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResourcePools.groovy").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployProjects',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjects.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

  step 'deployPost',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPost.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

}
