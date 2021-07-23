def pipeName='p12'
pipeline pipeName, {

  description = 'val'
  tags = ["arg1", "arg2"]

  acl {
    inheriting = '1'

    aclEntry 'user', principalName: 'project: ow_pr', {
      changePermissionsPrivilege = 'allow'
      executePrivilege = 'allow'
      modifyPrivilege = 'allow'
      readPrivilege = 'allow'
    }
  }
}
