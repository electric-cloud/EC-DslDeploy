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
    reportObjectTypeName = null
    required = '1'
    type = 'DATE'
    widgetName = null
  }

  widget 'testWidget', {
    description = ''
    dashboardName = 'testDashbord'
    iconUrl = null
    linkTarget = null
    orderIndex = '1'
    phase = null
    reportName = null
    reportProjectName = null
    section = null
    title = null
    visualization = null
  }
}