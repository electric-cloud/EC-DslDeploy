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
                                         stepName: 'testProcedureStep', 
                                         notifierName: 'newNotifier', 
                                         formattingTemplate: 'Default', 
                                         destinations: 'a@a.a'
                                         )"""
        assert newProcedNotifier
        def newStepNotifier = dsl"""getEmailNotifier(projectName: '$projName',
                                         procedureName: 'testProcedure', 
                                         stepName: 'testProcedureStep', 
                                         notifierName: 'newNotifier', 
                                         formattingTemplate: 'Default', 
                                         destinations: 'a@a.a'
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
        assert stepNotifiers?.emailNotifier[0]?.notifierName == 'testEmailNotifier'

    }


    def "overwrite_installProject with workflowDefinition"(){
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

        //add new property to workflow definition
        when: "new property is added to workflowDefintion"
        dsl """createProperty(projectName: '$projName',
                                    workflowDefinitionName: 'wfd',
                                    propertyName: 'testProperty2'
                                    )"""
        then: "property exists"
        def newWorkflowProp = """getProperty(projectName: '$projName',
                                    workflowDefinitionName: 'wfd',
                                    propertyName: 'testProperty2'
                                    )"""
        assert newWorkflowProp

        //add new transtiton to existing state definition
        when: "new transition definition is added"
        dsl """createTransitionDefinition(projectName: '$projName',
                                                workflowDefinitonName: 'wfd',
                                                transitionDefinitonName: 'newTransition',
                                                stateDefinitionName: 'start',
                                                targetState: 'start'
                                                )"""
        then: "new transition definition exists"
        def newTrans = dsl """getTransitionDefinition(projectName: '$projName',
                                                workflowDefinitonName: 'wfd',
                                                transitionDefinitonName: 'newTransition',
                                                stateDefinitionName: 'start'
                                                )"""
        assert newTrans
        //add new emailNotifier to existing state definition
        //add new formalParameter to existing state definition
        //add new property to existing state definition

        //add new state definition to worklow definiton

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
}
