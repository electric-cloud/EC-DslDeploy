package com.electriccloud.plugin.spec

import spock.lang.*

class Properties
    extends PluginTestHelper
{

  static String pName='EC-DslDeploy'
  static String jira="BEE18910"

  @Shared String pVersion
  @Shared String plugDir

  def doSetupSpec()
  {
    dsl """ deleteProject(projectName: "$jira") """
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
  }

  def doCleanupSpec()
  {
    conditionallyDeleteProject("$jira")
  }

  // check that details for property and property sheet are imported
  def "property details import"()
  {
    given:

    when: "Load DSL code"
      def result= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/$jira",
            pool: "$defaultPool"
          ]
        )""")
    then:
      assert result.jobId
      def outcome=getJobProperty("outcome", result.jobId)
      assert outcome == "success"

      // Properties are created properly
      println "Checking properties"
      def prop1 = dsl "getProperty propertyName: '/projects/$jira/prop1'"
      assert prop1
      assert prop1.property.value == "prop1"
      assert prop1.property.description == "prop1"

      def propSheet1 = dsl "getProperty propertyName: '/projects/$jira/propSheet1'"
      assert propSheet1
      assert propSheet1.property.description == "propSheet1"
  }

  def "round trip for complex project with properties"() {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load complex project with properties from single DSL file"
    dslFile("complex_project.dsl")

    when: "Generate DSL files"
    def result1 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "generateDslToDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            includeAllChildren: '1',
            suppressNulls: '1',
            objectType: 'project',
            objectName: 'proj_name',
            httpIdleTimeout: '180'
          ]
        )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Import DSL files"
    def result2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'proj_name', c1: 'comp_name1', c2: 'comp_name2', p1: 'proc_name1', p2: 'proc_name2'])
    new File(dslDir).deleteDir()
  }

  def "multiline properties"() {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load project with multiline properties from single DSL file"
    dslFile("project_with_multiline_properties.dsl")

    when: "Generate DSL files"
    def result1 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "generateDslToDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            includeAllChildren: '1',
            suppressNulls: '1',
            objectType: 'project',
            objectName: 'BEE-30105',
            httpIdleTimeout: '210'
          ]
        )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Import DSL files"
    def result2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'BEE-30105', c1: 'comp_name1', c2: 'comp_name2', p1: 'proc_name1', p2: 'proc_name2'])
    new File(dslDir).deleteDir()
  }

  def "BEE-33074 property name with with encoded character"() {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load project with property name that contains special character that should be encoded/decoded"
    dslFile("project_with_property_name_with_encoded_character.dsl")

    when: "Generate DSL files"
    def result1 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "generateDslToDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            includeAllChildren: '1',
            suppressNulls: '1',
            objectType: 'project',
            objectName: 'BEE-33074',
            httpIdleTimeout: '240'
          ]
        )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Import DSL files"
    def result2 = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$dslDir",
            pool: "$defaultPool",
            overwrite: '1'
          ]
        )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"
    and: "check number of existing properties"
    def properties = dsl """getProperties(projectName: 'BEE-33074')"""

    assert properties?.propertySheet?.property.size() == 1

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'BEE-30105', c1: 'comp_name1', c2: 'comp_name2', p1: 'proc_name1', p2: 'proc_name2'])
    new File(dslDir).deleteDir()
  }

  // check that entities with special characters in the name are imported successfully
  // special characters: ?<>%*!@#$^&()|:.
  def "nested entities with special characters in name"()
  {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load entities"
    dslFile("nested_entities_special_chars.dsl")

    when: "Generate DSL files"
    def result1 = runProcedureDsl("""
          runProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "generateDslToDirectory",
            actualParameter: [
              directory: "$dslDir",
              pool: "$defaultPool",
              includeAllChildren: '1',
              suppressNulls: '1',
              objectType: 'project',
              objectName: 'projectName ?<>%*!@#\$^&()|:. projectName',
              httpIdleTimeout: '270'
            ]
          )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Import DSL files in overwrite mode with debug enabled"
    def result2 = runProcedureDsl("""
          runProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "installDslFromDirectory",
            actualParameter: [
              directory: "$dslDir",
              pool: "$defaultPool",
              additionalDslArguments: "--debug 1",
              overwrite: '1'
            ]
          )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"

    and:
    // Properties are created properly
    println "Checking properties"
    def propSheet1 = dsl "getProperty propertyName: '/projects/projectName ?<>%*!@#\$^&()|:. projectName/pipelines/pipelineName ?<>%*!@#\$^&()|:. pipelineName/stages/stageName ?<>%*!@#\$^&()|:. stageName/tasks/taskName ?<>%*!@#\$^&()|:. taskName/properties/propertySheetName ?<>%*!@#\$^&()|:. propertySheetName'"
    assert propSheet1
    and:
    def prop1 = dsl "getProperty propertyName: '/projects/projectName ?<>%*!@#\$^&()|:. projectName/pipelines/pipelineName ?<>%*!@#\$^&()|:. pipelineName/stages/stageName ?<>%*!@#\$^&()|:. stageName/tasks/taskName ?<>%*!@#\$^&()|:. taskName/properties/propertySheetName ?<>%*!@#\$^&()|:. propertySheetName/propertyName ?<>%*!@#\$^&()|:. propertyName'"
    assert prop1
    assert prop1.property.value == "?<>%*!@#\$^&()|:."

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'projectName ?<>%*!@#$^&()|:. projectName'])
    new File(dslDir).deleteDir()
  }

  // check that entities with puncted characters in the name are imported successfully
  // punctuated characters: ÒÓÔÕÖ×ØÙÚÛÜÝÞß
  def "nested entities with names contain punctuated characters"()
  {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load entities"
    dslFile("nested_entities_punctuated_chars.dsl")

    when: "Generate DSL files"
    def result1 = runProcedureDsl("""
          runProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "generateDslToDirectory",
            actualParameter: [
              directory: "$dslDir",
              pool: "$defaultPool",
              includeAllChildren: '1',
              suppressNulls: '1',
              objectType: 'project',
              objectName: 'projectName ÒÓÔÕÖ×ØÙÚÛÜÝÞß projectName',
              httpIdleTimeout: '270'
            ]
          )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Import DSL files in overwrite mode with debug enabled"
    def result2 = runProcedureDsl("""
          runProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "installDslFromDirectory",
            actualParameter: [
              directory: "$dslDir",
              pool: "$defaultPool",
              additionalDslArguments: "--debug 1",
              overwrite: '1'
            ]
          )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"

    and:
    // Properties are created properly
    println "Checking properties"
    def propSheet1 = dsl "getProperty propertyName: '/projects/projectName ÒÓÔÕÖ×ØÙÚÛÜÝÞß projectName/pipelines/pipelineName ÒÓÔÕÖ×ØÙÚÛÜÝÞß pipelineName/stages/stageName ÒÓÔÕÖ×ØÙÚÛÜÝÞß stageName/tasks/taskName ÒÓÔÕÖ×ØÙÚÛÜÝÞß taskName/properties/propertySheetName ÒÓÔÕÖ×ØÙÚÛÜÝÞß propertySheetName'"
    assert propSheet1
    and:
    def prop1 = dsl "getProperty propertyName: '/projects/projectName ÒÓÔÕÖ×ØÙÚÛÜÝÞß projectName/pipelines/pipelineName ÒÓÔÕÖ×ØÙÚÛÜÝÞß pipelineName/stages/stageName ÒÓÔÕÖ×ØÙÚÛÜÝÞß stageName/tasks/taskName ÒÓÔÕÖ×ØÙÚÛÜÝÞß taskName/properties/propertySheetName ÒÓÔÕÖ×ØÙÚÛÜÝÞß propertySheetName/propertyName ÒÓÔÕÖ×ØÙÚÛÜÝÞß propertyName'"
    assert prop1
    assert prop1.property.value == "ÒÓÔÕÖ×ØÙÚÛÜÝÞß"

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'projectName ÒÓÔÕÖ×ØÙÚÛÜÝÞß projectName'])
    new File(dslDir).deleteDir()
  }

  def "BEE-33683: import new and/or nested properties in the overwrite mode"()
  {
    def dslDir = '/tmp/' + randomize('dsl')

    given: "Load entities"
    dslFile("new_nested_properties_in_overwrte.dsl")

    when: "Generate DSL files with properties"
    def result1 = runProcedureDsl("""
          runProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "generateDslToDirectory",
            actualParameter: [
              directory: "$dslDir",
              pool: "$defaultPool",
              includeAllChildren: '1',
              suppressNulls: '1',
              objectType: 'project',
              objectName: 'BEE-33683',
              httpIdleTimeout: '270'
            ]
          )""")
    then:
    assert result1.jobId
    def outcome1 = getJobProperty("outcome", result1.jobId)
    assert outcome1 == "success"

    when: "Remove properties"
    dsl "deleteProperty propertyName: '/projects/BEE-33683/procedures/BEE-33683/properties/testPropertySheet'"
    then:
    def propSheet1 = dsl "getProperty propertyName: '/projects/BEE-33683/procedures/BEE-33683/properties/testPropertySheet'"
    assert !propSheet1
    and:
    def prop1 = dsl "getProperty propertyName: '/projects/BEE-33683/procedures/BEE-33683/properties/testPropertySheet/testProperty'"
    assert !prop1

    when: "Import DSL files in overwrite mode with debug enabled"
    def result2 = runProcedureDsl("""
          runProcedure(
            projectName: "/plugins/$pName/project",
            procedureName: "installDslFromDirectory",
            actualParameter: [
              directory: "$dslDir",
              pool: "$defaultPool",
              additionalDslArguments: "--debug 1",
              overwrite: '1'
            ]
          )""")
    then:
    assert result2.jobId
    def outcome2 = getJobProperty("outcome", result2.jobId)
    assert outcome2 == "success"

    and:
    // Properties are created properly
    def propSheet2 = dsl "getProperty propertyName: '/projects/BEE-33683/procedures/BEE-33683/properties/testPropertySheet'"
    assert propSheet2
    and:
    def prop2 = dsl "getProperty propertyName: '/projects/BEE-33683/procedures/BEE-33683/properties/testPropertySheet/testProperty'"
    assert prop2
    assert prop2.property.value == "testValue"

    cleanup:
    deleteProjects([projectName: jira], false)
    deleteProjects([mainProject: 'BEE-33683'])
    new File(dslDir).deleteDir()
  }
}
