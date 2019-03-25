dashboard 'testDashbord', {
  description = 'val'
  tags = ["arg1", "arg2"]
  layout = 'FLOW'
  type = 'STANDARD'

  reportingFilter 'DateFilter', {
    dashboardName = 'testDashbord'
    operator = 'BETWEEN'
    orderIndex = '1'
    parameterName = '@timestamp'
    projectName = 'Default'
    reportObjectTypeName = null
    required = '1'
    type = 'DATE'
    widgetName = null
  }
}