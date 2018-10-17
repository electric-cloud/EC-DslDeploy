import java.io.File

def procName = 'installProject'
procedure procName, {

	//evalDsl the main.groovy if it exists
	step 'deployProjectAndProcedures',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjectAndProcedures.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployEnvironments',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployEnvironments.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployComponentTemplates',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployComponentTemplates.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployApplications',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployApplications.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployServices',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployServices.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployPipelines',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployPipelines.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployReleases',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployReleases.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

	step 'deployCatAndDash',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployCatAndDash.groovy").text,
    shell: 'ectool evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl"'

  // Do not Display in the property picker
  property 'standardStepPicker', value: false
}
