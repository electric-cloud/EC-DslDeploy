application 'testApp', {
  description = 'val'
  tags = ["arg1", "arg2"]

  applicationTier 'Tier 1', {
    applicationName = 'testApp'

    component 'testComp', pluginName: null, {
      applicationName = 'testApp'
      pluginKey = 'EC-Artifact'
      reference = '0'
      sourceComponentName = null
      sourceProjectName = null

      process 'testProc', {
        applicationName = null
        processType = 'DEPLOY'
        serviceName = null
        smartUndeployEnabled = null
        timeLimitUnits = null
        workingDirectory = null
        workspaceName = null

        processStep 'testPS', {
          actualParameter = [
            'commandToRun': 'echo hello',
          ]
          afterLastRetry = null
          alwaysRun = '0'
          applicationTierName = null
          componentRollback = null
          dependencyJoinType = 'and'
          emailConfigName = null
          errorHandling = 'abortJob'
          instruction = null
          notificationEnabled = null
          notificationTemplate = null
          processStepType = 'command'
          retryCount = null
          retryInterval = null
          retryType = null
          rollbackSnapshot = null
          rollbackType = null
          rollbackUndeployProcess = null
          skipRollbackIfUndeployFails = null
          smartRollback = null
          subcomponent = null
          subcomponentApplicationName = null
          subcomponentProcess = null
          subprocedure = 'RunCommand'
          subproject = '/plugins/EC-Core/project'
          subservice = null
          subserviceProcess = null
          timeLimitUnits = null
          useUtilityResource = '0'
          utilityResourceName = null
          workingDirectory = null
          workspaceName = null
        }
      }

      // Custom properties

      property 'ec_content_details', {

        // Custom properties

        property 'artifactName', value: 'testGroup:testArtKey', {
          expandable = '1'
          suppressValueTracking = '0'
        }
        artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
        filterList = ''
        overwrite = 'update'
        pluginProcedure = 'Retrieve'

        property 'pluginProjectName', value: 'EC-Artifact', {
          expandable = '1'
          suppressValueTracking = '0'
        }
        retrieveToDirectory = ''

        property 'versionRange', value: '', {
          expandable = '1'
          suppressValueTracking = '0'
        }
      }
    }
  }

  process 'testAP', {
    applicationName = 'testApp'
    processType = 'OTHER'
    serviceName = null
    smartUndeployEnabled = null
    timeLimitUnits = null
    workingDirectory = null
    workspaceName = null

    formalParameter 'ec_enforceDependencies', defaultValue: '0', {
      checkedValue = null
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
      uncheckedValue = null
    }

    processStep 'testAppPS', {
      actualParameter = [
        'commandToRun': 'echo hello',
      ]
      afterLastRetry = null
      alwaysRun = '0'
      applicationTierName = 'Tier 1'
      componentRollback = null
      dependencyJoinType = 'and'
      emailConfigName = null
      errorHandling = 'abortJob'
      instruction = null
      notificationEnabled = null
      notificationTemplate = null
      processStepType = 'command'
      retryCount = null
      retryInterval = null
      retryType = null
      rollbackSnapshot = null
      rollbackType = null
      rollbackUndeployProcess = null
      skipRollbackIfUndeployFails = null
      smartRollback = null
      subcomponent = null
      subcomponentApplicationName = null
      subcomponentProcess = null
      subprocedure = 'RunCommand'
      subproject = '/plugins/EC-Core/project'
      subservice = null
      subserviceProcess = null
      timeLimitUnits = null
      useUtilityResource = '0'
      utilityResourceName = null
      workingDirectory = null
      workspaceName = null

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

  tierMap 'f7598c32-4bdc-11e9-aefb-0050563309b8', {
    applicationName = 'testApp'
    environmentName = 'testEnv'
    environmentProjectName = 'SAMPLE_VAL'

    tierMapping '043db7fc-4bdd-11e9-aefb-0050563309b8', {
      applicationTierName = 'Tier 1'
      environmentTierName = 'Tier 1'
      resourceExpression = null
      tierMapName = 'f7598c32-4bdc-11e9-aefb-0050563309b8'
    }
  }

  // Custom properties

  property 'ec_deploy', {

    // Custom properties
    ec_notifierStatus = '0'
  }
  jobCounter = '1'
}
