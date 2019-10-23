
widget 'AverageDeploymentDuration', {
  attributeDataType = [
    'yAxis': 'DURATION',
    'xAxis': 'DATE',
  ]
  attributePath = [
    'yAxis': 'avg_duration',
    'xAxis': 'deployment_date_label',
  ]
  linkParameter = [
    'deploymentDateMax': '${deployment_date_max_label}',
    'deploymentDateMin': '${deployment_date_min_label}',
  ]
  linkTarget = 'Deployments'
  orderIndex = '3'
  reportName = 'AverageDeploymentDuration'
  visualization = 'LINE_CHART'
  visualizationProperty = [
    'defaultColor': '#00ADEE',
  ]
}
