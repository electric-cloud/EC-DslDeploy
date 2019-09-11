
process 'deploy', {
  processType = 'DEPLOY'

  processStep 'retrieve', {
    actualParameter = [
      'artifactName': '$[/myComponent/ec_content_details/artifactName]',
      'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
      'filterList': '$[/myComponent/ec_content_details/filterList]',
      'overwrite': '$[/myComponent/ec_content_details/overwrite]',
      'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
      'versionRange': '$[/myJob/ec_component1-version]',
    ]
    dependencyJoinType = 'and'
    processStepType = 'component'
    subprocedure = 'Retrieve'
    subproject = '/plugins/EC-Artifact/project'
  }
}
