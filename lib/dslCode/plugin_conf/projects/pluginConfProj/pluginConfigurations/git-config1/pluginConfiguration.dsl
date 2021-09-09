pluginConfiguration 'git-config1', {
    field = [
      'authType': 'token',
      'checkConnectionResource': 'local',
      'debugLevel': '0',
      'library': 'jgit',
      'repositoryURL': 'https://github.com/itykhan/gitsync.git',
      'token_credential': 'token_credential',
    ]
    pluginKey = 'EC-Git'

    addCredential 'token_credential', {
      passwordRecoveryAllowed = '1'
    }
  }