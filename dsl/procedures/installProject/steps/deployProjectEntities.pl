$[/myProject/scripts/perlHeaderJSON]

# Check if agent and server support clientFiles argument in evalDsl operation
my $clientFilesCompatible = checkClientFilesCompatibility();

# deploy sub project entities
my @subProjectEntities = ("project", "credentialProvider", "credential",
"pluginConfiguration","procedure", "resourceTemplate", "workflowDefinition",
"environmentTemplate", "environment", "component",
"application", "pipeline", "release", "schedule", "catalog", "report", "dashboard");

foreach my $objectType (@subProjectEntities ) {
    my $shell   = 'ectool --timeout $[/server/EC-DslDeploy/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]';

    # without Perl variables usage / substitution 
    my $command1 = <<'END_COMMAND';
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/Utils]

// Variables available for use in DSL code
def projectName = '$[projName]'
def projectDir = '$[projDir]'
def overwrite = '$[overwrite]'
def ignoreFailed = '$[ignoreFailed]'
END_COMMAND

    # with Perl variables usage / substitution 
    my $command2;


    # check if corresponding directory exists
    if ("project" eq "$objectType" && -d '$[projDir]/') {
        $command2 = <<"END_COMMAND";
def counter

project projectName, {
  counter = loadProject(projectDir, projectName, overwrite)
  loadProjectProperties(projectDir, projectName, overwrite)
  loadProjectAcls(projectDir, projectName)
}

if (counter == 0) {
  setProperty(propertyName: "summary", value: "No project.groovy or project.dsl found")
  setProperty(propertyName: "outcome", value: "warning")
}
return ""
END_COMMAND
    } elsif ("project" ne "$objectType" && -d '$[projDir]/' . pluralForm("$objectType")) {
        $command2 = <<"END_COMMAND";
def counters

project projectName, {
  counters = loadObjects("$objectType", projectDir, "/projects/" + projectName,
    [projectName: projectName, projectDir: projectDir], overwrite, ignoreFailed
  )
}

//pop up possible error
if (counters.get("Error") != null) {
  println("Error: " + counters.get("Error"))
  counters.remove("Error")
}

setProperty(propertyName: "summary", value: summaryString(counters))
return ""
END_COMMAND
    } else {
        $command2 = <<"END_COMMAND";

setProperty(propertyName: "summary", value: "no " + pluralForm("$objectType"))
END_COMMAND
    }

    my $command   = "$command1" . "$command2";
    my $localMode = '$[localMode]';

    # check support of clientFiles argument
    if ($localMode eq '0' && $clientFilesCompatible) {
        $shell .= " --clientFiles \"$[projDir]\"";
    }

    $ec->createJobStep({
        jobStepName   => "deploy $objectType",
        command       => "$command",
        shell         => "$shell",
        postProcessor => "postp"});
}
