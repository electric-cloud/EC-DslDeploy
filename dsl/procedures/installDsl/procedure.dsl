import java.io.File

def procName = 'installDsl'
procedure procName, {
    step 'retrieveArtifact',
            command: new File(pluginDir, "dsl/procedures/$procName/steps/retrieveArtifact.pl").text,
            errorHandling: 'abortProcedure',
            resourceName: '$[pool]',
            shell: 'cb-perl'

    step 'installFromDirectory',
            subprocedure: 'installDslFromDirectory',
            actualParameter: [
                    directory: ".",
                    pool     : '$[/myJob/assignedResourceName]',
                    overwrite: '$[overwrite]'
            ]
}
