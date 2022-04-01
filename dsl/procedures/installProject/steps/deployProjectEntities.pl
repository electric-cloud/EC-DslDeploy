$[/myProject/scripts/perlHeaderJSON]

# Check if agent and server support clientFiles argument in evalDsl operation
my $clientFilesCompatible = checkClientFilesCompatibility();

# deploy sub project entities
my @subProjectEntities = ("project", "credentialProvider", "credential",
    "pluginConfiguration","procedure", "resourceTemplate", "workflowDefinition",
    "environmentTemplate", "environment", "component",
    "application", "pipeline", "release", "schedule", "catalog", "report", "dashboard");

my ($userTimeout) = ("$[additionalDslArguments]" =~ m/--timeout\s+([0-9]+)/);
print("User timeout is: '$userTimeout'\n");

my $pluginTimeout = $[/server/EC-DslDeploy/timeout];
print("Plugin timeout is: '$pluginTimeout'\n");

my $timeout = $userTimeout;
if ("$timeout" eq "") {
    $timeout = $pluginTimeout;
}
print("Timeout is: '$timeout'\n");

# Is this an incremental import?
my $incremental = 0;
$ec->abortOnError(0);
$incremental = ($ec->getProperty("/myJob/incrementalImport")->findvalue("//value") eq "1");
$ec->abortOnError(1);
my $error = $ec->getError();
if ($error ne "") {
    #print("Could not get incrementalImport value due to: " . $error . "\n");
    $incremental = 0;
}
print("Incremental Import: $incremental\n");

my $changeListText = "";
# Is there a file path to a change list file
if ($incremental) {
    my @dirParts = split '/', "$[projDir]";
    @dirParts = splice @dirParts, 0, -2;
    my $rootDir = join '/', @dirParts;
    my $changeListFileName = $rootDir . "/change_list.json";
    print("ChangeListFileName: $changeListFileName\n");
    # if file exists, is not a folder and is readable...
    if (-e $changeListFileName && -f _ && -r _ ) {
        if (open(my $fileHandle, '<', $changeListFileName)) {
            read $fileHandle, $changeListText, -s $fileHandle;
            close($fileHandle);
        } else {
            print("Could not open $changeListFileName due to $!\n");
        }
    } else {
        print("$changeListFileName may be a folder or unreadable or not exist.\n");
    }
}
print("ChangeListText      : '$changeListText'\n");


foreach my $objectType (@subProjectEntities ) {
    my $pluralObjectTypeName = pluralForm($objectType);
    # Check change list if it is "incremental"
    if (index($changeListText, '"what":"INCREMENTAL"') != -1) {
        # If that object type is not found in the list skip them
        if (index($changeListText, "/".$objectType.".dsl\"") == -1) {
            print("Skip importing of $pluralObjectTypeName as those are not in the change list\n");
            next;
        }
    }

    my $shell   = 'ectool --timeout ' . $timeout . ' evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]';

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
def includeObjectsParam = '''$[includeObjects]'''
def excludeObjectsParam = '''$[excludeObjects]'''
def includeObjects = []
if (!includeObjectsParam.isEmpty()) {
  includeObjects = includeObjectsParam.split( '\n' )
}
def excludeObjects = []
if (!excludeObjectsParam.isEmpty()) {
  excludeObjects = excludeObjectsParam.split( '\n' )
}
END_COMMAND

    # with Perl variables usage / substitution
    my $command2;


    # check if corresponding directory exists
    if ("project" eq "$objectType" && -d '$[projDir]/') {
        $command2 = "def changeListText = '''" . $changeListText . "'''";
        $command2 = <<"END_COMMAND";

println "OUTER DSL for processing project: " + projectName
println '''  changeListText: $changeListText'''
def changeList = [:]
def jsonSlurp = new groovy.json.JsonSlurper()
if (changeListText != null && changeListText.size() > 0) {
    try {
        changeList = jsonSlurp.parseText('''$changeListText''')
    } catch (Exception ex) {
      println "Error parsing change list text: " + ex.getMessage()
    }
}

def counter
project projectName, {
  counter = loadProject(projectDir, projectName, overwrite, changeList)
  loadProjectProperties(projectDir, projectName, overwrite, changeList)
  loadProjectAcls(projectDir, projectName, changeList)
}

if (counter == 0) {
  setProperty(propertyName: "summary", value: "No project.groovy or project.dsl processed")
  setProperty(propertyName: "outcome", value: "warning")
}
return ""
END_COMMAND
    } elsif ("project" ne "$objectType" && -d '$[projDir]/' . pluralForm("$objectType")) {
        $command2 = "def changeListText = '''" . $changeListText . "'''";
        $command2 = <<"END_COMMAND";


println "OUTER DSL for processing project's ${objectType}(ie)s - project: " + projectName
println '''  changeListText: $changeListText'''
def changeList = [:]
def jsonSlurp = new groovy.json.JsonSlurper()
if (changeListText != null && changeListText.size() > 0) {
    try {
        changeList = jsonSlurp.parseText('''$changeListText''')
    } catch (Exception ex) {
      println "Error parsing change list text: " + ex.getMessage()
    }
}

def counters
project projectName, {
  counters = loadObjects("$objectType", projectDir, "/projects/" + projectName,
    [projectName: projectName, projectDir: projectDir], overwrite, ignoreFailed,
     true, includeObjects, excludeObjects, changeList)
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

    my $objectTypePlural = pluralForm("$objectType");
    my $projectName = "$[projName]";
    if ($objectType eq "project"
        || isIncluded("$[includeObjects]", "$[excludeObjects]",
        "/projects/$projectName/$objectTypePlural")) {

        $ec->createJobStep({
            jobStepName    => "deploy $objectType",
            command        => "$command",
            timeLimit      => "$userTimeout",
            timeLimitUnits => "seconds",
            shell          => "$shell",
            postProcessor  => "postp"});
    } else {
        print ("Skip import of $objectTypePlural as they do not match includeObjects/excludeObjects parameters\n");
    }
}
