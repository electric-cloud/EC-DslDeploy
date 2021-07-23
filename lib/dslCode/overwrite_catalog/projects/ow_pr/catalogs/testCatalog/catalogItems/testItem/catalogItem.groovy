
catalogItem 'testItem', {
  description = 'original description'
  buttonLabel = 'Original Label'
  dslParamForm = ''
  dslString = ''
  iconUrl = 'icon-catalog-item.svg'
  subpluginKey = 'EC-FileOps'
  subprocedure = 'ListFiles'

  formalParameter 'testParam1', defaultValue: 'value1', {
    description = 'test description'
    label = 'labe1'
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  formalParameter 'testParam2', defaultValue: 'true', {
    label = 'label2'
    orderIndex = '2'
    required = '1'
    type = 'entry'
  }
}
