
environment 'e1', {
  projectName = 'ow_pr'
  rollingDeployEnabled = '1'
  rollingDeployType = 'phase'

  rollingDeployPhase 'ph1', {
    orderIndex = '1'
  }

  environmentTier 't1'

  utilityResource 'u1'
}
