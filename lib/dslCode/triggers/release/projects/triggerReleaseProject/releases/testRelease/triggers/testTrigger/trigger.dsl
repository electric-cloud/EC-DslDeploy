
trigger 'testTrigger', {
  actualParameter = [
      'param1': 'actParam',
  ]
  pipelineParameter = [
    'param2': 'entryParam',
  ]
  pluginKey = 'webhook-plugin'
  pluginParameter = [
    'pushEvent': 'true',
  ]
  projectName = 'triggerReleaseProject'
  quietTimeMinutes = '0'
  releaseName = 'testRelease'
  runDuplicates = '0'
  serviceAccountName = 'trigger_sa'
  triggerType = 'webhook'
}
