reportObjectType 'rot', {
  displayName = 'Rot'

  reportObjectAssociation 'pipeline', {
    required = '0'
    sourceFields = 'name'
    targetFields = 'id'
  }

  reportObjectAttribute 'roa', {
    displayName = 'Roa'
  }
}
