# EC-DslDeploy
Deployment of your DSL code

The goal of this project is to provide a plugin to deploy DSL code formated in a similar fashion than PluginWizard for consistency (minus the plugin aspect).
The idea is to run 

# Structure
```
Top Level
  main.groovy
  Projects
    PROJ_1
      project.groovy
      Procedures
        PROC_1
          procedure.groovy
          Steps
            echo.pl
            compute.sh
      Pipelines
        PIPE1
          pipeline.groovy
```         
# MVP:
1. work on cluster
2. work on local workspace

## Dev Process
1. Generate and upload an articfact of the DSL Code (will provide script later on)

## DSL Installation

### Parameters
1. Artifact Version
1."local" pool (to reflect server resources)
1."broadcast" required if cluster and local workspace so artifact is retrieved on all resources
1. workspace??
1. Repo ??

### Steps
1. retrieve artifact on "local
2. run equivalent of PW promote.groovy

