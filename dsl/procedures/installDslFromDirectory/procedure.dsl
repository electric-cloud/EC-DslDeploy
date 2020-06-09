import java.io.File

def procName = 'installDslFromDirectory'

def dslShell = 'ectool --timeout $[/server/@PLUGIN_KEY@/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]'

procedure procName,
  jobNameTemplate: 'install-dsl-from-directory-$[jobId]',
  resourceName: '$[pool]',
{
  step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[directory]'

  step 'deployNonProjectEntities',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployNonProjectEntities.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[directory]'

  step 'deployProjects',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjects.groovy").text,
    resourceName: '$[pool]',
    shell: 'ec-groovy',
    workingDirectory: '$[/myJob/CWD]'

  step 'deployPost',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPost.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[/myJob/CWD]'

}
