
container 'container2', {
  imageName = 'container2'

  environmentVariable 'env_var_name', {
    value = 'v'
  }

  port 'container_port', {
    containerPort = '111'
  }
}
