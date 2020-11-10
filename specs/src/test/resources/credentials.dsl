project args.project, {

  credentialProvider 'testCredProvider', {
    providerType = 'HASHICORP'
    secretEnginePath = '/path'
    secretEngineType = 'KV2'
    serverUrl = 'http://localhost:1234'
  }

  credential 'externalCred', userName: 'testUser', {
    credentialProviderName = 'testCredProvider'
    credentialProviderProjectName = args.project
    credentialType = 'EXTERNAL'
    secretPath = 'db/secret'
  }

  credential 'localCred', userName: 'localUser', {
    credentialType = 'LOCAL'
  }
}