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
