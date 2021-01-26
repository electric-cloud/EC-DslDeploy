
process 'deploy', {
  exclusiveEnvironment = '0'
  processType = 'OTHER'

  formalParameter 'ec_component1-run', defaultValue: '1', {
    expansionDeferred = '1'
    type = 'checkbox'
  }

  formalParameter 'ec_component1-version', defaultValue: '$[/projects/dslTestProject/applications/app1/components/comp1/ec_content_details/versionRange]', {
    expansionDeferred = '1'
    type = 'entry'
  }

  formalParameter 'ec_enforceDependencies', defaultValue: '0', {
    expansionDeferred = '1'
    type = 'checkbox'
  }

  formalParameter 'ec_smartDeployOption', defaultValue: '1', {
    expansionDeferred = '1'
    type = 'checkbox'
  }

  formalParameter 'ec_stageArtifacts', defaultValue: '0', {
    expansionDeferred = '1'
    type = 'checkbox'
  }

  processStep 'deploy component1', {
    applicationTierName = 'T1'
    dependencyJoinType = 'and'
    processStepType = 'process'
    subcomponent = 'comp1'
    subcomponentApplicationName = 'app1'
    subcomponentProcess = 'deploy'

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }
  }
}
