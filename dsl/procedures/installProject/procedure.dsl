import java.io.File

def procName = 'installProject'

def dslShell = 'ectool --timeout $[/server/@PLUGIN_KEY@/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]'

procedure procName, {

    step 'setAdditionalDslArguments',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/setAdditionalDslArguments.pl").text,
    shell: 'ec-perl',
    postProcessor: 'postp'

    //evalDsl the main.groovy if it exists
    step 'deployProject',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProject.dsl").text,
    shell: dslShell

    step 'deployProcedures',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProcedures.dsl").text,
    shell: dslShell

    step 'deployResourceTemplates',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployResourceTemplates.dsl").text,
    shell: dslShell

    step 'deployWorkflowDefinitions',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployWorkflowDefinitions.groovy").text,
    shell: dslShell

    step 'deployEnvironmentTemplates',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployEnvironmentTemplates.groovy").text,
    shell: dslShell

    step 'deployEnvironments',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployEnvironments.groovy").text,
    shell: dslShell

    step 'deployComponentTemplates',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployComponentTemplates.groovy").text,
    shell: dslShell

    step 'deployApplications',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployApplications.groovy").text,
    shell: dslShell

    step 'deployServices',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployServices.groovy").text,
    shell: dslShell

    step 'deployPipelines',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPipelines.groovy").text,
    shell: dslShell

    step 'deployReleases',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployReleases.groovy").text,
    shell: dslShell

    step 'deploySchedules',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deploySchedules.dsl").text,
    shell: dslShell

    step 'deployCatalogs',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployCatalogs.groovy").text,
    shell: dslShell

    step 'deployReports',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployReports.groovy").text,
    shell: dslShell

    step 'deployDashboards',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployDashboards.groovy").text,
    shell: dslShell

  // Do not Display in the property picker
  property 'standardStepPicker', value: false
}
