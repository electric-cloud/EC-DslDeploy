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
            description: """
                EC-DslDeploy spec tests rely on the value of [Run]condition.
                Do not change [Run]condition without successfully running those tests.
            """,
            condition: '$[/javascript (getProperty("repoUrl")!="skip_checkout_changes_step_please");]',
            errorHandling: 'abortProcedure',
                actualParameter: [
                        config: '$[config]',
                        gitRepoFolder: '$[dest]',
                        branch: '$[branch]',
                        repoUrl: '$[repoUrl]',
                        propertyWithFileList: '$[/javascript if (myJob.incrementalImport == "1" || myJobStep.incrementalImport == "1" || myJobStep.parent.incrementalImport == "1") { "/myJob/change_list.json";}]'
                ]

    step 'renameObjectsInChangeList', {
        description = 'Issue rename commands based on the change list data'
        alwaysRun = '0'
        broadcast = '0'
        command = new File(pluginDir, "dsl/procedures/$procName/steps/renameObjectsInChangeList.groovy").text
        resourceName = '$[/myJob/usedResource]'
        shell = 'ec-groovy'
    }

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
                    excludeObjects: '''$[excludeObjects]'''
            ]

    step 'deleteObjectsInChangeList', {
        description = 'Issue delete commands based on the change list data'
        alwaysRun = '0'
        broadcast = '0'
        command = new File(pluginDir, "dsl/procedures/$procName/steps/deleteObjectsInChangeList.groovy").text
        resourceName = '$[/myJob/usedResource]'
        shell = 'ec-groovy'
    }


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