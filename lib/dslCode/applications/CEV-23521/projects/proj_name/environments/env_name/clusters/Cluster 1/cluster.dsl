
cluster 'Cluster 1', {
  pluginKey = 'EC-AzureContainerService'
  provisionParameter = [
          'adminUsername': 'admin',
          'agentPoolCount': 'v7',
          'agentPoolDnsPrefix': 'v8',
          'agentPoolName': 'v6',
          'agentPoolVmsize': 'Standard_D2',
          'clusterName': 'v2',
          'clusterWaitime': 'v9',
          'config': 'v1',
          'masterCount': '1',
          'masterDnsPrefix': 'v4',
          'masterFqdn': 'v5',
          'masterVmsize': 'Standard_D2',
          'masterZone': 'v10',
          'orchestratorType': 'kubernetes',
          'resourceGroupName': 'v3',
  ]
  provisionProcedure = 'Provision Cluster'
}
