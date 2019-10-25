def pipeName='p12'
pipeline pipeName, {

  description = 'val'
  tags = ["arg1", "arg2"]

  acl {
    inheriting = '1'

    aclEntry 'user', principalName: 'project: overwrite_installProject', {
      changePermissionsPrivilege = 'allow'
      executePrivilege = 'allow'
      modifyPrivilege = 'allow'
      readPrivilege = 'allow'
    }
  }
}
