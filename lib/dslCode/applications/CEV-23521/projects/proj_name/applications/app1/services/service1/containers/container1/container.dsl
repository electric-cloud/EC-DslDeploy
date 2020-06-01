
container 'container1', {
  imageName = 'container1'

  environmentVariable 'env_var_name', {
    value = 'v'
  }

  port 'container_port', {
    containerPort = '111'
  }
}
