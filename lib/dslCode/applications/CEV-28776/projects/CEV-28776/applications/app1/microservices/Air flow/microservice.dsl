
microservice 'Air flow', {
  applicationName = 'app1'
  definitionSource = 'helm_repository'
  definitionSourceParameter = [
    'repositoryName': 'bitnami',
    'repositoryUrl': 'https://charts.bitnami.com/bitnami',
  ]
  definitionType = 'helm'
  deployParameter = [
    'chart': 'bitnami/airflow',
    'releaseName': 'my-release',
    'values': 'nodeSelector: {}',
  ]
  projectName = 'CEV-28776'
  rollbackParameter = [
    'rollbackEnabled': 'false',
    'waitTimeout': '300',
  ]
}
