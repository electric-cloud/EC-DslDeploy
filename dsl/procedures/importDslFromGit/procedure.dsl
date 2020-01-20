import java.io.File

def procName = 'importDslFromGit'
procedure procName, {
    jobNameTemplate = 'import-dsl-from-git-$[jobId]'

    step 'assignResource',
        command: 'ectool setProperty /myJob/assignedResource $[/myResource]',
        resourceName: '$[pool]'

    step 'checkoutDsl',
            subprocedure: 'CheckoutCode',
            subproject:'/plugins/ECSCM-Git/project',
            resourceName: '$[pool]',
                actualParameter: [
                        clone: '$[clone]',
                        commit: '$[commit]',
                        config: '$[config]',
                        depth: '$[depth]',
                        dest: '$[/javascript if(\'$[dest]\'.trim() != \'\') {\'$[dest]\'} else {\'dsl\'}]',
                        GitBranch: '$[GitBranch]',
                        GitRepo: '$[GitRepo]',
                        overwrite: '$[GitOverwrite]',
                        tag: '$[tag]'
                ]

    step 'installFromDirectory',
            subprocedure: 'installDslFromDirectory',
            actualParameter: [
                    directory: '$[/javascript if(\'$[dest]\'.trim() != \'\') {\'$[dest]\'} else {\'dsl\'}]',
                    pool     : '$[pool]',
                    overwrite: '$[overwrite]'
            ]

    step 'cleanup', {
        condition = '''$[/javascript
          var cleanup= \'$[cleanup]\';
          (cleanup== \'true\' || cleanup== \'1\') ;
        ]'''
        alwaysRun = '1'
        command = new File(pluginDir, "dsl/procedures/$procName/steps/cleanup.groovy").text
        resourceName = '$[pool]'
        shell = 'ec-groovy'
      }
}