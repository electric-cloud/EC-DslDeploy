project args.projectName, {

    procedure 'proc1', {

        step 's1', {
            command = 'echo test'
        }

        proc1_testProperty = 'proc1 test'
    }



   application 'app1', {

      applicationTier 'Tier 1', {
        applicationName = 'app1'

        component 'component1', {
          applicationName = 'app1'
          pluginKey = 'EC-Artifact'

          process 'component_process', {
            processType = 'DEPLOY'

            processStep 'cmd', {
              actualParameter = [
                'commandToRun': 'echo \'component process step\'',
              ]
              dependencyJoinType = 'and'
              processStepType = 'command'
              subprocedure = 'RunCommand'
              subproject = '/plugins/EC-Core/project'
            }
          }

          // Custom properties

          property 'ec_content_details', {

            // Custom properties

            property 'artifactName', value: 'test:test'
            artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
            filterList = ''
            overwrite = 'update'
            pluginProcedure = 'Retrieve'

            property 'pluginProjectName', value: 'EC-Artifact'
            retrieveToDirectory = ''

            property 'versionRange', value: ''
          }
        }
      }

      process 'app_process', {
        exclusiveEnvironment = '0'
        processType = 'OTHER'

        formalParameter 'ec_component1-run', defaultValue: '1', {
          expansionDeferred = '1'
          type = 'checkbox'
        }

        formalParameter 'ec_component1-version', defaultValue: '$[/projects/' + args.projectName + '/applications/app1/components/component1/ec_content_details/versionRange]', {
          expansionDeferred = '1'
          type = 'entry'
        }

        formalParameter 'ec_enforceDependencies', defaultValue: '0', {
          expansionDeferred = '1'
          type = 'checkbox'
        }

        formalParameter 'ec_smartDeployOption', defaultValue: '1', {
          expansionDeferred = '1'
          type = 'checkbox'
        }

        formalParameter 'ec_stageArtifacts', defaultValue: '0', {
          expansionDeferred = '1'
          type = 'checkbox'
        }

        processStep 's1', {
          applicationTierName = 'Tier 1'
          dependencyJoinType = 'and'
          processStepType = 'process'
          subcomponent = 'component1'
          subcomponentApplicationName = 'app1'
          subcomponentProcess = 'component_process'

          // Custom properties

          property 'ec_deploy', {

            // Custom properties
            ec_notifierStatus = '0'
          }
        }

        processStep 'cmd', {
          applicationTierName = 'Tier 1'
          actualParameter = [
            'commandToRun': 'print \'application process step\'',
            'shellToUse': 'ec-groovy',
          ]
          dependencyJoinType = 'and'
          processStepType = 'command'
          subprocedure = 'RunCommand'
          subproject = '/plugins/EC-Core/project'

          // Custom properties

          property 'ec_deploy', {

            // Custom properties
            ec_notifierStatus = '0'
          }
        }

        processDependency 's1', targetProcessStepName: 'cmd'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }


    pipeline 'pipeline1', {
        stage 'stage1', {

            task 'cmd', {
                actualParameter = [
                    'commandToRun': 'echo test',
                ]
                subpluginKey = 'EC-Core'
                subprocedure = 'RunCommand'
                taskType = 'COMMAND'

                task_testProperty = 'test task'
            }

            stage_testProperty = 'test stage'

        }

        pipeline_testProperty = 'test pipeline'
    }

  // Custom properties

  property 'testSheet', {

    // Custom properties
    testProperty = 'test project'
  }

}
