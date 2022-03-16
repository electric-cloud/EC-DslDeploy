import java.io.File

def procName = 'importDslFromGitNew'
procedure procName, {
    jobNameTemplate = 'import-dsl-from-git-$[jobId]'

    step 'Assign resource',
        command: 'ectool createProperty /myJob/usedResource --value $[/myResource/name] ',
        resourceName: '$[rsrcName]',
        errorHandling: 'abortProcedure'

    step 'Checkout changes',
            subprocedure: 'PullClone',
            subproject:'/plugins/EC-Git/project',
            resourceName: '$[/myJob/usedResource]',
            errorHandling: 'abortProcedure',
                actualParameter: [
                        config: '$[config]',
                        gitRepoFolder: '$[dest]',
                        branch: '$[branch]',
                        repoUrl: '$[repoUrl]'
                ]

    step 'Import DSL from directory',
            subprocedure: 'installDslFromDirectory',
            errorHandling: 'abortProcedure',
            actualParameter: [
                    additionalDslArguments: '$[additionalDslArguments]',
                    directory: '$[/javascript if (myCall.relPath) { myCall.dest + \'/\' + myCall.relPath;} else {myCall.dest;}]',
                    pool     : '$[/myJob/usedResource]',
                    overwrite: '$[overwrite]',
                    localMode: '$[localMode]',
                    ignoreFailed: '$[ignoreFailed]',
                    includeObjects: '''$[includeObjects]''',
                    excludeObjects: '''$[excludeObjects]''',
                    pathToFileList: '''$[pathToFileList]''',
                    propertyWithFileList: '''$[propertyWithFileList]'''
            ]

    step 'Cleanup', {
        condition = '''$[/javascript
          var cleanup= \'$[cleanup]\';
          (cleanup== \'true\' || cleanup== \'1\') ;
        ]'''
        alwaysRun = '1'
        command = new File(pluginDir, "dsl/procedures/$procName/steps/cleanup.groovy").text
        resourceName = '$[/myJob/usedResource]'
        shell = 'ec-groovy'
      }
}