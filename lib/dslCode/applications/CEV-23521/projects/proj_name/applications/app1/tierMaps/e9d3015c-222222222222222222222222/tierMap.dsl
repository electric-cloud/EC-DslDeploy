
tierMap 'e9d3015c-222222222222222222222222', {
  environmentName = 'env_name'
  environmentProjectName = 'proj_name'

  serviceClusterMapping 'e9d30161-1', {
    clusterName = 'Cluster 1'
    serviceName = 'service1'

    port 'mapping_port', {
      listenerPort = '333'
      serviceClusterMappingName = 'e9d30161-1'
      subcontainer = 'container1'
      subport = 'container_port'
      tierMapName = 'e9d3015c-222222222222222222222222'
    }
  }

  serviceClusterMapping 'e9d30161-2', {
    clusterName = 'Cluster 1'
    serviceName = 'service2'
  }

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
