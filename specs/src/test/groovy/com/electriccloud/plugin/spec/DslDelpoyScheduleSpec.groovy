package com.electriccloud.plugin.spec

import spock.lang.Shared

class DslDelpoyScheduleSpec
    extends PluginTestHelper {
    static String pName = 'EC-DslDeploy'
    @Shared
    String pVersion
    @Shared
    String plugDir

    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
    }

    // overwrite with pipeline
    def "deploy multiple schedules"(){

        def projectName = 'schedule_project'
        def projects = [project : projectName]

        given: "the schedule project code"
        when: "Load DSL Code"
        def p = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installProject",
          actualParameter: [
            projDir: "$plugDir/$pName-$pVersion/lib/dslCode/schedules/deploySchedules/projects/$projectName",
            projName: '$projectName'
          ]
        )""")
        then: "job completes successfully"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        then: "verify schedule was creared"
        def schedule = dsl """getSchedule(projectName: '$projectName', scheduleName: 'mySchedule')"""
        assert schedule

        then: "verify CI schedule was created"
        def scheduleCI = dsl """getSchedule(projectName: '$projectName', scheduleName: 'myCISchedule')"""
        assert scheduleCI

        cleanup:
        deleteProjects(projects, false)

    }
}
