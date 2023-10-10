package com.electriccloud.plugin.spec

import spock.lang.Shared

class IncludeExcludeObjectsSpec extends PluginTestHelper {

    static String pName='EC-DslDeploy'
    @Shared String pVersion
    @Shared String plugDir
    static String projName="testPartial"


    def doSetupSpec() {
        pVersion = getP("/plugins/$pName/pluginVersion")
        plugDir = getP("/server/settings/pluginsDirectory")
        dsl "deleteUser(userName: 'user1')"
        dsl "deleteUser(userName: 'user2')"
    }

    def "test Utils.groovy isIncluded"() {
        def cmd = """
\$[/plugins/EC-DslDeploy/project/scripts/Utils]

assert !isIncluded(["/groups"], ["/projects", "/users"], "/projects")
assert isIncluded([], [], "/projects")
assert isIncluded(["/projects", "/users", ], [], "/projects")
assert isIncluded([ "/projects", "/users"], [], "/projects/Default")
//
assert isIncluded(["/users", "/projects/Default"], [],
        "/projects")
assert isIncluded(["/users", "/projects/Default/releases"], [],
        "/projects")
assert isIncluded(["/users", "/projects/Default/releases/release1"], [],
        "/projects")
//
assert isIncluded(["/users", "/projects/Default"], [],
        "/projects/Default")
assert isIncluded(["/users", "/projects/Default/releases"], [],
        "/projects/Default")
assert isIncluded(["/users", "/projects/Default/releases/release1"], [],
        "/projects/Default")
//
assert isIncluded(["/users", "/projects/Default"], [],
        "/projects/Default/releases")
assert isIncluded(["/users", "/projects/Default/releases"], [],
        "/projects/Default/releases")
assert isIncluded(["/users", "/projects/Default/releases/release1"], [],
        "/projects/Default/releases")

//
assert isIncluded(["/users", "/projects/Default"], [],
        "/projects/Default/releases/release1")
assert isIncluded(["/users", "/projects/Default/releases"], [],
        "/projects/Default/releases/release1")
assert isIncluded(["/users", "/projects/Default/releases/release1"], [],
        "/projects/Default/releases/release1")

assert !isIncluded(["/users", "/projects/Default/releases/release1"],
        [],
        "/projects/Default/releases/release2")

assert !isIncluded(["/users", "/projects/Default/releases"],
        ["/projects/Default/releases/release2"],
        "/projects/Default/releases/release2")

assert !isIncluded(["/users", "/projects/Default/releases"],
        [],
        "/projects/Default/applications")


assert !isIncluded(["/users", "/projects/Default/releases"],
        [],
        "/projects/Default/applications/app1")


assert !isIncluded(["/users", "/projects/Default"],
        ["/projects/Default/releases"],
        "/projects/Default/releases")

assert isIncluded(["/users", "/projects/Default"],
        ["/projects/Default/releases"],
        "/projects/Default/applications")

assert !isIncluded(["/users", "/projects/Default"],
        ["/projects/Default/releases"],
        "/projects/Default/releases")


//
assert !isIncluded(["/users", "/projects"], ["/projects/Default"],
        "/projects/Default")

assert !isIncluded(["/users", "/projects"], ["/projects/Default"],
        "/projects/Default/applications")

assert !isIncluded(["/users"], [],
        "/projects/Default/applications")

assert !isIncluded([], ["/users", "/projects"], "/projects")
assert !isIncluded([], ["/users", "/projects"], "/projects/Default")

//
assert isIncluded(["/projects/*/applications"], [],
        "/projects")
assert isIncluded(["/projects/*/applications"], [],
        "/projects/Default")
assert isIncluded(["/projects/*/applications"], [],
        "/projects/Default/applications")
assert isIncluded(["/projects/*/applications"], [],
        "/projects/Default/applications/app1")

//
assert isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects")
assert isIncluded(["/projects/*/applications"], [],
        "/projects/Default")

assert isIncluded(["/projects/*/applications"], [],
        "/projects/Default/applications")
assert isIncluded(["/projects/*/applications"], [],
        "/projects/Default/applications/app1")
//


assert isIncluded(["/projects"], ["/projects/Default/applications"],
        "/projects/Default")
assert !isIncluded(["/projects"], ["/projects/Default/applications"],
        "/projects/Default/applications")

//

assert isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects/Default")

assert isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects")

assert isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects/Default/releases")

assert isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects/Default/releases/test")

assert !isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects/Default/applications")

assert !isIncluded(["/projects"], ["/projects/*/applications"],
        "/projects/Default/applications/app1")

//
assert isIncluded(["/projects", "/projects/Default/applications"],
        ["/projects/*/applications"], "/projects/Default/applications")

assert isIncluded(["/projects", "/projects/Default/applications"],
        ["/projects/*/applications"],
        "/projects/Default/applications/app1")

assert isIncluded(["/projects", "/projects/Default/applications/app1"],
        ["/projects/*/applications"],
        "/projects/Default/applications")

assert isIncluded(["/projects",
                   "/users"],
        [], "/projects/Default/applications")
"""
        given:
        dsl ("""
project '$projName', {
    procedure 'test-procedure', {
        step 'check', {
            command = '''$cmd'''
            shell = 'ec-groovy'
        }
    }
}
""")

        when: 'run procedure'
        def p = runProcedureDsl("""
        runProcedure(
          projectName: '$projName',
          procedureName: "test-procedure"
        )
""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        cleanup:
            deleteProjects([projectName: projName])
    }

    def "test perlHeaderJSON.pl isIncluded"() {
        def cmd = """
\$[/plugins/EC-DslDeploy/project/scripts/perlHeaderJSON]

die "Condition not true" unless(isIncluded("", "", "/projects"));
die "Condition not true" unless(isIncluded("/projects", "", "/projects"));
#
die "Condition not true" unless (!isIncluded("/groups", "/projects\\n/users", "/projects"));
die "Condition not true" unless (isIncluded("/projects\\n/users", "", "/projects"));
die "Condition not true" unless (isIncluded("/projects\\n/users", "", "/projects/Default"));
#
die "Condition not true" unless(isIncluded("/users\\n/projects/Default", "","/projects"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases", "", "/projects"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases/release1", "", "/projects"));
#
die "Condition not true" unless(isIncluded("/users\\n/projects/Default", "", "/projects/Default"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases", "", "/projects/Default"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases/release1", "", "/projects/Default"));
#
die "Condition not true" unless(isIncluded("/users\\n/projects/Default", "", "/projects/Default/releases"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases", "", "/projects/Default/releases"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases/release1", "", "/projects/Default/releases"));
#
die "Condition not true" unless(isIncluded("/users\\n/projects/Default", "", "/projects/Default/releases/release1"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases", "", "/projects/Default/releases/release1"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default/releases/release1", "", "/projects/Default/releases/release1"));
die "Condition not true" unless(!isIncluded("/users\\n/projects/Default/releases/release1","","/projects/Default/releases/release2"));
die "Condition not true" unless(!isIncluded("/users\\n/projects/Default/releases", "/projects/Default/releases/release2","/projects/Default/releases/release2"));
die "Condition not true" unless(!isIncluded("/users\\n/projects/Default/releases", "", "/projects/Default/applications"));
die "Condition not true" unless(!isIncluded("/users\\n/projects/Default/releases", "", "/projects/Default/applications/app1"));
die "Condition not true" unless(!isIncluded("/users\\n/projects/Default", "/projects/Default/releases", "/projects/Default/releases"));
die "Condition not true" unless(isIncluded("/users\\n/projects/Default", "/projects/Default/releases", "/projects/Default/applications"));
die "Condition not true" unless(!isIncluded("/users\\n/projects/Default", "/projects/Default/releases", "/projects/Default/releases"));
#
die "Condition not true" unless(!isIncluded("/users\\n/projects", "/projects/Default",  "/projects/Default"));
die "Condition not true" unless(!isIncluded("/users\\n/projects", "/projects/Default", "/projects/Default/applications"));
die "Condition not true" unless(!isIncluded("/users", "", "/projects/Default/applications"));
die "Condition not true" unless(!isIncluded("", "/users\\n/projects", "/projects"));
die "Condition not true" unless(!isIncluded("", "/users\\n/projects", "/projects/Default"));
#
die "Condition not true" unless(isIncluded("/projects/*/applications", "", "/projects"));
die "Condition not true" unless(isIncluded("/projects/*/applications", "", "/projects/Default"));
die "Condition not true" unless(isIncluded("/projects/*/applications", "",  "/projects/Default/applications"));
die "Condition not true" unless(isIncluded("/projects/*/applications", "",  "/projects/Default/applications/app1"));
#
die "Condition not true" unless(isIncluded("/projects", "/projects/*/applications", "/projects"));
die "Condition not true" unless(isIncluded("/projects/*/applications", "" , "/projects/Default"));
die "Condition not true" unless(isIncluded("/projects/*/applications", "", "/projects/Default/applications"));
die "Condition not true" unless(isIncluded("/projects/*/applications", "", "/projects/Default/applications/app1"));
#
die "Condition not true" unless(isIncluded("/projects", "/projects/Default/applications", "/projects/Default"));
die "Condition not true" unless(!isIncluded("/projects", "/projects/Default/applications", "/projects/Default/applications"));
#
die "Condition not true" unless(isIncluded("/projects", "/projects/*/applications", "/projects/Default"));
die "Condition not true" unless(isIncluded("/projects", "/projects/*/applications",  "/projects"));
die "Condition not true" unless(isIncluded("/projects", "/projects/*/applications", "/projects/Default/releases"));
die "Condition not true" unless(isIncluded("/projects", "/projects/*/applications", "/projects/Default/releases/test"));
die "Condition not true" unless(!isIncluded("/projects", "/projects/*/applications", "/projects/Default/applications"));
die "Condition not true" unless(!isIncluded("/projects", "/projects/*/applications", "/projects/Default/applications/app1"));
#
die "Condition not true" unless(isIncluded("/projects\\n/projects/Default/applications", "/projects/*/applications", "/projects/Default/applications"));
die "Condition not true" unless(isIncluded("/projects\\n/projects/Default/applications", "/projects/*/applications",  "/projects/Default/applications/app1"));
die "Condition not true" unless(isIncluded("/projects\\n/projects/Default/applications/app1", "/projects/*/applications",  "/projects/Default/applications"));
die "Condition not true" unless(isIncluded("/projects\\n/users", "" , "/projects/Default/applications"));
"""
        given:
        dsl ("""
project '$projName', {
    procedure 'test-procedure', {
        command = '''$cmd'''
          shell = 'cb-perl'
    }
}
""")

        when: 'run procedure'
        def p = runProcedureDsl("""
        runProcedure(
          projectName: '$projName',
          procedureName: "test-procedure"
        )
""")
        then: "job succeeds"
        assert p.jobId
        assert getJobProperty("outcome", p.jobId) == "success"

        cleanup:
        deleteProjects([projectName: projName])
    }


    def "import with includeObjects/excludeObjects"() {

        when: "Load DSL Code"
        def proc = runProcedureDsl("""
        runProcedure(
          projectName: "/plugins/$pName/project",
          procedureName: "installDslFromDirectory",
          actualParameter: [
            directory: "$plugDir/$pName-$pVersion/lib/dslCode/partial",
            pool: "$defaultPool",
            localMode: '0',
            includeObjects: '''/users
/groups
/projects/*/applications
/projects/proj1/pipelines/pipe1''',
            excludeObjects: '''/projects/proj3
/projects/proj1/applications/app1
/users/user1'''    
          ]
        )""")

        then: "job completes"
        assert proc.jobId
        assert getJobProperty("outcome", proc.jobId) == "success"

        and: "check user1 is not created"
        def response = dsl "getUser(userName: 'user1')", /*args*/ null,
                [ignoreStatusCode: true]
        assert response?.error?.message =~ "NoSuchUser"

        and: "check user2 is created"
        def response2 =  dsl "getUser(userName: 'user2')"
        assert response2?.user

        and: "check group1 is created"
        def response3 =  dsl "getGroup(groupName: 'group1')"
        assert response3?.group

        and: "check personaPage1 is not created"
        def response4 =  dsl "getPersonaPage(personaPageName: 'user1')",
                /*args*/
                null, [ignoreStatusCode: true]
        assert response4?.error?.message =~ "NoSuchPersonaPage"

        and: "check app1 in proj1 is not created"
        def response5 =  dsl """getApplication(projectName: 'proj1', 
                applicationName: 'app1')""", /*args*/
                null, [ignoreStatusCode: true]
        assert response5?.error?.message =~ "NoSuchApplication"

        and: "check app2 in proj1 is created"
        def response6 =  dsl """getApplication(projectName: 'proj1', 
                              applicationName: 'app2')"""
        assert response6?.application

        and: "check app1 in proj2 is created"
        def response7 =  dsl """getApplication(projectName: 'proj2', 
                               applicationName: 'app1')"""
        assert response7?.application

        and: "check proj3 is not created"
        def response8 =  dsl """getProject(projectName: 'proj3')""", /*args*/
                null, [ignoreStatusCode: true]
        assert response8?.error?.message =~ "NoSuchProject"

        and: "check pipe1 in proj1 is created"
        def response9 =  dsl """getPipeline(projectName: 'proj1', 
                               pipelineName: 'pipe1')"""
        assert response9?.pipeline

        and: "check pipe2 in proj1 is not created"
        def response10 =  dsl """getPipeline(projectName: 'proj1', 
                               pipelineName: 'pipe2')""",  null,
                [ignoreStatusCode: true]
        assert response10?.error?.message =~ "NoSuchPipeline"


        and: "check pipelines in proj2 is not created"
        def response11 =  dsl """getPipelines(projectName: 'proj2')"""
        assert !response11?.pipeline || response.pipeline.size() == 0


        cleanup:
        deleteProjects([pr1: 'proj1', pr2: 'proj2'], false)
        dsl ("deleteUser (userName: 'user1')")
        dsl ("deleteGroup (groupName: 'group1')")
    }

}
