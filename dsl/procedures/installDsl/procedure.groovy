import java.io.File

def procName = 'installDsl'
procedure procName, {

	step 'retrieveArtifact',
		subproject: '/plugins/EC-Artifact/project',
		subprocedure: 'retrieve',
		actualParameter:[
			artifactName: '$[artName]',
			versionRange: '$[artVersion]',
			artifactVersionLocationProperty: '/myJob/retrievedArtifactVersions/$[assignedResourceName]',
			retrieveToDirectory: "."
	]

	//evalDsl the main.groovy if it exists
	step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.groovy").text,
    shell: 'ec-groovy'

	// loop on each project
	step 'projectLoop'
}
