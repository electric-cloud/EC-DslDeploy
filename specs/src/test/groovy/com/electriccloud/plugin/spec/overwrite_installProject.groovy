package com.electriccloud.plugin.spec

import spock.lang.*

class overwrite_installProject extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir
    static String projName = "overwrite_installProject"

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl """
      deleteProject(projectName: "$projName")
    """
    }

    def doCleanupSpec() {
        conditionallyDeleteProject(projName)
    }

    def "overwrite_installProject with procedure"() {
        given: "the overwrite_installProject application code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_procedure/projects/$projName",
            projName: '$projName'
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        when: "add procedure fields"
        dsl """modifyProcedure(
            projectName: '$projName', 
            procedureName: 'testProcedure',
            description: 'new value',
            jobNameTemplate: 'new value',
            resourceName: 'new value',
            timeLimit: '10', 
            timeLimitUnits: 'seconds',
            workspaceName: 'new value'
        )"""

        then: "procedure fields are not empty"
        def procWithFields = dsl """getProcedure(
            projectName: '$projName', 
            procedureName: 'testProcedure'
        )"""
        assert procWithFields.procedure.description != ''
        assert procWithFields.procedure.resourceName != ''
        assert procWithFields.procedure.jobNameTemplate != ''
        assert procWithFields.procedure.workspaceName != ''
        assert procWithFields.procedure.timeLimit != ''

        when: "procedure step is added"
        dsl """createStep(projectName: '$projName', 
                                procedureName: 'testProcedure',
                                stepName: 'newStep'
                                )"""

        then: "new step exists"
        def newStep = dsl """getStep(projectName: '$projName', 
                                          procedureName: 'testProcedure', 
                                          stepName: 'newStep'
                                          )"""
        assert newStep

        when: "new formal parameter is added"
        //add to procedure
        dsl """createFormalParameter(projectName: '$projName',
                                           procedureName: 'testProcedure', 
                                           formalParameterName: 'newProcedFormal'
                                           )"""

        then: "new formal parameter exists"
        def newProcedFormal = dsl """getFormalParameter(projectName: '$projName', 
                                                            procedureName: 'testProcedure', 
                                                            formalParameterName: 'newProcedFormal'
                                                            )"""
        assert newProcedFormal

        when: "new formal output parameters is added"
        dsl """createFormalOutputParameter(projectName: '$projName',
                                                 procedureName: 'testProcedure',
                                                 formalOutputParameterName: 'newProcedFormalOutput'
                                                 )"""

        then: "new formal output parameter exists"
        def newFormalOutput = dsl """getFormalOutputParameter(projectName: '$projName',
                                                                  procedureName: 'testProcedure', 
                                                                  formalOutputParameterName: 'newProcedFormalOutput'
                                                                  )"""
        assert newFormalOutput

        when: "new properties are added"
        dsl """createProperty(projectName: '$projName', 
                                    procedureName: 'testProcedure', 
                                    propertyName: 'testProperty2'
                                    )"""
        dsl """createProperty(projectName: '$projName',
                                    procedureName: 'testProcedure', 
                                    stepName: 'testProcedureStep', 
                                    propertyName: 'testProperty2'
                                    )"""

        then: "new properties exists"
        def newProcedProperty = """getProperty(projectName: '$projName',
                                            procedureName: 'testProcedure',
                                            propertyName: 'testProperty2')"""
        assert newProcedProperty
        def newStepProperty = """getProperty(projectName: '$projName', 
                                          procedureName: 'testProcedure', 
                                          stepName: 'testProcedureStep', 
                                          propertyName: 'testProperty2'
                                          )"""
        assert newStepProperty

        when: "email notifiers are added"
        dsl """createEmailNotifier(projectName: '$projName',
                                         procedureName: 'testProcedure', 
                                         notifierName: 'newNotifier', 
                                         formattingTemplate: 'Default',
                                         destinations: 'a@a.a'
                                         )"""
        dsl """createEmailNotifier(projectName: '$projName',
                                         procedureName: 'testProcedure', 
                                         stepName: 'testProcedureStep',
                                         notifierName: 'newNotifier', 
                                         formattingTemplate: 'Default',
                                         destinations: 'a@a.a'
                                         )"""

        then: "new email notifiers exist"
        def newProcedNotifier = dsl"""getEmailNotifier(projectName: '$projName',
                                         procedureName: 'testProcedure',
                                         notifierName: 'newNotifier'
                                         )"""
        assert newProcedNotifier
        def newStepNotifier = dsl"""getEmailNotifier(projectName: '$projName',
                                         procedureName: 'testProcedure', 
                                         stepName: 'testProcedureStep',
                                         notifierName: 'newNotifier'
                                         )"""
        assert newStepNotifier

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_procedure/projects/$projName",
            projName: '$projName',
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p2.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "procedure has one step"
        def steps  = dsl """getSteps(projectName: 'overwrite_installProject', procedureName: 'testProcedure')"""
        assert steps?.step?.size == 1

        then: "procedure has one formal parameter"
        def formalParams = dsl """getFormalParameters(projectName: '$projName',
                                                          procedureName: 'testProcedure')"""
        assert formalParams?.formalParameter?.size == 1
        assert formalParams?.formalParameter[0].formalParameterName == 'testParameter'


        then: "procedure has one formal output parameter"
        def formalOutputParams = dsl """getFormalOutputParameters(projectName: '$projName',
                                                          procedureName: 'testProcedure')"""
        assert formalOutputParams?.formalOutputParameter?.size == 1
        assert formalOutputParams?.formalOutputParameter[0].formalOutputParameterName == 'testOutputParam'

        then: "procedure has one property"
        def procedProperties = dsl """getProperties(projectName: '$projName',
                                                          procedureName: 'testProcedure')"""

        assert procedProperties?.propertySheet?.property.size == 2
        assert procedProperties?.propertySheet?.property[1]?.propertyName == 'testProperty'

        then: "procedure has one email notifier"
        def procedNotifiers = dsl """getEmailNotifiers(projectName: '$projName',
                                                           procedureName: 'testProcedure')"""
        assert procedNotifiers?.emailNotifier?.size == 1
        assert procedNotifiers?.emailNotifier[0]?.notifierName == 'testEmailNotifier'

        then: "step has one property"
        def stepProperty = dsl """getProperties(projectName: '$projName',
            procedureName: 'testProcedure',
            stepName: 'testProcedureStep'
        )"""
        assert stepProperty?.propertySheet?.property?.size == 1
        assert stepProperty?.propertySheet?.property[0]?.propertyName == 'testStepProperty'

        then: "step has one email notifier"
        def stepNotifiers = dsl """getEmailNotifiers(projectName: '$projName',
                                                         procedureName: 'testProcedure',
                                                         stepName: 'testProcedureStep'
                                                         )"""
        assert stepNotifiers?.emailNotifier?.size == 1
        assert stepNotifiers?.emailNotifier[0]?.notifierName == 'testStepNotifier'

        and: "procedure fields are empty"
        def procWithoutFields = dsl """getProcedure(
            projectName: '$projName', 
            procedureName: 'testProcedure'
        )"""
        assert procWithoutFields.procedure.description == ''
        assert procWithoutFields.procedure.resourceName == ''
        assert procWithoutFields.procedure.jobNameTemplate == ''
        assert procWithoutFields.procedure.workspaceName == ''
        assert procWithoutFields.procedure.timeLimit == ''


    }

    def "overwrite_installProject with workflowDefinition"(){
        given: "the overwrite_installProject application code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_workflowDefinition/projects/$projName",
            projName: '$projName'
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        //add new property to workflow definition
        when: "new property is added to workflowDefintion"
        dsl """createProperty(projectName: '$projName',
                                    workflowDefinitionName: 'test_wfd',
                                    propertyName: 'testProperty2'
                                    )"""
        then: "property exists"
        def newWorkflowProp = """getProperty(projectName: '$projName',
                                    workflowDefinitionName: 'test_wfd',
                                    propertyName: 'testProperty2'
                                    )"""
        assert newWorkflowProp

        //add new transtiton to existing state definition
        when: "new transition definition is added"
        dsl """createTransitionDefinition(projectName: '$projName',
                                                workflowDefinitionName: 'test_wfd',
                                                transitionDefinitionName: 'newTransition',
                                                stateDefinitionName: 'start',
                                                targetState: 'start'
                                                )"""
        then: "new transition definition exists"
        def newTrans = dsl """getTransitionDefinition(projectName: '$projName',
                                                           workflowDefinitionName: 'test_wfd',
                                                           transitionDefinitionName: 'newTransition',
                                                           stateDefinitionName: 'start',
                                                           targetState: 'start'
                                                           )"""
        assert newTrans

        //add new emailNotifier to existing state definition
        when: "new email notifier is added to existing state definiton"
        dsl """createEmailNotifier(projectName: '$projName',
                                         workflowDefinitionName: 'test_wfd',
                                         stateDefinitionName: 'start', 
                                         notifierName: 'newNotifier', 
                                         formattingTemplate: 'Default',
                                         destinations: 'a@a.a'
                                         )"""
        then: "new notifier exists"
        def newNotifier = dsl"""getEmailNotifier(projectName: '$projName',
                                                     workflowDefinitionName: 'test_wfd',
                                                     notifierName: 'newNotifier',
                                                     stateDefinitionName: 'start'
                                                     )"""
        assert newNotifier

        //add new formalParameter to existing state definition
        when: "new formal parameter is added to existing state definition"
        dsl"""createFormalParameter(projectName: '$projName',
                                          workflowDefinitionName: 'test_wfd',
                                          formalParameterName: 'newFormal',
                                          stateDefinitionName: 'start'
                                          )"""
        then: "new formal parameter exists"
        def newFormal = dsl"""getFormalParameter(projectName: '$projName',
                                                         workflowDefinitionName: 'test_wfd',
                                                         formalParameterName: 'newFormal',
                                                         stateDefinitionName: 'start'
                                                         )"""
        assert newFormal

        //add new property to existing state definition
        when: "new property is added to workflowDefintion"
        dsl """createProperty(projectName: '$projName',
                                    workflowDefinitionName: 'test_wfd',
                                    stateDefinitionName: 'start',
                                    propertyName: 'testProperty2'
                                    )"""
        then: "property exists"
        def newStateProp = dsl """getProperty(projectName: '$projName',
                                    workflowDefinitionName: 'test_wfd',
                                    stateDefinitionName: 'start',
                                    propertyName: 'testProperty2'
                                    )"""
        assert newStateProp

        //add new state definition to worklow definiton
        when: "new state definition is added to workflow definition"
        dsl """createStateDefinition (projectName: '$projName',
                                            workflowDefinitionName: 'test_wfd',
                                            stateDefinitionName: 'newState'
                                            )"""
        then: "new state definition exists"
        def newState = dsl """getStateDefinition (projectName: '$projName',
                                                       workflowDefinitionName: 'test_wfd',
                                                       stateDefinitionName: 'newState'
                                                       )"""
        assert newState

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
                runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_workflowDefinition/projects/$projName",
            projName: '$projName',
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success"

        then: "workflow definition has only two state definitions"
        def stateDefinitions = dsl """getStateDefinitions(projectName: '$projName',
                                                             workflowDefinitionName: 'test_wfd')"""
        assert stateDefinitions?.stateDefinition?.size == 2
        assert stateDefinitions?.stateDefinition[0]?.stateDefinitionName == 'start'
        assert stateDefinitions?.stateDefinition[1]?.stateDefinitionName == 'finish'

        then: "workflow propery was cleaned up"
        def wfdProperties = dsl"""getProperties(projectName: '$projName',
                                                    workflowDefinitionName: 'test_wfd'
                                                    )"""
        assert wfdProperties?.propertySheet?.property[0]?.propertyName == 'testWorkflowProperty'

        then: "added transition definition was cleaned up"
        def transitions = dsl"""getTransitionDefinitions(projectName: '$projName',
                                                             workflowDefinitionName: 'test_wfd',
                                                             stateDefinitionName: 'start'
                                                             )"""
        assert transitions?.transitionDefinition?.size == 1
        assert transitions?.transitionDefinition[0].transitionDefinitionName == 'transition1'

        then: "added email notifier was cleaned up"
        def notifiers = dsl"""getEmailNotifiers(projectName: 'overwrite_installProject',                                                workflowDefinitionName: 'test_wfd',
                                      stateDefinitionName: 'start'
                                      )"""
        assert notifiers?.emailNotifier?.size == 1
        assert notifiers?.emailNotifier[0]?.notifierName == 'testNotifier'

        then: "added formal parameter was cleaned up"
        def formals = dsl"""getFormalParameters(projectName: '$projName',
                                                         workflowDefinitionName: 'test_wfd',
                                                         stateDefinitionName: 'start'
                                                         )"""
        assert formals?.formalParameter?.size ==1
        assert formals?.formalParameter[0].formalParameterName == 'testParameter'

        then: "added stated definition property was cleaned up"
        def stateProps = dsl """getProperties(projectName: '$projName',
                                    workflowDefinitionName: 'test_wfd',
                                    stateDefinitionName: 'start'
                                    )"""
        assert stateProps?.propertySheet?.property?.size == 2
        assert stateProps?.propertySheet?.property[1]?.propertyName == 'testStartProperty'
    }

    // overwrite with pipeline
    def "overwrite_installProject with pipeline"() {
        given: "the overwrite_installProject code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_pipeline/projects/overwrite_installProject",
            projName: 'overwrite_installProject'
          ]
        )""")
        then: "job completed with warnings"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "warning"

        when: "add content to pipeline"
        dsl """
        createStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "newStage"
        )"""

        // check master component
        then: "Check the stage is present"
        println "Checking new stage exists"
        def newStage = dsl """
        getStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "newStage"
        )"""
        assert newStage.stage.stageName == "newStage"

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_pipeline/projects/overwrite_installProject",
            projName: 'overwrite_installProject',
            overwrite: '1'
          ]
        )""")
        then: "job completed with warning"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "warning"

        then: "stage not exists"
        println "Checking new stage is not exists"

        def getTaskResult =
                dslWithXmlResponse("""
        getStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "newStage"
        )""", null, [ignoreStatusCode: true])

        assert getTaskResult
        assert getTaskResult.contains("NoSuchStage")
    }

    // overwrite with application
    def "overwrite_installProject with application"() {
        given: "the overwrite_installProject application code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_application/projects/overwrite_installProject",
            projName: 'overwrite_installProject'
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        when: "add content to application"
        dsl """
        createApplicationTier(
          projectName: "$projName",
          applicationName: "app1",
          applicationTierName: "newTier"
        )"""

        // check master component
        then: "Check the application tier is present"
        def newTier = dsl """
        getApplicationTier(
          projectName: "$projName",
          applicationName: "app1",
          applicationTierName: "newTier"
        )"""
        assert newTier.applicationTier.applicationTierName == "newTier"

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_application/projects/overwrite_installProject",
            projName: 'overwrite_installProject',
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success"

        then: "The application tier not exists"
        println "Checking new application tier is not exists"

        def getTierResult =
                dslWithXmlResponse("""
        getApplicationTier(
          projectName: "$projName",
          applicationName: "app1",
          applicationTierName: "newTier"
        )""", null, [ignoreStatusCode: true])

        assert getTierResult
        assert getTierResult.contains("NoSuchApplicationTier")
    }

    def "overwrite_installProject with catalogs"(){
        given: "the overwrite_installProject catalog code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_catalog/projects/overwrite_installProject",
            projName: 'overwrite_installProject'
          ]
        )""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        and: "store catalog id"
        def catalog = dsl"""getCatalog(projectName: 'overwrite_installProject', catalogName: 'testCatalog')"""
        def catalogId = catalog?.catalog?.catalogId

        and: "store catalog item id"
        def catalogItem = dsl"""getCatalogItem(projectName: 'overwrite_installProject', catalogName: 'testCatalog', catalogItemName: 'testItem')"""
        assert catalogItem
        def catalogItemId = catalogItem?.catalogItem?.catalogItemId

        when: "add catalog item to catalog"
        dsl """createCatalogItem(projectName: 'overwrite_installProject', catalogName: 'testCatalog', catalogItemName: 'testItem2')"""

        then: "new catalog item is created"
        def newCatalogItem = dsl """getCatalogItem(projectName: 'overwrite_installProject', catalogName: 'testCatalog', catalogItemName: 'testItem2')"""
        assert newCatalogItem

        then: "add property to catalog"
        def newProperty = dsl """createProperty(propertyName: 'testCatalogProperty2', projectName: 'overwrite_installProject', catalogName: 'testCatalog')"""
        assert newProperty

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_catalog/projects/overwrite_installProject",
            projName: 'overwrite_installProject',
            overwrite: '1'
          ]
        )""")
        then: "job succeeds"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success"

        then: "only one catalog remained in project"
        def catalogs = dsl """getCatalogs(projectName: 'overwrite_installProject')"""
        assert catalogs?.catalog?.size == 1
        def remainedCatalog = catalogs?.catalog[0]

        then: "catalog entity UUID did not change"
        assert remainedCatalog?.catalogId == catalogId

        then: "added property was overwritten"
        def properties = dsl """getProperties (projectName: 'overwrite_installProject', catalogName: 'testCatalog' )"""
        assert properties?.propertySheet?.property?.size == 1

        then: "only one catalog item remained"
        def catalogItems = dsl """getCatalogItems(projectName: 'overwrite_installProject', catalogName: 'testCatalog')"""
        assert catalogItems?.catalogItem?.size == 1
        def remainedCatalogItem = catalogItems?.catalogItem[0]

        then: "catalog item UUID did not change"
        assert remainedCatalogItem?.catalogItemId == catalogItemId
    }

    def "overwrite_installProject with environment"() {
        given: "the overwrite_installProject code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_environment/projects/overwrite_installProject",
            projName: 'overwrite_installProject'
          ]
        )""")
        then: "job completed"
        waitUntil {
            assert p.jobId
            assert getJobProperty("outcome", p.jobId) == "success" || getJobProperty("outcome", p.jobId) == "warning"
        }

        when: "add fields to cluster"
        def changedCluster = dsl """
        modifyCluster(
          projectName: "$projName",
          environmentName: "e1",
          clusterName: "c1",
          description: "new value"
        )"""

        then: "Cluster changed"
        assert changedCluster
        assert changedCluster.cluster.description == 'new value'

        when: "Create new cluster"
        def newCluster = dsl """
        createCluster(
          projectName: "$projName",
          environmentName: "e1",
          clusterName: "newCluster"
        )"""

        then: "Cluster created"
        assert newCluster
        assert newCluster.cluster.clusterName == 'newCluster'

        when: "Create new environment tier"
        def newTier = dsl """
        createEnvironmentTier(
          projectName: "$projName",
          environmentName: "e1",
          environmentTierName: "newTier"
        )"""

        then: "Tier created"
        assert newTier
        assert newTier.environmentTier.environmentTierName == 'newTier'

        when: "add fields to environment"
        def changedEnv = dsl """
        modifyEnvironment(
          projectName: "$projName",
          environmentName: "e1",
          description: "new value"
        )"""

        then: "Env changed"
        assert changedEnv
        assert changedEnv.environment.description == 'new value'
        assert changedEnv.environment.clusterCount == '2'
        assert changedEnv.environment.tierCount == '2'

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_environment/projects/overwrite_installProject",
            projName: 'overwrite_installProject',
            overwrite: '1'
          ]
        )""")
        then: "job completed"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success" || getJobProperty("outcome", p2.jobId) == "warning"

        when: "check env state"
        def changedEnv2 = dsl """
        getEnvironment(
          projectName: "$projName",
          environmentName: "e1"
        )"""

        then: "description is empty"
        assert changedEnv2
        assert changedEnv2.environment.description == ''
        assert changedEnv2.environment.clusterCount == '1'
        assert changedEnv2.environment.tierCount == '1'


        when: "check cluster description"
        def changedCluster2 = dsl """
            getCluster(
              projectName: "$projName",
              environmentName: "e1",
              clusterName: "c1"
        )"""

        then: "description is empty"
        assert changedCluster2
        assert changedCluster2.cluster.description == ''
    }

    def "overwrite_installProject with component"(){
        given: "the overwrite_installProject code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_component/projects/overwrite_installProject",
            projName: 'overwrite_installProject'
          ]
        )""")
        then: "job completed"
        waitUntil {
            assert p.jobId
            assert getJobProperty("outcome", p.jobId) == "success" || getJobProperty("outcome", p.jobId) == "warning"
        }

        when: "modify component fields"

        def modifiedComponent = dsl """
        modifyComponent(
            projectName: '$projName',
            componentName : 'comp_name1',
            description: 'this is new description'
            )"""

        then: "component was modified"
        assert modifiedComponent.component.description == 'this is new description'

        when: "modify component process fields"
        def modifiedComponentProcess = dsl """
        modifyProcess(
                projectName: '$projName',
                componentName : 'comp_name1',
                processName: 'proc_name1',
                timeLimit: '15',
                timeLimitUnits: 'hours',
                description: 'this is new description'
            )"""

        then: "component process was modified"
        assert modifiedComponentProcess.process.description == 'this is new description'
        assert modifiedComponentProcess.process.timeLimit == '15'
        assert modifiedComponentProcess.process.timeLimitUnits == 'hours'

        when: "modify component process step fields"
        def modifiedComponentProcessStep = dsl """
        modifyProcessStep(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1',
            processStepName: 'step1',
            timeLimit: '15',
            timeLimitUnits: 'hours',
            workspaceName: 'atatata',
            description: 'this is new description'
            )"""

        then: 'component process step was modified'
        assert modifiedComponentProcessStep.processStep.description == 'this is new description'
        assert modifiedComponentProcessStep.processStep.timeLimit == '15'
        assert modifiedComponentProcessStep.processStep.timeLimitUnits == 'hours'
        assert modifiedComponentProcessStep.processStep.workspaceName == 'atatata'

        when: "add component process"
        dsl """
        createProcess(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'tempProcess'
        )
        """
        then: 'new process was added'
        def tempProcess = dsl """
        getProcess(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'tempProcess'
        )
        """
        assert tempProcess

        when: "add component process step to existing process"
        dsl """createProcessStep(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1',
            processStepName: 'tempStep'
            )
        """

        then: 'process step was added'
        def tempProcessStep = dsl """
        getProcessStep(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1',
            processStepName: 'tempStep'
        )
        """
        assert tempProcessStep

        when: "Load DSL Code with overwrite = 1"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_component/projects/overwrite_installProject",
            projName: 'overwrite_installProject',
            overwrite: '1'
          ]
        )""")
        then: "job completed"
        assert p2.jobId
        assert getJobProperty("outcome", p2.jobId) == "success" || getJobProperty("outcome", p2.jobId) == "warning"

        then: 'component fields were cleared'
        def component = dsl """
        getComponent(
            projectName: '$projName',
            componentName : 'comp_name1'
        )
        """
        assert component.component.description == ''

        then: 'new component process was deleted'
        def componentProcess = dsl """
        getProcess(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1'
                )"""
        assert componentProcess
        assert componentProcess.process.timeLimit == ''
        assert componentProcess.process.timeLimitUnits == 'minutes'
        assert componentProcess.process.workspaceName == null
        assert component.component.processCount == '1'

        then: 'new process step was deleted'
        def componentProcessStep = dsl """
       getProcessStep(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1',
            processStepName: 'step1'
       ) 
       """
        assert componentProcessStep.processStep.timeLimit == ''
        assert componentProcessStep.processStep.timeLimitUnits == 'minutes'
        assert componentProcessStep.processStep.workspaceName == null

        def processSteps = dsl """
        getProcessSteps(
            projectName: 'overwrite_installProject',
            componentName : 'comp_name1',
            processName: 'proc_name1'
        )
        """
        assert processSteps.processStep.size() == 2
        assert processSteps.processStep[0].processStepName == 'step1'
        assert processSteps.processStep[1].processStepName == 'step2'
    }
}
