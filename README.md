# EC-DslDeploy
Deployment of your DSL code structure to your server.

# Summary
Plugin to deploy a full DSL code structure as shown below. The main advanage is
to be able to push all the code as one unit to your server.
In addition, it allows you to easily decompose the object structure (project,
procedure, ...) from the execution code itself in your steps. This will ease
code writing without the need to escape dollar signs, quotes, ... and to use
syntax highlight in your favorite editor.

# Structure
```
Top Level
  main.groovy
  projects
    PROJ_1
      project.groovy
      procedures
        PROC_1
          procedure.groovy
          steps
            echo.pl
            compute.sh
```      

# Usage
1. create an artifact of your code structure (see sample/createArtifactVersion.sh
  for an example)
2. Run the procedure installDsl and pass the artifact name and version

# MVP:
1. work on cluster
2. work on local workspace

## Dev Process
1. Generate and upload an artifact of the DSL Code (will provide script later on)

## DSL Installation

### Parameters
1. Artifact Name
1. Artifact Version
1. "local" pool (to reflect server resources)
1. "broadcast" required if cluster and local workspace so artifact is retrieved on all resources
1. workspace??
1. Repo ??

### Steps
1. retrieve artifact on "local
2. run equivalent of PW promote.groovy
