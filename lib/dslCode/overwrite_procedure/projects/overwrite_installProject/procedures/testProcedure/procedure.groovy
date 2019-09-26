
procedure 'testProcedure', {
  description = 'testProcedure'
  jobNameTemplate = 'testJobTemplateName'

  formalOutputParameter 'testOutputParam'

  formalParameter 'testParameter', defaultValue: 'default_value', {
    type = 'entry'
  }
}
