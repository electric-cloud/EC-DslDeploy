import java.io.File

def procName = 'importDslFromGit'
procedure procName, {
    step 'checkoutDsl',
            subprocedure: 'CheckoutCode',
            subproject:'/plugins/ECSCM-Git/project',
            resourceName: '$[pool]',
                actualParameter: [
                        clone: '$[clone]',
                        commit: '$[commit]',
                        config: '$[config]',
                        depth: '$[depth]',
                        dest: '$[dest]',
                        GitBranch: '$[GitBranch]',
                        GitRepo: '$[GitRepo]',
                        overwrite: '$[GitOverwrite]',
                        tag: '$[tag]'
                ]

    step 'installFromDirectory',
            subprocedure: 'installDslFromDirectory',
            actualParameter: [
                    directory: '$[dest]',
                    pool     : '$[pool]',
                    overwrite: '$[overwrite]'
            ]
    step 'cleanup', {
        alwaysRun = '1'
        command = new File(pluginDir, "dsl/procedures/$procName/steps/cleanup.groovy").text
        resourceName = '$[pool]'
        shell = 'ec-groovy'
      }
}