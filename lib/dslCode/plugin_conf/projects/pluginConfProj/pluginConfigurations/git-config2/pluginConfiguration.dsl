pluginConfiguration 'git-config2', {
    credentialReferenceParameter = [
      'token_credential': '/projects/pluginConfProj/credentials/test-cred',
    ]
    field = [
      'authType': 'token',
      'checkConnectionResource': 'local',
      'debugLevel': '0',
      'library': 'jgit',
      'repositoryURL': 'https://github.com/itykhan/gitsync.git',
      'token_credential': 'token_credential',
    ]
    pluginKey = 'EC-Git'
  }