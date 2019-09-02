package com.electriccloud.plugin.spec

/*
Copyright 2018-2019 Electric Cloud, Inc.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import com.electriccloud.spec.*

class PluginTestHelper extends PluginSpockTestSupport {
  static def automationTestsContextRun = System.getenv ('AUTOMATION_TESTS_CONTEXT_RUN') ?: ''
  static def defaultPool = System.getenv('DSL_DEPLOY_POOL') ?: 'local'
  static def password = System.getProperty("COMMANDER_PASSWORD", "changeme")

  def redirectLogs(String parentProperty = '/myJob') {
    def propertyLogName = parentProperty + '/debug_logs'
    dsl """
      setProperty(
        propertyName: "/plugins/EC-DslDeploy/project/ec_debug_logToProperty",
        value: "$propertyLogName"
      )
    """
    return propertyLogName
  }

  def getJobLogs(def jobId) {
    assert jobId
    def logs
    try {
        logs = getJobProperty("/myJob/debug_logs", jobId)
    } catch (Throwable e) {
        logs = "Possible exception in logs; check job"
    }
    logs
  }

  def runProcedureDsl(dslString) {
    redirectLogs()
    assert dslString

    def result = dsl(dslString)
    assert result.jobId
    waitUntil {
      jobCompleted result.jobId
    }
    def logs = getJobLogs(result.jobId)
    def outcome = jobStatus(result.jobId).outcome
    logger.debug("DSL: $dslString")
    logger.debug("Logs: $logs")
    logger.debug("Outcome: $outcome")
    [logs: logs, outcome: outcome, jobId: result.jobId]
  }

  def getP(String path) {
    def result = dsl """getProperty(propertyName: "$path")"""
    return result?.property?.value
  }

  def getStepProperty(def jobId, def stepName, def propName) {
    assert jobId
    assert propName

    def prop
    def property = "/myJob/jobSteps/$stepName/$propName"
    println "Trying to get property: $property, jobId: $jobId"
    try {
      prop = getJobProperty(property, jobId)
    } catch (Throwable e) {
      logger.debug("Can't retrieve Upper Step Summary from the property: '$property'; check job: " + jobId)
    }
    return prop
  }

  def getStepSummary(def jobId, def stepName){
    return getStepProperty(jobId, stepName, "summary")
  }

  def conditionallyDeleteProject(String projectName){
    if (System.getenv("LEAVE_TEST_PROJECTS")){
      return
    }
    dsl "deleteProject(projectName: '$projectName')"
  }

//
// Copied from PluginSpockTestSupport.groovy
//

  def runCommand(command) {
    logger.debug("Command: $command")
    def stdout = new StringBuilder()
    def stderr = new StringBuilder()
    def process = command.execute()
    process.consumeProcessOutput(stdout, stderr)
    process.waitForOrKill(50 * 1000)
    logger.debug("STDOUT: $stdout")
    logger.debug("STDERR: $stderr")
    logger.debug("Exit code: ${process.exitValue()}")
    def text = "$stdout\n$stderr"
    if (process.exitValue() != 0) {
      throw new RuntimeException("Process failed: $text")
    }
    assert process.exitValue() == 0
    text
  }


  def publishArtifactVersion(String artifactName, String version, String dir) {
    String commanderServer = System.getProperty("COMMANDER_SERVER") ?: 'localhost'
    String username = System.getProperty('COMMANDER_USER') ?: 'admin'
    String password = System.getProperty('COMMANDER_PASSWORD') ?: 'changeme'
    String commanderHome = System.getenv('COMMANDER_HOME') ?: '/opt/EC/'
    assert commanderHome: "Env COMMANDER_HOME must be provided"

    String ectoolPath
    if (System.properties['os.name'].toLowerCase().contains('windows')) {
      ectoolPath = "bin/ectool.exe"
    } else {
      ectoolPath = "bin/ectool"
    }
    File ectool = new File(commanderHome, ectoolPath)
    assert ectool.exists(): "File ${ectool.absolutePath} does not exist"

    logger.debug("ECTOOL PATH: " + ectool.absolutePath.toString())

    String command = "${ectool.absolutePath} --server $commanderServer "
    runCommand("${command} login ${username} ${password}")

    runCommand("${command} deleteArtifactVersion ${artifactName}:${version}")

    String publishCommand = "${command} publishArtifactVersion --version $version --artifactName ${artifactName} "
    publishCommand += "--fromDirectory $dir"
    runCommand(publishCommand)
  }

}
