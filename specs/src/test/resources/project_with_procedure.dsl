user 'limited_user', {
  email = 'xyz@electric-cloud.com'
  fullUserName = 'user'
  password = 'changeme'
  sessionPassword = args.sessionPassword
}

project args.projectName, {

    procedure 'proc1', {
        resourceName = 'test'

        step 's1', {
            command = 'echo test'
        }

        step 's2', {
            command = 'println "test";'
            shell = 'ec-perl'
        }

        step 's3', {
            command = 'print \'test\''
            shell = 'ec-groovy'
        }
    }

    procedure 'proc2', {
        step 's1', {
            command = 'exit 1'
        }
    }

    testProperty = 'test'
}

createAclEntry (projectName: args.projectName,
                          principalType: 'user', principalName: 'limited_user',
                          readPrivilege: 'allow', modifyPrivilege: 'allow', executePrivilege: 'allow')


createAclEntry (projectName: args.projectName,  procedureName: 'proc2',
                          principalType: 'user', principalName: 'limited_user',
                          readPrivilege: 'deny')
breakAclInheritance (projectName: args.projectName,  procedureName: 'proc2')