import java.io.File

def procName = 'exportDslToGit'
procedure procName, {
    jobNameTemplate = 'export-dsl-to-git-$[jobId]'

    step 'Clone',
                subprocedure: 'Clone',
                subproject: '/plugins/EC-Git/project',
                condition: '$[/myJob/actualParameters/clone]',
                errorHandling:'abortProcedure',
                resourceName: '$[rsrcName]',
                    actualParameter: [
                            branch: '$[branch]',
                            config: '$[gitConfig]',
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

    step 'generateDslToDirectory',
            subprocedure: 'generateDslToDirectory',
            errorHandling: 'abortProcedure',
                        actualParameter: [
                        directory: '$[dest]',
                        objectType: '$[objectType]',
                        objectName: '$[objectName]',
                        pool: '$[rsrcName]',
                        suppressNulls: '$[suppressNulls]',
                        suppressDefaults: '$[suppressDefaults]',
                        suppressParent: '$[suppressParent]',
                        includeAcls: '$[includeAcls]',
                        includeAclsInDifferentFile: '$[includeAclsInDifferentFile]',
                        includeAllChildren: '$[includeAllChildren]',
                        includeChildren: '$[includeChildren]',
                        includeChildrenInSameFile: '$[includeChildrenInSameFile]',
                        childrenInDifferentFile: '$[childrenInDifferentFile]'
                        ]

    step 'exportDslToGit',
            subprocedure: 'Commit',
            subproject:'/plugins/EC-Git/project',
            resourceName: '$[rsrcName]',
            errorHandling: 'abortProcedure',
            actualParameter: [
                    committerEmail: '$[committerEmail]',
                    committerName: '$[committerName]',
                    config: '$[gitConfig]',
                    failOnEmptyCommit: '$[failOnEmptyCommit]',
                    files: '$[files]',
                    gitRepoFolder: '$[dest]',
                    message: '$[message]',
                    push: 'true',
                    removeMissing: '$[removeMissing]'
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