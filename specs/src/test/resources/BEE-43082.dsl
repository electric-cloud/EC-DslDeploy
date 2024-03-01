project 'BEE-43082', {
  tracked = '1'

  application 'BEE-43082', {
    applicationType = 'microservice'

    microservice 'BEE-43082', {
      definitionSource = 'git'
      definitionSourceParameter = [
        'branch': 'test',
        'config': 'test',
        'repoUrl': 'test',
      ]
      definitionType = 'helm'
      deployParameter = [
        'chart': 'test',
        'releaseName': 'test',
      ]

      process 'Deploy Microservice Process', {
        description = 'System generated process for microservice deployment'
        processType = 'DEPLOY'

        formalParameter 'p1', {
          type = 'entry'
          orderIndex = '1'
         }
        formalParameter 'p2', {
          type = 'entry'
          orderIndex = '2'
        }

        processStep 'Retrieve Artifact', {
          description = 'System generated step to retrieve microservice definition artifact'
          processStepType = 'plugin'
          subprocedure = 'Source Provider'
          subproject = '/plugins/EC-Git/project'
        }

        processStep 'Deploy Microservice', {
          description = 'System generated step to deploy microservice'
          processStepType = 'plugin'
          subprocedure = 'Deploy Service'
          subproject = '/plugins/EC-Helm/project'
        }

        processDependency 'Retrieve Artifact', targetProcessStepName: 'Deploy Microservice'
      }
    }

    process 'Deploy Application', {
      description = 'System generated process for microservice application'
      processType = 'DEPLOY'

      formalParameter 'ec_BEE-43082-run', defaultValue: '1', {
        expansionDeferred = '1'
        type = 'checkbox'
      }

      formalParameter 'ec_rolloutApprovers', {
        expansionDeferred = '1'
        type = 'assigneeList'
      }

      formalParameter 'ec_rolloutNotificationEnabled', defaultValue: '0', {
        expansionDeferred = '1'
        type = 'checkbox'
      }

      processStep 'BEE-43082', {
        description = 'System generated step to invoke microservice process'
        processStepType = 'process'
        submicroservice = 'BEE-43082'
        submicroserviceProcess = 'Deploy Microservice Process'
        useUtilityResource = '1'

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

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }
  }
}