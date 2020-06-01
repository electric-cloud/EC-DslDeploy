project 'generateDslTestProject', {

procedure 'generateDslAndPublish', {
  projectName = 'generateDslTestProject'

  formalParameter 'artifactName', defaultValue: '', {
    required = '1'
    type = 'entry'
  }

  formalParameter 'artifactVersionVersion', defaultValue: '', {
    required = '1'
    type = 'entry'
  }

  formalParameter 'childrenInDifferentFile', defaultValue: '', {
    description = 'Comma-separated list of patterns to include, like pipeline, procedures.*, applications.applicationTiers.components'
    label = 'Children in Different Files'
    type = 'textarea'
  }

  formalParameter 'directory', defaultValue: '', {
    description = 'Folder where the code is generated'
    label = 'Directory path'
    required = '1'
    type = 'entry'
  }

  formalParameter 'includeAcls', defaultValue: '0', {
    description = 'Include in the generated DSL ACLs for objects.'
    checkedValue = '1'
    label = 'Include ACLs'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  formalParameter 'includeAclsInDifferentFile', defaultValue: '0', {
    description = 'Include ACLs for generated objects in different file.'
    checkedValue = '1'
    label = 'Include ACLs in different file'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  formalParameter 'includeAllChildren', defaultValue: '0', {
    description = 'Include in the generated DSL all object children. If True - ignore value of \'Include Children\' parameter.'
    checkedValue = '1'
    label = 'Include All Children'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  formalParameter 'includeChildren', defaultValue: '', {
    description = 'Comma-separated list of object children the DSL should be generated for.'
    label = 'Include children'
    type = 'textarea'
  }

  formalParameter 'includeChildrenInSameFile', defaultValue: '0', {
    description = 'Include in the generated DSL all object children. If True - ignore value of \'Include Children\' parameter.'
    checkedValue = '1'
    label = 'Include Children in Same File'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  formalParameter 'objectName', defaultValue: '', {
    description = 'Object name to generate DSL for.'
    label = 'Object Name'
    required = '1'
    type = 'entry'
  }

  formalParameter 'objectType', defaultValue: '', {
    description = 'Object type to generate DSL for.'
    label = 'Object Type'
    required = '1'
    type = 'entry'
  }

  formalParameter 'runResourceName', defaultValue: 'local', {
    label = 'Server Resource'
    required = '1'
    type = 'entry'
  }

  formalParameter 'suppressDefaults', defaultValue: '0', {
    description = 'Exclude from the generated DSL properties with default value.'
    checkedValue = '1'
    label = 'Suppress Defaults'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  formalParameter 'suppressNulls', defaultValue: '1', {
    description = 'Exclude from the generated DSL properties with null value.'
    checkedValue = '1'
    label = 'Suppress Nulls'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  formalParameter 'suppressParent', defaultValue: '0', {
    description = 'Exclude from the generated DSL properties referred to object parent.'
    checkedValue = '1'
    label = 'Suppress Parent'
    type = 'checkbox'
    uncheckedValue = '0'
  }

  step 'generate dsl', {
    condition = ''
    precondition = ''
    projectName = 'generateDslTestProject'
    resourceName = '$[runResourceName]'
    subprocedure = 'generateDslToDirectory'
    subproject = '/plugins/EC-DslDeploy/project'
    actualParameter 'childrenInDifferentFile', '$[childrenInDifferentFile]'
    actualParameter 'directory', '$[directory]'
    actualParameter 'includeAcls', '$[includeAcls]'
    actualParameter 'includeAclsInDifferentFile', '$[includeAclsInDifferentFile]'
    actualParameter 'includeAllChildren', '$[includeAllChildren]'
    actualParameter 'includeChildren', '$[includeChildren]'
    actualParameter 'includeChildrenInSameFile', '$[includeChildrenInSameFile]'
    actualParameter 'objectName', '$[objectName]'
    actualParameter 'objectType', '$[objectType]'
    actualParameter 'pool', '$[runResourceName]'
    actualParameter 'suppressDefaults', '$[suppressDefaults]'
    actualParameter 'suppressNulls', '$[suppressNulls]'
    actualParameter 'suppressParent', '$[suppressParent]'
  }

  step 'publish artifact', {
    condition = ''
    precondition = ''
    projectName = 'generateDslTestProject'
    resourceName = '$[runResourceName]'
    subprocedure = 'Publish'
    subproject = '/plugins/EC-Artifact/project'
    actualParameter 'artifactName', '$[artifactName]'
    actualParameter 'artifactVersionVersion', '$[artifactVersionVersion]'
    actualParameter 'compress', '1'
    actualParameter 'dependentArtifactVersionList', ''
    actualParameter 'excludePatterns', ''
    actualParameter 'followSymlinks', '1'
    actualParameter 'fromLocation', '$[/myJob/directoryFullPath]'
    actualParameter 'includePatterns', ''
    actualParameter 'repositoryName', 'default'
  }
}

}