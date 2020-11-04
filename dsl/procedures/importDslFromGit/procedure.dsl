import java.io.File

def procName = 'importDslFromGit'
procedure procName, {
    jobNameTemplate = 'import-dsl-from-git-$[jobId]'

    step 'Clone',
            subprocedure: 'Clone',
            subproject: '/plugins/EC-Git/project',
            condition: '$[/myJob/actualParameters/clone]',
            resourceName: '$[rsrcName]',
            errorHandling: 'abortProcedure',
                actualParameter: [
                        branch: '$[branch]',
                        commit: '$[commit]',
                        config: '$[gitConfig]',
                        depth: '$[depth]',
                        gitRepoFolder: '$[dest]',
                        overwrite: '$[GitOverwrite]',
                        repoUrl: '$[repoUrl]',
                        tag: '$[tag]'
                    ]

    step 'Pull',
            subprocedure: 'Pull',
            subproject: '/plugins/EC-Git/project',
            resourceName: '$[rsrcName]',
            errorHandling: 'abortProcedure',
                 actualParameter: [
                        branch: '$[branch]',
                        config: '$[gitConfig]',
                        gitRepoFolder: '$[dest]',
                        repoUrl: '$[repoUrl]',
                        ]


    step 'installFromDirectory',
            subprocedure: 'installDslFromDirectory',
            errorHandling: 'abortProcedure',
            actualParameter: [
                    directory: '$[dest]',
                    pool     : '$[rsrcName]',
                    overwrite: '$[overwrite]',
                    localMode: '$[localMode]',
                    ignoreFailed: '$[ignoreFailed]'
            ]

    step 'cleanup', {
        condition = '''$[/javascript
          var cleanup= \'$[cleanup]\';
          (cleanup== \'true\' || cleanup== \'1\') ;
        ]'''
        alwaysRun = '1'
        command = new File(pluginDir, "dsl/procedures/$procName/steps/cleanup.groovy").text
        resourceName = '$[rsrcName]'
        shell = 'ec-groovy'
      }
}