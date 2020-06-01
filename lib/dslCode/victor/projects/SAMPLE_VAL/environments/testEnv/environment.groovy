environment 'testEnv', {
  description = 'val'
  tags = ["arg1", "arg2"]
  environmentEnabled = '1'
  reservationRequired = '0'
  rollingDeployEnabled = null
  rollingDeployType = null

  environmentTier 'Tier 1', {
    batchSize = null
    batchSizeType = null
    resourceName = [
      'local',
    ]
  }
}