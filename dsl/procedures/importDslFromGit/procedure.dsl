import java.io.File

def procName = 'importDslFromGit'
procedure procName, {
    jobNameTemplate = 'import-dsl-from-git-$[jobId]'

    step 'checkoutDsl',
            subprocedure: 'CheckoutCode',
            subproject:'/plugins/ECSCM-Git/project',
            resourceName: '$[rsrcName]',
            errorHandling: 'abortProcedure',
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
            errorHandling: 'abortProcedure',
            actualParameter: [
                    additionalDslArguments: '$[additionalDslArguments]',
                    directory: '$[/javascript if (myCall.relPath) { myCall.dest + \'/\' + myCall.relPath;} else {myCall.dest;}]',
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