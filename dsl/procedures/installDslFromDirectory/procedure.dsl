import java.io.File

def procName = 'installDslFromDirectory'
def dslShell = 'ectool --timeout $[/server/@PLUGIN_KEY@/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

procedure procName,
  resourceName: '$[pool]',
{
  step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[directory]'

  step 'deployTags',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployTags.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployUsers',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployUsers.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployPersonas',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPersonas.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployResources',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResources.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployResourcePools',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResourcePools.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[directory]',
    shell: dslShell

  step 'deployProjects',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjects.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[directory]'

  step 'deployPost',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPost.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[directory]'

}
