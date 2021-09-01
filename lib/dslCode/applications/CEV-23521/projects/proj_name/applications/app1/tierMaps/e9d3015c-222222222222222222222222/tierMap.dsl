
tierMap 'e9d3015c-222222222222222222222222', {
  environmentName = 'env_name'
  environmentProjectName = 'proj_name'

  tierMapping 'e9d3015d-4c9a1', {
    applicationTierName = 'tier1'
    environmentTierName = 'tier1'
    tierMapName = 'e9d3015c-222222222222222222222222'
  }

  tierMapping 'e9d3015d-4c9a2', {
    applicationTierName = 'tier2'
    environmentTierName = 'tier1'
    tierMapName = 'e9d3015c-222222222222222222222222'
  }
}
