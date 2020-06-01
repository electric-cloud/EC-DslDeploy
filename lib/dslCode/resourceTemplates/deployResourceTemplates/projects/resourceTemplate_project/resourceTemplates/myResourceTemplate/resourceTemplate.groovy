
resourceTemplate 'myResourceTemplate', {
  cloudProviderParameter = [
    'connection_config': 'azureConfig',
    'create_public_ip': '0',
    'disable_password_auth': '0',
    'image': 'PROVIDE IMAGE',
    'instance_count': '1',
    'is_user_image': '1',
    'job_step_timeout': '',
    'location': 'local',
    'machine_size': 'Standard_D2_v2',
    'os_type': 'Windows',
    'public_key': '',
    'resource_group_name': 'local',
    'resource_pool': '',
    'resource_port': '',
    'resource_workspace': 'default',
    'resource_zone': '',
    'result_location': '',
    'storage_account': 'PROVIDE STORAGE ACCOUNT',
    'storage_container': 'PROVIDE STORAGE CONTAINER',
    'subnet': '',
    'vm_name': 'PROVIDE VM NAME',
    'vnet': '',
  ]
  cloudProviderPluginKey = 'EC-Azure'
  cloudProviderProcedure = 'Create VM'
}
