import java.io.File

def procName = 'installProject'

def dslShell = 'ectool --timeout $[/server/@PLUGIN_KEY@/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]'

procedure procName, {

    step 'deployProjectEntities',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjectEntities.pl").text,
    shell: 'cb-perl',
    postProcessor: 'postp'

  // Do not Display in the property picker
  property 'standardStepPicker', value: false
}
