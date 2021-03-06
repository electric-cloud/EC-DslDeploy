$[/myProject/scripts/perlHeaderJSON]

# Check if agent and server support clientFiles argument in evalDsl operation
my $clientFilesCompatible = checkClientFilesCompatibility();

# deploy non project entities
my @nonProjectEntities = ("tag", "personaPage", "personaCategory", "persona", "user", "group", "reportObjectType", "resource", "resourcePool");

foreach my $objectType (@nonProjectEntities) {
    my $resource = '$[pool]';
    my $shell    = 'ectool --timeout $[/server/EC-DslDeploy/timeout] evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]';

    # without Perl variables usage / substitution 
    my $command1 = <<'END_COMMAND';
import groovy.io.FileType
import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.util.BaseObject

//noinspection GroovyUnusedAssignment
@BaseScript BaseObject baseScript

$[/myProject/scripts/Utils]
END_COMMAND

    # with Perl variables usage / substitution 
    my $command2;   

    # check if corresponding directory exists
    if (-d pluralForm("$objectType")) {
$command2 = <<"END_COMMAND";
def absDir    = '$[/myJob/CWD]'
def overwrite = '$[overwrite]'
def ignoreFailed = '$[ignoreFailed]'
File dir      = new File(absDir, pluralForm("$objectType"))

if (dir.exists()) {
  def counters = loadObjects("$objectType", absDir, "/", [:], overwrite, ignoreFailed)
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName: "summary", value: "no " + pluralForm("$objectType"))
}
END_COMMAND

        my $localMode = '$[localMode]';

        # check support of clientFiles argument
        if ($localMode eq '0' && $clientFilesCompatible) {
            $shell .= " --clientFiles \"$[directory]\""
        }
    } else {
        $command2 = <<"END_COMMAND";

setProperty(propertyName: "summary", value: "no " + pluralForm("$objectType"))
END_COMMAND
    }

    my $command  = "$command1" . "$command2";

    $ec->createJobStep({
        jobStepName   => "deploy $objectType",
        command       => "$command",
        resourceName  => "$resource",
        shell         => "$shell",
        postProcessor => "postp"});
}
