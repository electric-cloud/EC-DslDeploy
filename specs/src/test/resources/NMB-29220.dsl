project args.projectName, {

  environment 'pvEnvironment', {
    environmentEnabled = '1'
    projectName = args.projectName
    reservationRequired = '0'
    rollingDeployEnabled = null
    rollingDeployType = null

    environmentTier 'Tier 1', {
      resourceName = [
        'local',
      ]
    }
  }

  application args.appName, {
    description = ''

    applicationTier 'Tier 1', {
      applicationName = args.appName
      projectName = args.projectName

      component 'pvNativeJenkinsTestComponent01', pluginName: null, {
        applicationName = args.appName
        pluginKey = 'EC-Artifact'
        reference = '0'

        process 'DeployComponentProcess', {
          processType = 'DEPLOY'
          smartUndeployEnabled = '0'
          timeLimitUnits = 'minutes'
          workspaceName = 'default'
        }
      }
    }

    tierMap args.tierMapName, {
      applicationName = args.appName
      environmentName = 'pvEnvironment'
      environmentProjectName = args.projectName
      projectName = args.projectName

      tierMapping '4af5de3f-7f11-11ea-857c-0242ac140003', {
        applicationTierName = 'Tier 1'
        environmentTierName = 'Tier 1'
        resourceExpression = null
        tierMapName = args.tierMapName
      }
    }
  }
}
//Create Users
user 'user1', {
    userName = 'user1'
    fullUserName = 'Allowed to project'
    password = 'default'
    sessionPassword = args.sessionPassword
}

user 'slave', {
    userName = 'slave'
    fullUserName = 'Not allowed to projects'
    password = 'default'
    sessionPassword = args.sessionPassword
}


//Create ACLs for projects and Plugins
createAclEntry([principalType :'user',  principalName :'slave', projectName:args.projectName, changePermissionsPrivilege: 'deny',executePrivilege: 'deny',modifyPrivilege: 'deny',readPrivilege: 'deny'])
createAclEntry([principalType :'user',  principalName :'user1', projectName:args.projectName ,changePermissionsPrivilege: 'allow',executePrivilege: 'allow',modifyPrivilege: 'allow',readPrivilege: 'allow'])
