import java.io.File

def procName = 'installDslFromDirectory'

def dslShell = 'ectool --timeout $[/server/@PLUGIN_KEY@/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]'

procedure procName,
  jobNameTemplate: 'install-dsl-from-directory-$[jobId]',
  resourceName: '$[pool]',
{
  step 'setAdditionalDslArguments',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/setAdditionalDslArguments.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[directory]',
    postProcessor: 'postp'

  step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.pl").text,
    resourceName: '$[pool]',
    shell: 'ec-perl',
    workingDirectory: '$[directory]'

  step 'deployTags',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployTags.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployPersonaPages',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPersonaPages.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployPersonaCategories',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPersonaCategories.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployPersonas',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPersonas.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployUsers',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployUsers.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployGroups',
     command: new File(pluginDir, "dsl/procedures/$procName/steps/deployGroups.dsl").text,
     resourceName: '$[pool]',
     workingDirectory: '$[/myJob/CWD]',
     shell: dslShell

  step 'deployReportObjectTypes',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployReportObjectTypes.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployResources',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResources.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

  step 'deployResourcePools',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResourcePools.dsl").text,
    resourceName: '$[pool]',
    workingDirectory: '$[/myJob/CWD]',
    shell: dslShell

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
