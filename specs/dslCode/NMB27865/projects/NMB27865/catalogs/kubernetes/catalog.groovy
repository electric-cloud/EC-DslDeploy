catalog 'createKubernetesMicroservice',
    description: 'create microservice for application',
{
    catalogItem 'createKubernetesMicrosesrvice',
        description: '''<xml>
  <title>
     Create Kubernetes Micorservice
  </title>

  <htmlData>
    <![CDATA[

    ]]>
  </htmlData>
</xml>''',
        buttonLabel: 'Create',
        dslParamForm: null,
        dslString:'''
  def microserviceName = args.microserviceName
  service microserviceName, {
  applicationName = null
  defaultCapacity = null
  maxCapacity = null
  minCapacity = null
  volume = null

  container \'kubernetesToolsMicroservice\', {
    applicationName = null
    command = null
    cpuCount = null
    cpuLimit = null
    entryPoint = null
    imageName = \'docker.edwardjones.com/not-used\'
    imageVersion = \'1.0.80\'
    memoryLimit = null
    memorySize = null
    registryUri = null
    serviceName = microserviceName
    volumeMount = null
  }

  process \'deployApplication\', {
    applicationName = null
    processType = \'DEPLOY\'
    serviceName = microserviceName
    smartUndeployEnabled = null
    timeLimitUnits = null
    workingDirectory = null
    workspaceName = null
  }
}
