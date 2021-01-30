
widget 'df', {
  description = 'Breakdown of deployments by outcome over time'
  attributeDataType = [
    'yAxis': 'NUMBER',
    'xAxis': 'DATE',
    'groups': 'STRING',
  ]
  attributePath = [
    'yAxis': 'deployment_outcome_count',
    'xAxis': 'deployment_date_label',
    'groups': 'deployment_outcome',
  ]
  color = [
    'rollback': '#567b99',
    'aborted': '#808080',
    'success': '#70b723',
    'warning': '#DA833E',
    'error': '#eb1c24',
  ]
  linkParameter = [
    'deploymentDateMax': '${deployment_date_max_label}',
    'deploymentDateMin': '${deployment_date_min_label}',
  ]
  linkTarget = 'Deployments'
  orderIndex = '3'
  reportName = 'df'
  visualization = 'STACKED_AREA_CHART'
}
