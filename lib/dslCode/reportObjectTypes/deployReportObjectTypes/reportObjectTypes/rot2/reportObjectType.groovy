reportObjectType 'rot2', {
  displayName = 'Rot2'

  reportObjectAssociation 'pipeline', {
    required = '0'
    sourceFields = 'name'
    targetFields = 'id'
  }

  reportObjectAttribute 'roa2', {
    displayName = 'Roa2'
  }
}
