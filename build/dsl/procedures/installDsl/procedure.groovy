import java.io.File

def procName = 'installDsl'
procedure procName, {

	step 'retrieveArtifact',
		subproject: '/plugins/EC-Artifact/project',
		subprocedure: 'sub-pac_createConfigurationCode'
		actualParameter:[
			artifactName: '$[artName]',
			versionRange: '$[artVersion]',
			artifactVersionLocationProperty: '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
	]

	step 'deployDsl',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployDsl.groovy").text,
    shell: 'ec-groovy'

}
