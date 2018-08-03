import java.io.File

def procName = 'installProject'
procedure procName, {

	//evalDsl the main.groovy if it exists
	step 'deployProject',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProject.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

  // Do not Display in the property picker
  property 'standardStepPicker', value: false
}
