# EC-DslDeploy
Create structured DSL from your server or Deploy it into your server.

# Summary
Use this plugin to create a folder of Dsl files structured to match the object hierarchy of Projects (and other objects) in your server.
([file structure](https://github.com/electric-cloud-community/EC-DslDeploy/wiki/file-structure))

The resulting files and folders are suitable for storing in most source code repositories.
This also enables DevOps developers to treat your server configuration as code.

This plugin will also import a folder structure that has been created by this plugin as described above.

# Structure
The file structure is explained in detail in the [Wiki](https://github.com/electric-cloud-community/EC-DslDeploy/wiki/file-structure)

# Use Cases
1. Export server configuration to Dsl
   1. Run generateDslToDirectory and pass a target directory and upper level object type and/or name
   2. Or use the "Export DSL" catalog item
2. Import previously created Dsl
   1. If necessary create an artifact of your code structure (see sample/createArtifactVersion.sh
      for an example)
   2. Run installDslFromDirectory and pass the source directory
   3. OR use the "Import DSL" catalog item
3. Import Dsl from a Git repository
   1. Run importDslFromGitNew pass several parameters including the URL for the git repository and the target directory for the resulting Dsl.
   2. Or use the "Import DSL from Git" catalog item
   3. importDslFromGitNew also allows a partial/incremental import 

# Entry Points and parameters
## generateDslToDirectory
Export server configuration to Dsl
  * **Directory path** - target folder to create Dsl files and folders
  * **Server Pool or Resource** - resource which will perform the export
  * **Object Type** - which object type to generate Dsl for
  * **Object Name** - which object name to generate Dsl for
  * **Suppress Nulls** - exclude properties with a null value
  * **Suppress Default** - exclude properties with the default value
  * **Suppress Parent** - exclude properties which refer to the parent object
  * **Include ACLs** - include ACLs
  * **Include ACLs in different file** - put ACLs in separate files
  * **Include All Children** - include all children (and ignore "Include Children")
  * **Include Children** - list of which children to include
  * **Include Children in Same file** - put children in the same file (and ignore "Include Children")
  * **Children in Different Files** - list of children to put in different files
## installDslFromDirectory
Import previously created Dsl
  * **Directory Path** - the source folder of Dsl to import
  * **Server Pool or Resource** - resource which will perform the import
  * **Overwrite Mode** - remove non-existent dsl Elements
  * **Additional DSL arguments** - Additional argument(s) for evalDsl call. ie: --timeout or --debug. Values used in --timeout will be used as timeouts for ectool, evalDsl, and the job step time out used to import the DSL.
  * **Ignore failures** - keep importing in spite of a failure
  * **Local mode** - make file references locally instead of using a transfer protocol to the resource performing the import
  * **Include Objects** - List of paths of objects to include (default is ALL)
  * **Exclude Objects** - List of paths of objects to exclude (default is None)
## importDslFromGitNew
Import Dsl from a Git repository
  * **Server Pool or Resource** - The resource or one of the pool resources where the DSL files will be checked out from git and imported to the CD/RO server.
  * **Destination Directory** - Target folder where the Dsl files are created and imported
  * **Relative path to DSL files** - If the Dsl files are not at the top level (in the repository) then this is the path to get to them
  * **Cleanup?** - Delete the "Destination Directory" when done
  * **Overwrite Mode** - remove non-existent dsl Elements
  * **Configuration** - The plugin configuration path name to make access through access EC-Git
  * **Git repository** - URL to the git repository
  * **Remote branch** - which git repository branch name (default 'master')
  * **Ignore failures** - keep importing in spite of a failure
  * **Local mode** - make file references locally instead of using a transfer protocol to the resource performing the import
  * **Additional DSL arguments** - Additional argument(s) for evalDsl call. ie: --timeout or --debug. Values used in --timeout will be used as timeouts for ectool, evalDsl, and the job step time out used to import the DSL.
  * **Include objects** - list of paths to objects to include (default ALL)
  * **Exclude objects** - list of paths to objects to exclude (default None)
  * **Incremental Import** - Import only changes since the last "importDslFromGitNew".  Use a previous run's saved commit ID to compare to the current git repository commit ID and produce a change list to work from.
