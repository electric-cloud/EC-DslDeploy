package com.electriccloud.plugin.spec

import spock.lang.Shared
import java.util.regex.Matcher
import java.util.regex.Pattern

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
        assert getJobProperty("outcome", p2.jobId) == "success"

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

        String wfdName = 'test_wfd'

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

        then: "validate workflowDefiniton"
        def wfd = dsl """getWorkflowDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName')"""
        assert wfd.workflowDefinition.description == 'original description'
        assert wfd.workflowDefinition.workflowNameTemplate == 'original template'

        then: "validate state definitions"
        def startDefinition = dsl """getStateDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName',
                                                    stateDefinitionName: 'start')"""
        assert startDefinition?.stateDefinition?.description == 'original description'
        assert startDefinition?.stateDefinition?.startable == '0'

        then: "validate transition definition"
        def transition = dsl """getTransitionDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName',
                                                    stateDefinitionName: 'start',
                                                    transitionDefinitionName: 'transition1')"""

        assert transition?.transitionDefinition?.description == 'original description'
        assert transition?.transitionDefinition?.trigger == 'manual'
        assert transition?.transitionDefinition?.condition == '1'

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

        then: "modify workflow defintion fields"
        def modifiedWfd = dsl """modifyWorkflowDefinition(projectName: '$projName',
                                                              workflowDefinitionName: '$wfdName',
                                                              description: 'new description',
                                                              workflowNameTemplate: 'new template')"""
        assert modifiedWfd?.workflowDefinition?.description == 'new description'
        assert modifiedWfd.workflowDefinition.workflowNameTemplate == 'new template'

        then: "modify state definition fields"
        def modifiedStateDefinition = dsl """modifyStateDefinition(projectName: '$projName',
                                                                      workflowDefinitionName: '$wfdName',
                                                                      stateDefinitionName: 'start',
                                                                      description: 'new description',
                                                                      startable: '1')"""
        assert modifiedStateDefinition?.stateDefinition?.description == 'new description'
        assert modifiedStateDefinition?.stateDefinition?.startable == '1'

        then: "modify transition definition fields"
        def modifiedTransition = dsl """modifyTransitionDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName',
                                                    stateDefinitionName: 'start',
                                                    transitionDefinitionName: 'transition1',
                                                    description: 'new description',
                                                    trigger: 'onEnter',
                                                    condition: 'new')"""

        assert modifiedTransition?.transitionDefinition?.description == 'new description'
        assert modifiedTransition?.transitionDefinition?.trigger == 'onEnter'
        assert modifiedTransition?.transitionDefinition?.condition == 'new'

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

        then: "validate ovewrite workflowDefiniton"
        def overwriteWfd = dsl """getWorkflowDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName')"""
        assert overwriteWfd.workflowDefinition.description == 'original description'
        assert overwriteWfd.workflowDefinition.workflowNameTemplate == 'original template'

        then: "validate overwrite state definitions"
        def overwriteStartDefinition = dsl """getStateDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName',
                                                    stateDefinitionName: 'start')"""
        assert overwriteStartDefinition?.stateDefinition?.description == 'original description'
        assert overwriteStartDefinition?.stateDefinition?.startable == '0'

        then: "validate overwrite transition definition"
        def overwriteTransition = dsl """getTransitionDefinition(projectName: '$projName',
                                                    workflowDefinitionName: '$wfdName',
                                                    stateDefinitionName: 'start',
                                                    transitionDefinitionName: 'transition1')"""

        assert overwriteTransition?.transitionDefinition?.description == 'original description'
        assert overwriteTransition?.transitionDefinition?.trigger == 'manual'
        assert overwriteTransition?.transitionDefinition?.condition == '1'


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
        def newStageName = "newStage"
        def newAclEntryName = "u2"

        given: "the overwrite_installProject code"
        dslWithXmlResponse("""createUser( 
            userName: "u2"
         )""", null, [ignoreStatusCode: true])

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

        when: "add new aclEntry to project"
        def newAclEntry = dsl """createAclEntry(
            projectName: "$projName",
            principalType: "user",
            principalName: "$newAclEntryName"
        )"""

        then: "new acl entry exists"
        assert newAclEntry
        assert newAclEntry.aclEntry.principalName == newAclEntryName

        when: "add new aclEntry to stage"
        def newAclEntry2 = dsl """createAclEntry(
            projectName: "$projName",
            principalType: "user",
            pipelineName: "p12",
            stageName: "SIT",
            principalName: "$newAclEntryName"
        )"""

        then: "new acl entry exists"
        assert newAclEntry2
        assert newAclEntry2.aclEntry.principalName == newAclEntryName

        when: "add stage to pipeline"
        dsl """
        createStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "$newStageName"
        )"""

        // check master component
        then: "Check the stage is present"
        println "Checking new stage exists"
        def newStage = dsl """
        getStage(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "$newStageName"
        )"""
        assert newStage.stage.stageName == newStageName

        when: "add task to stage"
        def newTask = dsl """
        createTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "SIT",
          taskName: "newTask"
        )"""

        then: "Check the task is present"
        assert newTask
        assert newTask.task.taskName == "newTask"
        when: "add task to stage group"
        def newTask2 = dsl """
        createTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "DEV",
          groupName: "Group 1",
          taskName: "newTaskInGroup"
        )"""

        then: "Check the task is present"
        assert newTask2
        assert newTask2.task.taskName == "newTaskInGroup"
        assert newTask2.task.groupName == "Group 1"

        when: "add task to gate"
        def newRule2 = dsl """
        createTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "DEV",
          gateType: "PRE",
          taskName: "newRule2"
        )"""

        then: "Check the rule is present"
        assert newRule2
        assert newRule2.task.taskName == "newRule2"
        assert !newRule2.task.gateName.isEmpty()


        when: "add task to gate group"
        def newRule = dsl """
        createTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "DEV",
          gateType: "PRE",
          groupName: "Group 1",
          taskName: "newRule"
        )"""

        then: "Check the rule is present"
        assert newRule
        assert newRule.task.taskName == "newRule"
        assert !newRule.task.gateName.isEmpty()
        assert newRule.task.groupName == "Group 1"

        when: "add description to task"
        def oldTask = dsl """
        modifyTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "SIT",
          taskName: "JA1 Deploy",
          description: "newValue"
        )"""

        then: "Check the task is changed"
        assert oldTask
        assert oldTask.task.taskName == "JA1 Deploy"
        assert oldTask.task.description == "newValue"

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
        assert getJobProperty("outcome", p2.jobId) == "warning" || getJobProperty("outcome", p2.jobId) == "success"

        then: "Check stages state"

        def getStagesResult = dsl """
            getStages(
              projectName: "$projName",
              pipelineName: "p12"
            )"""

        assert getStagesResult
        assert getStagesResult.stage.size() == 3

        def stage1 = getStagesResult.stage.find {it.stageName == newStageName}
        assert stage1 == null
        def stage2 = getStagesResult.stage.find {it.stageName == 'DEV'}
        assert stage2
        assert stage2.stageName == 'DEV'

        then: "task not exists"
        def getTaskResult =
                dslWithXmlResponse("""
        getTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "SIT",
          taskName: "newTask"
        )""", null, [ignoreStatusCode: true])

        assert getTaskResult
        assert getTaskResult.contains("NoSuchTask")

        then: "task in group not exists"
        def getTaskResult2 =
                dslWithXmlResponse("""
        getTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "DEV",
          taskName: "newTaskInGroup"
        )""", null, [ignoreStatusCode: true])

        assert getTaskResult2
        assert getTaskResult2.contains("NoSuchTask")

        then: "rule not exists"
        def getRuleResult2 =
                dslWithXmlResponse("""
        getTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "DEV",
          gateType: "PRE",
          taskName: "newRule2"
        )""", null, [ignoreStatusCode: true])

        assert getRuleResult2
        assert getRuleResult2.contains("NoSuchTask")


        then: "rule in group not exists"
        def getRuleResult =
                dslWithXmlResponse("""
        getTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "DEV",
          gateType: "PRE",
          taskName: "newRule"
        )""", null, [ignoreStatusCode: true])

        assert getRuleResult
        assert getRuleResult.contains("NoSuchTask")

        then: "description is empty"

        def taskJA = dsl """
        getTask(
          projectName: "$projName",
          pipelineName: "p12",
          stageName: "SIT",
          taskName: "JA1 Deploy"
        )"""

        assert taskJA
        assert taskJA.task.taskName == "JA1 Deploy"
        assert taskJA.task.description == ""

        then: "old aclEntry still exists"
        def oldEntryResult1 = dsl """ getAclEntry(
            projectName: "$projName",
            principalType: "user",
            principalName: "project: overwrite_installProject"
        )"""
        assert oldEntryResult1
        assert oldEntryResult1.aclEntry.principalName == "project: overwrite_installProject"

        and: "old aclEntry for pipeline still exists"
        def oldEntryResult2 = dsl """ getAclEntry(
            projectName: "$projName",
            pipelineName: "p12",
            principalType: "user",
            principalName: "project: overwrite_installProject"
        )"""
        assert oldEntryResult2
        assert oldEntryResult2.aclEntry.principalName == "project: overwrite_installProject"

        then: "project aclEntry not exists"
        def aclEntryResult =
                dslWithXmlResponse("""
        getAclEntry(
            projectName: "$projName",
            principalType: "user",
            principalName: "$newAclEntryName"
        )""", null, [ignoreStatusCode: true])
        assert aclEntryResult
        assert aclEntryResult.contains("NoSuchAclEntry")

        then: "stage aclEntry not exists"
        def aclEntryResult2 =
                dslWithXmlResponse("""
        getAclEntry(
            projectName: "$projName",
            principalType: "user",
            pipelineName: "p12",
            stageName: "SIT",
            principalName: "$newAclEntryName"
        )""", null, [ignoreStatusCode: true])
        assert aclEntryResult2
        assert aclEntryResult2.contains("NoSuchAclEntry")
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

        when: 'add component and process for component1, and process step for process1'
        dsl """
        project  '$projName', {  
            application 'app1', {
                applicationTier 'Tier 1', {
                    component 'new', {
                        pluginKey = 'EC-Artifact'
                    }
                    
                    component 'component1', {
                        description = 'new'
                        process 'new', {
                             processType = 'DEPLOY'
                        }
                        process 'process1', {
                            processStep 'new', {
                                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                                processStepType = 'manual'
                                assignee = 'admin'
                            }
                        }
                    }
                }
            }
        }
        """

        then: "Check the application tier is present"
        def newComponent = dsl """
         getComponent(
          projectName: "$projName",
          applicationName: "app1",
          componentName: "new"
        )"""
        assert newComponent.component.componentName == "new"


        and: "Load DSL Code with overwrite = 1"
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

        //
        def getComponentResult = dslWithXmlResponse("""
        getComponent(
          projectName: "$projName",
          applicationName: "app1",
          componentName: "new"
        )""", null, [ignoreStatusCode: true])

        assert getComponentResult &&  getComponentResult.contains("NoSuchComponent")

        //
        def getComponentProcRes = dslWithXmlResponse("""
        getProcess(
          projectName: "$projName",
          componentApplicationName: "app1",
          componentName: "component1",
          processName: "new"
        )""", null, [ignoreStatusCode: true])

        assert getComponentProcRes &&  getComponentProcRes.contains("NoSuchProcess")

        //
        def getComponentProcStepRes = dslWithXmlResponse("""
        getProcessStep(
          projectName: "$projName",
          componentApplicationName: "app1",
          componentName: "component1",
          processName: "process1",
          processStepName: "new"
        )""", null, [ignoreStatusCode: true])

        assert getComponentProcStepRes &&  getComponentProcStepRes.contains("NoSuchProcessStep")
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
        assert catalog?.catalog?.description == 'original description'

        and: "store catalog item id"
        def catalogItem = dsl"""getCatalogItem(projectName: 'overwrite_installProject', catalogName: 'testCatalog', catalogItemName: 'testItem')"""
        assert catalogItem
        def catalogItemId = catalogItem?.catalogItem?.catalogItemId
        assert catalogItem?.catalogItem?.description == 'original description'
        assert catalogItem?.catalogItem?.buttonLabel == 'Original Label'

        and: "modify catalog fields values"
        def modifiedCatalog = dsl """modifyCatalog(projectName: 'overwrite_installProject', catalogName: 'testCatalog', description: 'new description')"""
        assert modifiedCatalog.catalog.description == 'new description'

        and: "modify catalog item fields values"
        def modifiedCatalogItem = dsl"""modifyCatalogItem(
                            projectName: 'overwrite_installProject',
                            catalogName: 'testCatalog', 
                            catalogItemName: 'testItem',
                            description: 'new description',
                            buttonLabel: 'new label')"""
        assert modifiedCatalogItem?.catalogItem?.description == 'new description'
        assert modifiedCatalogItem?.catalogItem?.buttonLabel == 'new label'

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
        assert remainedCatalog?.description == 'original description'

        then: "added property was overwritten"
        def properties = dsl """getProperties (projectName: 'overwrite_installProject', catalogName: 'testCatalog' )"""
        assert properties?.propertySheet?.property?.size == 1

        then: "only one catalog item remained"
        def catalogItems = dsl """getCatalogItems(projectName: 'overwrite_installProject', catalogName: 'testCatalog')"""
        assert catalogItems?.catalogItem?.size == 1
        def remainedCatalogItem = catalogItems?.catalogItem[0]

        then: "catalog item UUID did not change"
        assert remainedCatalogItem?.catalogItemId == catalogItemId
        assert remainedCatalogItem?.description == 'original description'
        assert remainedCatalogItem?.buttonLabel == 'Original Label'
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

        then: "validate component fields"
        def component = dsl """getComponent(projectName: '$projName',
                                                componentName: 'comp_name1')"""

        assert component?.component?.description == 'original description'

        then: "validate component process fields"
        def compProcess = dsl """
        getProcess(
                projectName: '$projName',
                componentName : 'comp_name1',
                processName: 'proc_name1'
                )"""

        assert compProcess?.process?.description == 'original description'
        assert compProcess?.process?.timeLimit == '15'
        assert compProcess?.process?.timeLimitUnits == 'seconds'
        assert compProcess?.process?.processType == 'UNDEPLOY'
        assert compProcess?.process?.workingDirectory == 'tmp'

        then: "validate component process step fields"
        def compProcessStep = dsl """getProcessStep(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1',
            processStepName: 'step1'
            )"""

        assert compProcessStep?.processStep?.description == 'original description'
        assert compProcessStep?.processStep?.timeLimit == '10'
        assert compProcessStep?.processStep?.timeLimitUnits == 'seconds'

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
                description: 'this is new description',
                processType: 'DEPLOY',
                workingDirectory: 'new tmp'
            )"""

        then: "component process was modified"
        assert modifiedComponentProcess.process.description == 'this is new description'
        assert modifiedComponentProcess.process.timeLimit == '15'
        assert modifiedComponentProcess.process.timeLimitUnits == 'hours'
        assert modifiedComponentProcess?.process?.processType == 'DEPLOY'
        assert modifiedComponentProcess?.process?.workingDirectory == 'new tmp'

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
        def overrideComponent = dsl """
        getComponent(
            projectName: '$projName',
            componentName : 'comp_name1'
        )
        """
        assert overrideComponent.component.description == 'original description'
        //temp process was deleted
        assert overrideComponent.component.processCount == '1'

        then: "validate component process fields"
        def overwriteProcess = dsl """
        modifyProcess(
                projectName: '$projName',
                componentName : 'comp_name1',
                processName: 'proc_name1')"""

        assert overwriteProcess?.process?.description == 'original description'
        assert overwriteProcess?.process?.timeLimit == '15'
        assert overwriteProcess?.process?.timeLimitUnits == 'seconds'
        assert overwriteProcess?.process?.processType == 'UNDEPLOY'
        assert overwriteProcess?.process?.workingDirectory == 'tmp'

        then: "validate component process step fields"
        def overwriteprocessStep = dsl """getProcessStep(
            projectName: '$projName',
            componentName : 'comp_name1',
            processName: 'proc_name1',
            processStepName: 'step1'
            )"""

        assert overwriteprocessStep?.processStep?.description == 'original description'
        assert overwriteprocessStep?.processStep?.timeLimit == '10'
        assert overwriteprocessStep?.processStep?.timeLimitUnits == 'seconds'

        then: 'new process step was deleted'
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


    def "overwrite_installProject properties"(){
        given: "the overwrite_installProject code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_project/projects/overwrite_properties",
            projName: 'overwrite_properties'
          ]
        )""")
        then: "job completed"
        waitUntil {
            assert p.jobId
            assert getJobProperty("outcome", p.jobId) == "success" || getJobProperty("outcome", p.jobId) == "warning"
        }

        when: 'create new properties and modify existent'
        dsl """
            project 'overwrite_properties', {
                pipeline 'pipe1', {
                    property 'ec_pipe1', {
                        ec_pipe1 = 'new'
                        ec_pipe2 = 'new'
                        
                        property 'ec_pipe11', {
                            ec_pipe11 = 'new'
                            ec_pipe12 = 'new'
                        }
                    }
                    property 'ec_pipe2', {
                        property 'ec_pipe2', {
                            value: 'new'
                        }
                        property 'ec_pipe1', {
                            value: 'new'
                        }
                    }
                
                }
                
                
                property 'ec_1', {
                    ec_prop1 = 'new'
                    ec_prop2 = 'new'
                    property 'ec_11', {
                        ec_prop11 = 'new'
                        ec_prop12 = 'new'
                        property 'ec_111', {
                            ec_prop111 = 'new'
                            ec_prop112 = 'new'
                        }
                    }    
                }
                
                property 'ec2', {
                    ec2_prop1 = 'new'
                    ec2_prop2 = 'new'
                }
             }   
"""

        then: 'check the properties number'
        def projPropResult = dsl "getProperties projectName: 'overwrite_properties'"
        assert  projPropResult.propertySheet.property.size() == 2

        def pipePropResult = dsl "getProperties projectName: 'overwrite_properties', pipelineName: 'pipe1'"
        assert pipePropResult.propertySheet.property.size() == 2

        when: "Load DSL Code in overwrite mode"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_project/projects/overwrite_properties",
            projName: 'overwrite_properties',
            overwrite: '1'
          ]
        )""")
        then: "job completed"
        waitUntil {
            assert p2.jobId
            assert getJobProperty("outcome", p2.jobId) == "success" || getJobProperty("outcome", p2.jobId) == "warning"
        }

        then: 'check properties'

        def projPropResult2 = dsl "getProperties projectName: 'overwrite_properties'"
        assert projPropResult2.propertySheet.property.size() == 2

        def ec_1 = projPropResult.propertySheet.property.find{it.propertyName == 'ec_1'}

        def ec_1Res = dsl "getProperties propertySheetId: '$ec_1.propertySheetId'"
        assert ec_1Res.propertySheet.property.size() == 2

        def ec_prop1 = ec_1Res.propertySheet.property.find{it.propertyName == 'ec_prop1'}
        assert ec_prop1.value == '1'

        def ec_11 = ec_1Res.propertySheet.property.find{it.propertyName == 'ec_11'}
        def ec_11Res = dsl "getProperties propertySheetId: '$ec_11.propertySheetId'"
        assert ec_11Res.propertySheet.property.size() == 2

        def ec_prop11 =  ec_11Res.propertySheet.property.find{it.propertyName == 'ec_prop11'}
        assert ec_prop11.value == '11'

        def ec_111 = ec_11Res.propertySheet.property.find{it.propertyName == 'ec_111'}
        def ec_111Res = dsl "getProperties propertySheetId: '$ec_111.propertySheetId'"
        assert ec_111Res.propertySheet.property.size() == 1


        def projPipeResult2 = dsl "getProperties projectName: 'overwrite_properties', pipelineName: 'pipe1'"
        assert projPipeResult2.propertySheet.property.size() == 1

        def ec_pipe1 = projPipeResult2.propertySheet.property.find{it.propertyName == 'ec_pipe1'}

        def ec_pipe1Res = dsl "getProperties propertySheetId: '$ec_pipe1.propertySheetId'"
        assert ec_pipe1Res.propertySheet.property.size() == 2

        def ec_pipe1prop = ec_pipe1Res.propertySheet.property.find{it.propertyName == 'ec_pipe1'}
        assert ec_pipe1prop.value == 'pipe1'

        def ec_pipe11 =  ec_pipe1Res.propertySheet.property.find{it.propertyName == 'ec_pipe11'}

        def ec_pipe11Res = dsl "getProperties propertySheetId: '$ec_pipe11.propertySheetId'"
        assert ec_pipe11Res.propertySheet.property.size() == 1
        assert ec_pipe11Res.propertySheet.property[0].value == 'pipe11'

    }

    /**
     * CEV-24672: import of '/projects/Commander/release/counter' property
     * failed
     */
    def "overwrite_installProject properties:conflict with intrinsic property"(){
        def testProjName = 'CEV-24672';

        given: "the overwrite_installProject code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_project/projects/$testProjName",
            projName: "$testProjName"
          ]
        )""")
        then: "job completed"
        waitUntil {
            assert p.jobId
            assert getJobProperty("outcome", p.jobId) == "success" || getJobProperty("outcome", p.jobId) == "warning"
        }

        def projPropResult = dsl "getProperties projectName: '$testProjName'"
        assert  projPropResult.propertySheet.property.size() == 2

        //
        def release = projPropResult.propertySheet.property.find{it
                .propertyName == 'release'}

        def releaseRes = dsl "getProperties propertySheetId: '$release.propertySheetId'"
        assert releaseRes.propertySheet.property.size() == 1
        def r_counter_prop = releaseRes.propertySheet.property.find{
            it.propertyName == 'counter'}
        assert r_counter_prop.value == '10'
        //
        def procedure = projPropResult.propertySheet.property.find{it
                .propertyName == 'procedure'}

        def procedureRes = dsl "getProperties propertySheetId: '$procedure.propertySheetId'"
        assert procedureRes.propertySheet.property.size() == 2
        def p_counter_prop = procedureRes.propertySheet.property.find{
            it.propertyName == 'counter'}
        assert p_counter_prop.value == '20'
        //
        def test = procedureRes.propertySheet.property.find{it
                .propertyName == 'test'}
        def testRes = dsl "getProperties propertySheetId: '$test.propertySheetId'"
        assert testRes.propertySheet.property.size() == 1
        def dot_prop = testRes.propertySheet.property.find{
            it.propertyName == 'property.with.dot'}
        assert dot_prop.value == 'test'
        //
        def releaseResult = dsl "getProperties projectName: " +
                "'$testProjName', releaseName: 'release1'"
        assert releaseResult.propertySheet.property.size() == 2
        //

        when: 'create new properties'
        dsl """
            project 'CEV-24672', {
                release 'release1', {
                    property 'sheet1', {
                        new_property = 'new'
                    }
                    property 'ec_pipe1', {
                        ec_pipe1 = 'new'
                        ec_pipe2 = 'new'
                    }
                    property 'new', {
                        value: 'new'
                    }
                }
                
                prop1 = 'new'
                property 'new-sheet', {
                    prop2 = 'new'
                }
             }   
"""

        and: "Load DSL Code in overwrite mode"
        def p2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/overwrite_project/projects/$testProjName",
            projName: '$testProjName',
            overwrite: '1'
          ]
        )""")
        then: "job completed"
        waitUntil {
            assert p2.jobId
            assert getJobProperty("outcome", p2.jobId) == "success" || getJobProperty("outcome", p2.jobId) == "warning"
        }

        then: 'check properties'

        def projPropResult2 = dsl "getProperties projectName: '$testProjName'"
        assert  projPropResult2.propertySheet.property.size() == 4

        //
        def release2 = projPropResult2.propertySheet.property.find{it
                .propertyName == 'release'}

        def releaseRes2 =
                dsl "getProperties propertySheetId: '$release2.propertySheetId'"
        assert releaseRes2.propertySheet.property.size() == 1
        def r_counter_prop2 = releaseRes2.propertySheet.property.find{
            it.propertyName == 'counter'}
        assert r_counter_prop2.value == '10'
        //
        def procedure2 = projPropResult.propertySheet.property.find{it
                .propertyName == 'procedure'}

        def procedureRes2 =
                dsl "getProperties propertySheetId: '$procedure2.propertySheetId'"
        assert procedureRes2.propertySheet.property.size() == 2
        def p_counter_prop2 = procedureRes2.propertySheet.property.find{
            it.propertyName == 'counter'}
        assert p_counter_prop2.value == '20'
        //
        def test2 = procedureRes2.propertySheet.property.find{it
                .propertyName == 'test'}
        def testRes2 =
                dsl "getProperties propertySheetId: '$test2.propertySheetId'"
        assert testRes2.propertySheet.property.size() == 1
        def dot_prop2 = testRes2.propertySheet.property.find{
            it.propertyName == 'property.with.dot'}
        assert dot_prop2.value == 'test'
        //
        def releaseResult2 = dsl "getProperties projectName: " +
                "'$testProjName', releaseName: 'release1'"
        assert releaseResult2.propertySheet.property.size() == 2

        cleanup:
        deleteProjects([projectName: testProjName], false)
    }

    //Leading and trailing spaces are not supported
    def "install project with special symbols in names"() {
        def testProjName = 'new test Import'
        def procName = 'A Procedure %% \\\\\\\\ @'
        def procStepName = 'step # 1'
        def pipelineName = 'Verify QA / Notify'
        def pipeStageName = 'Stage 1'
        def pipeTaskName = 'task %%1'

        given: "spec_symbols project code"

        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/spec_symbols/projects/new test Import",
            projName: "$testProjName"
          ]
        )""")

        then: "job completed"
        waitUntil {
            assert p.jobId
            assert getJobProperty("outcome", p.jobId) == "success" || getJobProperty("outcome", p.jobId) == "warning"
        }

        when:
        def result = dsl "getProject(projectName: '$testProjName')"

        then: "project created"
        assert result.project['projectName'] == testProjName

        when:
        result = dsl "generateDsl(path: '/projects/$testProjName')"
        println result
        //resort to manual parsing due to implicit dsl test escaping
        Matcher matcher = Pattern.compile("procedure '(.*)', \\{[\\W\\w]*step '(.*)', \\{[\\W\\w]*command\\s=\\s'(.*)'[\\W\\w]*?}?\\W*}")
                                 .matcher(result.value);
        if (matcher.find()) {
            assert matcher.group(1) == procName
            assert matcher.group(2) == procStepName
            assert matcher.group(3) == "echo Procedure is completed"
        }
        else assert false

        and:
        result = dsl "getPipeline(projectName: '$testProjName', pipelineName: '$pipelineName')"

        then: "pipeline created"
        assert result.pipeline['pipelineName'] == pipelineName

        when:
        result = dsl "getStage(projectName: '$testProjName', pipelineName: '$pipelineName', stageName: '$pipeStageName')"

        then: "stage created"
        assert result.stage['stageName'] == pipeStageName

        when:
        result = dsl "getTask(projectName: '$testProjName', pipelineName: '$pipelineName', stageName: '$pipeStageName', taskName: '$pipeTaskName')"

        then: "task created"
        assert result.task['taskName'] == pipeTaskName
        assert result.task['actualParameters']['parameterDetail'].parameterName[0] == 'commandToRun'
        assert result.task['actualParameters']['parameterDetail'].parameterValue[0] == 'echo task %%1 completed'

        cleanup:
        deleteProjects([projectName: testProjName], false)
    }

    def "install project with spaces in names"() {
        def testProjName = 'old test Import'
        def procName = 'A Procedure #1'
        def procStepName = 'step 1'
        def pipelineName = 'Verify QA, Notify'
        def pipeStageName = 'Stage 1'
        def pipeTaskName = 'task 1'

        given: "spec_symbols project code"

        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/spec_symbols/projects/$testProjName",
            projName: "$testProjName"
          ]
        )""")

        then: "job completed"
        waitUntil {
            assert p.jobId
            assert getJobProperty("outcome", p.jobId) == "success" || getJobProperty("outcome", p.jobId) == "warning"
        }

        when:
        def result = dsl "getProject(projectName: '$testProjName')"

        then: "project created"
        assert result.project['projectName'] == testProjName

        when:
        result = dsl "getProcedure(projectName: '$testProjName', procedureName: '$procName')"

        then:"procedure created"
        assert result.procedure['procedureName'] == procName

        when:
        result = dsl "getStep(projectName: '$testProjName', procedureName: '$procName', stepName: '$procStepName')"

        then: "step created"
        assert result.step['stepName'] == procStepName
        assert result.step['command'] == "echo Procedure is completed"

        when:
        result = dsl "getPipeline(projectName: '$testProjName', pipelineName: '$pipelineName')"

        then: "pipeline created"
        assert result.pipeline['pipelineName'] == pipelineName

        when:
        result = dsl "getStage(projectName: '$testProjName', pipelineName: '$pipelineName', stageName: '$pipeStageName')"

        then: "stage created"
        assert result.stage['stageName'] == pipeStageName

        when:
        result = dsl "getTask(projectName: '$testProjName', pipelineName: '$pipelineName', stageName: '$pipeStageName', taskName: '$pipeTaskName')"

        then: "task created"
        assert result.task['taskName'] == pipeTaskName
        assert result.task['actualParameters']['parameterDetail'].parameterName[0] == 'commandToRun'
        assert result.task['actualParameters']['parameterDetail'].parameterValue[0] == 'echo task 1 completed'

        cleanup:
        deleteProjects([projectName: testProjName], false)
    }
}
