component 'master12', {
  applicationName = null
  pluginKey = 'EC-Artifact'
  reference = '0'
  sourceComponentName = null
  sourceProjectName = null

  // Custom properties

  property 'ec_content_details', {

    // Custom properties

    property 'artifactName', value: 'com.ec:usreportal', {
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
    retrieveToDirectory = '/tmp'

    property 'versionRange', value: '', {
      expandable = '1'
      suppressValueTracking = '0'
    }
  }
}
