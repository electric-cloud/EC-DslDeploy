package com.electriccloud.plugin.spec
import spock.lang.*

class issue34 extends PluginTestHelper {
  static String pName='EC-DslDeploy'
  @Shared String pVersion
  @Shared String plugDir
  static String projName="issue34"

  def doSetupSpec() {
    pVersion = getP("/plugins/$pName/pluginVersion")
    plugDir = getP("/server/settings/pluginsDirectory")
    dsl """
      deleteProject(projectName: "$projName")
    """
  }

  def doCleanupSpec() {
    conditionallyDeleteProject(projName)
    dsl """
      deleteUser(userName: "user34")
   """
  }

  // Check sample
  def "issue34 test suite upload"() {
    given: "the isssue34 code"
    when: "Load DSL Code"
      def p= runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/issue34",
            pool: "$defaultPool"
          ]
        )""")
    then: "job succeeds"
      assert p.jobId
      assert getJobProperty("outcome", p.jobId) == "success"

     // check ACLs on project
     then: "ACL entry on project exists"
      println "Checking ACL"
      def ace=dsl """ getAclEntry(
        projectName: "$projName",
        principalType: 'user',
        principalName: 'user34'
      )"""
      assert ace.aclEntry.readPrivilege    == "allow"
      assert ace.aclEntry.modifyPrivilege  == "deny"
      assert ace.aclEntry.executePrivilege == "inherit"

     // check ACLs on environment
     then: "ACL entry on enviroment exists"
      println "Checking ACL"
      def ace2=dsl """ getAclEntry(
        projectName: "$projName",
        environmentName: 'env34',
        principalType: 'user',
        principalName: 'user34'
      )"""
      assert ace2.aclEntry.readPrivilege    == "allow"
      assert ace2.aclEntry.modifyPrivilege  == "deny"
      assert ace2.aclEntry.executePrivilege == "inherit"

 }
}
