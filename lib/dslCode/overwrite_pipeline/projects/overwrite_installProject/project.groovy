project "overwrite_installProject", {
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
