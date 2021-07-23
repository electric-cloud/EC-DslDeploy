project "ow_pr", {
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
