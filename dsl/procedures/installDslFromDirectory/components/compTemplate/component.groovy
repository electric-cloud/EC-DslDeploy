["comp1", "comp2", "comp3"] .each { compName ->
  component "UC4CompTemplate_$compName", {
    applicationName = null
    pluginKey = 'EC-Artifact'
    reference = '0'
    sourceComponentName = null
    sourceProjectName = null

    process 'deploy', {
      processType = 'DEPLOY'

      processStep 'retrieve', {
        applicationTierName = null
        errorHandling = 'abortJob'
        processStepType = 'component'
        subprocedure = 'Retrieve'
        subproject = '/plugins/EC-Artifact/project'
        actualParameter = [
          'artifactName': '$[/myComponent/ec_content_details/artifactName]',
          'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
          'filterList': '$[/myComponent/ec_content_details/filterList]',
          'overwrite': '$[/myComponent/ec_content_details/overwrite]',
          'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
          'versionRange': '$[/myJob/ec_compTemplate-version]',
        ]
      }

       processStep 'deployStep',
         applicationTierName: null,
         processStepType: 'command',
         subprocedure: 'RunCommand',
         subproject: '/plugins/EC-Core/project',
         actualParameter: [
            'commandToRun': '''echo "Deploying App $[/myApplication/applicationName] ''' + compName + '''"
             sleep 5'''
         ]

       processDependency 'Retrieve',
         targetProcessStepName: 'deployStep',
         branchType: 'ALWAYS'

     } // process


    // Custom properties
    property 'ec_content_details', {
      property 'artifactName', value: 'EJones:$[/myApplication/applicationName]_' + compName, {
        expandable = '1'
      }
      artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
      filterList = ''
      overwrite = 'update'
      pluginProcedure = 'Retrieve'

      property 'pluginProjectName', value: 'EC-Artifact', {
        expandable = '1'
      }
      retrieveToDirectory = ''

      property 'versionRange', value: null, {
        expandable = '1'
      }
    }   // ec_content_details
  }     // component
}       // compName loop
