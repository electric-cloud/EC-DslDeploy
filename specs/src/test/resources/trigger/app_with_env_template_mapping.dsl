serviceAccount 'serviceAccount'

project 'environmentProject',{
  description = 'This is another environment project'
}

project 'applicationProject', {

  environmentTemplate 'testEnvTemplate', {

    environmentTemplateTier 'testEnvTemplateTier', {
      addResourceToEnvironmentTemplateTier(resourceName: 'local')
    }
  }

  application 'testApp', {

    applicationTier 'testAppTier', {
      applicationName = 'testApp'
      projectName = 'applicationProject'
    }

    environmentTemplateTierMap 'b94be670-cc4f-11e5-9a91-34e6d73cb28d', {
      applicationName = 'testApp'
      environmentProjectName = 'applicationProject'
      environmentTemplateName = 'testEnvTemplate'
      projectName = 'applicationProject'
      tierMapping = ['testAppTier': 'testEnvTemplateTier']
    }

    process 'testApp_process', {
      applicationName = 'testApp'
      processType = 'OTHER'

      processStep 'command_step', {
        applicationTierName = 'testAppTier'
        errorHandling = 'abortJob'
        processStepType = 'plugin'
        subprocedure = 'RunCommand'
        subproject = '/plugins/EC-Core/project'
        actualParameter 'commandToRun', 'echo test'
      }
    }

    trigger 'app-webhook', {
      accessTokenPublicId = 'ieqfpqgixfmfrajtswxdk1swwp11dx'
      triggerType='webhook'
      pluginKey = 'webhook-plugin'
      pluginParameter = ['pushEvent': 'true']
      serviceAccountName = 'serviceAccount'
      processName='testApp_process'
      environmentName = 'test_env_1'
      environmentTemplateName = 'testEnvTemplate'
      environmentTemplateProjectName = 'applicationProject'
      actualParameter 'param1', 'paramValue'
      tierResourceCount = ['testEnvTemplateTier': '1']
    }

  }
}
