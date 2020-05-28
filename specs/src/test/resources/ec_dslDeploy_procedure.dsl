project args.projectName, {

  procedure args.procedureName, {

    step 'stepName', {
      resourceName = 'local'
      subprocedure = 'generateDslToDirectory'
      subproject = '/plugins/EC-DslDeploy/project'
      actualParameter 'childrenInDifferentFile', ''
      actualParameter 'directory', '/tmp/'
      actualParameter 'includeAcls', '0'
      actualParameter 'includeAclsInDifferentFile', '0'
      actualParameter 'includeAllChildren', '1'
      actualParameter 'includeChildren', ''
      actualParameter 'includeChildrenInSameFile', '0'
      actualParameter 'objectName', args.projectName
      actualParameter 'objectType', 'project'
      actualParameter 'pool', 'local'
      actualParameter 'suppressDefaults', '1'
      actualParameter 'suppressNulls', '1'
      actualParameter 'suppressParent', '1'
    }
  }
}