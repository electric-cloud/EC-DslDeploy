project '3_serviceProject', {

  environment 'myEnv', {
    environmentEnabled = '1'
    projectName = '3_serviceProject'

    cluster 'gc-cluster', {
      environmentName = 'myEnv'
      pluginProjectName = '1_gce'
      providerClusterName = 'does-poc'
      providerProjectName = 'microservices-poc-143218'
      provisionParameter = [
        'clusterName': 'does-poc',
        'clusterProjectID': 'microservices-poc-143218',
        'config': 'gce_test_config',
        'diskSize': '10',
        'enableAutoscaling': '1',
        'imageType': 'CONTAINER_VM',
        'machineType': 'n1-standard-1',
        'masterZone': 'us-west1-a',
        'maxNodeCount': '5',
        'minNodeCount': '3',
        'network': 'default',
        'nodePoolName': 'default-pool',
        'nodePoolSize': '3',
      ]
      provisionProcedure = 'Provision Cluster'

      // Custom properties

      property 'ec_provision_parameter', {

        // Custom properties

        property 'clusterName', value: 'does-poc', {
          expandable = '1'
          suppressValueTracking = '0'
        }
        clusterProjectID = 'microservices-poc-143218'
        config = 'gce_test_config'
        diskSize = '10'
        enableAutoscaling = '1'
        imageType = 'CONTAINER_VM'
        machineType = 'n1-standard-1'
        masterZone = 'us-west1-a'
        maxNodeCount = '5'
        minNodeCount = '3'
        network = 'default'
        nodePoolName = 'default-pool'
        nodePoolSize = '3'
      }
    }

    cluster 'gc2-cluster', {
      environmentName = 'myEnv'
      pluginProjectName = '1_gce'
      providerClusterName = 'does-poc'
      providerProjectName = 'microservices-poc-143218'
      provisionParameter = [
        'clusterName': 'does-poc',
        'clusterProjectID': 'microservices-poc-143218',
        'config': 'gce_test_config',
        'diskSize': '10',
        'enableAutoscaling': '1',
        'imageType': 'CONTAINER_VM',
        'machineType': 'n1-standard-1',
        'masterZone': 'us-west1-a',
        'maxNodeCount': '5',
        'minNodeCount': '3',
        'network': 'default',
        'nodePoolName': 'default-pool',
        'nodePoolSize': '3',
      ]
      provisionProcedure = 'Provision Cluster'

      // Custom properties

      property 'ec_provision_parameter', {

        // Custom properties

        property 'clusterName', value: 'does-poc', {
          expandable = '1'
          suppressValueTracking = '0'
        }
        clusterProjectID = 'microservices-poc-143218'
        config = 'gce_test_config'
        diskSize = '10'
        enableAutoscaling = '1'
        imageType = 'CONTAINER_VM'
        machineType = 'n1-standard-1'
        masterZone = 'us-west1-a'
        maxNodeCount = '5'
        minNodeCount = '3'
        network = 'default'
        nodePoolName = 'default-pool'
        nodePoolSize = '3'
      }
    }

    environmentTier 'myEnvTier', {
      resourceName = [
        'resource12',
      ]
    }

    // Custom properties
    service_url = 'store-backend'
  }

  procedure 'runCmdProcedure', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    formalParameter 'procedureCmd', defaultValue: '', {
      description = ''
      expansionDeferred = '0'
      required = '1'
      type = 'entry'
    }

    step 'step1', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '$[procedureCmd]'
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = ''
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'echoParam', {

          // Custom properties
          formType = 'standard'
        }

        property 'procedureCmd', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
  }

  service 'testService', {
    defaultCapacity = '1'
    maxCapacity = '3'
    minCapacity = '1'

    container 'logapp', {
      cpuCount = '1'
      cpuLimit = '2'
      imageName = 'nitinp9/logging-nodejs'
      imageVersion = 'v1.0'
      memorySize = '2048'
      serviceName = 'testService'

      environmentVariable 'SERVICE_PORT', {
        type = 'string'
        value = '8090'
      }

      environmentVariable 'SERVICE_URL', {
        type = 'string'
        value = '$[service_url]'
      }

      port 'p3', {
        containerName = 'logapp'
        containerPort = '5000'
        projectName = '3_serviceProject'
        serviceName = 'testService'
      }
    }

    container 'nodejsapp', {
      cpuCount = '1'
      cpuLimit = '2'
      imageName = 'nitinp9/bike-nodejs'
      imageVersion = 'v1.0'
      memorySize = '2048'
      serviceName = 'testService'

      environmentVariable 'SERVICE_PORT', {
        type = 'string'
        value = '8080'
      }

      environmentVariable 'SERVICE_URL', {
        type = 'string'
        value = '$[service_url]'
      }

      port 'p2', {
        containerName = 'nodejsapp'
        containerPort = '5000'
        projectName = '3_serviceProject'
        serviceName = 'testService'
      }
    }

    environmentMap 'map1', {
      environmentName = 'myEnv'
      environmentProjectName = '3_serviceProject'
      projectName = '3_serviceProject'
      serviceName = 'testService'

      serviceClusterMapping 'fde0b215-8da9-11e7-adac-ecf4bb6fcfa0', {
        actualParameter = [
          'loadBalancerIP': '104.196.226.78',
        ]
        clusterName = 'gc-cluster'
        environmentMapName = 'map1'
        maxCapacity = '3'
        serviceName = 'testService'

        serviceMapDetail 'logapp', {
          serviceMapDetailName = '1b9db110-c1ed-11e9-a871-68f728538d1f'
          imageName = 'nitinp9/logging-nodejs2'
          imageVersion = 'v1.5'
          serviceClusterMappingName = 'fde0b215-8da9-11e7-adac-ecf4bb6fcfa0'
        }
      }
    }

    port 'sp2', {
      listenerPort = '5000'
      projectName = '3_serviceProject'
      serviceName = 'testService'
      subcontainer = 'nodejsapp'
      subport = 'p2'
    }

    port 'sp3', {
      listenerPort = '5000'
      projectName = '3_serviceProject'
      serviceName = 'testService'
      subcontainer = 'logapp'
      subport = 'p3'
    }

    process 'testService_process', {
      processType = 'DEPLOY'
      serviceName = 'testService'

      formalParameter 'procedureParam', defaultValue: '', {
        expansionDeferred = '0'
        orderIndex = '1'
        required = '1'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      processStep 'cmdStep', {
        actualParameter = [
          'commandToRun': 'echo hello',
        ]
        alwaysRun = '0'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'command'
        subprocedure = 'RunCommand'
        subproject = '/plugins/EC-Core/project'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processStep 'serviceStep', {
        alwaysRun = '0'
        errorHandling = 'abortJob'
        processStepType = 'service'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processStep 'manualStep', {
        alwaysRun = '0'
        errorHandling = 'abortJob'
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_manual_process_step_notification_template'
        processStepType = 'manual'
        useUtilityResource = '0'
        assignee = [
          'admin',
        ]

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '1'
        }
      }

      processStep 'procedureStep', {
        actualParameter = [
          'procedureCmd': '$[procedureParam]',
        ]
        alwaysRun = '0'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'procedure'
        subprocedure = 'runCmdProcedure'
        subproject = '3_serviceProject'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processDependency 'cmdStep', targetProcessStepName: 'serviceStep', {
        branchType = 'ALWAYS'
      }

      processDependency 'serviceStep', targetProcessStepName: 'manualStep', {
        branchType = 'ALWAYS'
      }

      processDependency 'manualStep', targetProcessStepName: 'procedureStep', {
        branchType = 'ALWAYS'
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    process 'testService_undeploy', {
      processType = 'UNDEPLOY'
      serviceName = 'testService'

      formalParameter 'procedureParam', {
        expansionDeferred = '0'
        orderIndex = '1'
        required = '1'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      processStep 'serviceStep', {
        alwaysRun = '0'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'service'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processStep 'procedureStep', {
        actualParameter = [
          'procedureCmd': '$[procedureParam]',
        ]
        alwaysRun = '0'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'procedure'
        subprocedure = 'runCmdProcedure'
        subproject = '3_serviceProject'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processDependency 'serviceStep', targetProcessStepName: 'procedureStep', {
        branchType = 'ALWAYS'
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

    property 'sourceProjectName', value: '', {
      expandable = '1'
      suppressValueTracking = '0'
    }
    sourceServiceName = ''
  }
}
