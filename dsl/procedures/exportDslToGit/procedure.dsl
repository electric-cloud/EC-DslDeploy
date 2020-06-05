import java.io.File

def procName = 'exportDslToGit'
procedure procName, {
    jobNameTemplate = 'export-dsl-to-git-$[jobId]'

    step 'checkoutDsl',
            subprocedure: 'CheckoutCode',
            subproject:'/plugins/ECSCM-Git/project',
            resourceName: '$[rsrcName]',
            errorHandling: 'abortProcedure',
                actualParameter: [
                        clone: 'true',
                        commit: 'HEAD',
                        config: '$[checkoutConfig]',
                        depth: '1',
                        dest: '$[dest]',
                        GitBranch: '$[GitBranch]',
                        GitRepo: '$[GitRepo]',
                        overwrite: '$[GitOverwrite]',
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
                    authorEmail: '$[authorEmail]',
                    authorName: '$[authorName]',
                    committerEmail: '$[committerEmail]',
                    committerName: '$[committerName]',
                    config: '$[commitConfig]',
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