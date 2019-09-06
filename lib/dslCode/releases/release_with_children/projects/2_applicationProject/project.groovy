project '2_applicationProject', {
  description = 'myProject project with application which has maven components'

  environment 'myEnv2', {
    environmentEnabled = '1'
    projectName = '2_applicationProject'

    environmentTier 'myEnvTier', {
      resourceName = [
        'resource22',
      ]
    }
  }

  application 'myApp', {

    applicationTier 'AppTier', {
      description = 'This is application tier1'
      applicationName = 'myApp'
      projectName = '2_applicationProject'

      component 'maven_comp', {
        applicationName = 'myApp'
        pluginKey = 'EC-Maven'
        reference = '0'

        process 'maven_comp_process', {
          processType = 'DEPLOY'

          processStep 'maven_comp_proc_step', {
            actualParameter = [
              'artifact': '$[/myComponent/ec_content_details/artifact]',
              'classifier': '$[/myComponent/ec_content_details/classifier]',
              'config': '$[/myComponent/ec_content_details/config]',
              'directory': '$[/myComponent/ec_content_details/directory]',
              'overwrite': '$[/myComponent/ec_content_details/overwrite]',
              'repository': '$[/myComponent/ec_content_details/repository]',
              'server': '$[/myComponent/ec_content_details/server]',
              'type': '$[/myComponent/ec_content_details/type]',
              'version': '$[/myJob/ec_maven_comp-version]',
            ]
            alwaysRun = '0'
            errorHandling = 'failProcedure'
            processStepType = 'component'
            subprocedure = 'Retrieve Artifact'
            subproject = '/plugins/EC-Maven/project'
            useUtilityResource = '0'
          }
        }

        // Custom properties

        property 'ec_content_details', {

          // Custom properties

          property 'artifact', value: 'org.springframework:spring-core', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          classifier = ''
          config = ''
          directory = ''
          overwrite = '1'
          pluginProcedure = 'Retrieve Artifact'

          property 'pluginProjectName', value: 'EC-Maven', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          repository = 'maven2'
          server = 'http://repo1.maven.org'

          property 'type', value: '.jar', {
            expandable = '1'
            suppressValueTracking = '0'
          }

          property 'version', value: '', {
            expandable = '1'
            suppressValueTracking = '0'
          }
        }

        property 'ec_ui', {

          // Custom properties
          stepType = 'operation'
        }
      }
    }

    process 'myApp_process', {
      applicationName = 'myApp'
      processType = 'OTHER'

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_maven_comp-run', defaultValue: '1', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_maven_comp-version', defaultValue: '$[/projects/2_applicationProject/applications/myApp/components/maven_comp/ec_content_details/version]', {
        expansionDeferred = '1'
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_maven_component-run', defaultValue: '1', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_maven_component-version', defaultValue: '$[/projects/myProject/applications/myApp/components/maven_comp/ec_content_details/version]', {
        expansionDeferred = '1'
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_smartDeployOption', defaultValue: '1', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_stageArtifacts', defaultValue: '0', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      processStep 'download_spring_core_artifact', {
        alwaysRun = '0'
        applicationTierName = 'AppTier'
        errorHandling = 'failProcedure'
        processStepType = 'process'
        subcomponent = 'maven_comp'
        subcomponentApplicationName = 'myApp'
        subcomponentProcess = 'maven_comp_process'
        useUtilityResource = '0'

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

    tierMap 'myTierMap', {
      applicationName = 'myApp'
      environmentName = 'myEnv2'
      environmentProjectName = '2_applicationProject'
      projectName = '2_applicationProject'

      tierMapping '21c37c1d-c1ed-11e9-b1a1-68f728538d1f', {
        applicationTierName = 'AppTier'
        environmentTierName = 'myEnvTier'
        tierMapName = 'myTierMap'
      }
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }

    property 'jobCounter', value: '2', {
      expandable = '1'
      suppressValueTracking = '1'
    }
  }
}
