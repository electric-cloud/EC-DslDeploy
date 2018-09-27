import java.io.File

def procName = 'installDsl'
procedure procName, {

	step 'retrieveArtifact',
   command: new File(pluginDir, "dsl/procedures/$procName/steps/retrieveArtifact.pl").text,
   errorHandling: 'abortProcedure',
   resourceName: '$[pool]',
   shell: 'ec-perl'
/*
    subproject: '/plugins/EC-Artifact/project',
		subprocedure: 'retrieve',
		actualParameter:[
			artifactName: '$[artName]',
			versionRange: '$[artVersion]',
			artifactVersionLocationProperty: '/myJob/retrievedArtifactVersions/$[assignedResourceName]',
			retrieveToDirectory: "."
	]
*/

	//evalDsl the main.groovy if it exists
	step 'deployMain',
    command: new File(pluginDir, "dsl/procedures/$procName/steps/deployMain.groovy").text,
    shell: 'ec-groovy',
    resourceName: '$[/myJob/assignedResourceName]'

	// loop on each project
	step 'deployProjects',
		command: new File(pluginDir, "dsl/procedures/$procName/steps/deployProjects.groovy").text,
    resourceName: '$[/myJob/assignedResourceName]',
    shell: 'ec-groovy'
}
