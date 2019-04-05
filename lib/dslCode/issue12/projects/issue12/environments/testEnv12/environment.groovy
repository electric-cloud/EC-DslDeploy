environment 'testEnv12', {
  description = 'val12'
  tags = ["arg1", "arg2"]
  environmentEnabled = '1'
  reservationRequired = '0'
  rollingDeployEnabled = null
  rollingDeployType = null

  environmentTier 'Tier 12', {
    batchSize = null
    batchSizeType = null
    resourceName = [
      'res12',
    ]
  }
}
