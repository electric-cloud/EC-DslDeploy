import java.io.File

def procName = 'importDslFromGit'
procedure procName, {
    jobNameTemplate = 'import-dsl-from-git-$[jobId]'

    step 'checkoutDslClone', {
        subprocedure = 'Clone'
        subproject = '/plugins/EC-Git/project'
        resourceName = '$[rsrcName]'
        errorHandling = 'abortProcedure'
        actualParameter = [
            commit: '$[commit]',
            config: '$[config]',
            depth: '$[depth]',
            repoUrl: '$[GitRepo]',
            branch: '$[GitBranch]',
            gitRepoFolder: '$[dest]',
            overwrite: '$[GitOverwrite]',
            tag: '$[tag]'
        ]
        condition = '$[/javascript myJob.clone == "1" || myJob.clone == "true"]'
    }

    step 'checkoutDslPull', {
        subprocedure = 'Pull'
        subproject = '/plugins/EC-Git/project'
        resourceName = '$[rsrcName]'
        errorHandling =  'abortProcedure'
        actualParameter = [
            config: '$[config]',
            branch: '$[GitBranch]',
            repoUrl: '$[GitRepo]',
            gitRepoFolder: '$[dest]',
        ]
        condition = '$[/javascript myJob.clone == "0" || myJob.clone == "false"]'
    }

    step 'installFromDirectory',
            subprocedure: 'installDslFromDirectory',
            errorHandling: 'abortProcedure',
            actualParameter: [
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
