dashboard 'testDashboard', {
  description = 'val'
  tags = ["arg1", "arg2"]
  layout = 'FLOW'
  type = 'STANDARD'

  reportingFilter 'DateFilter', {
    operator = 'BETWEEN'
    orderIndex = '1'
    parameterName = '@timestamp'
    reportObjectTypeName = null
    required = '1'
    type = 'DATE'
    widgetName = null
  }
}
