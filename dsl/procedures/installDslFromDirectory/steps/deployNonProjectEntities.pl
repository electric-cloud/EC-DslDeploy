$[/myProject/scripts/perlHeaderJSON]

# Check if agent and server support clientFiles argument in evalDsl operation
my $clientFilesCompatible = checkClientFilesCompatibility();

# deploy non project entities
my @nonProjectEntities = ("tag", "personaPage", "personaCategory", "persona", "user", "group", "reportObjectType", "resource", "resourcePool", "serviceAccount");

my @includeObjects = ();
if ("$[includeObjects]" ne "") {
    @includeObjects = split('\n', "$[includeObjects]");
}

my @excludeObjects = ();
if ("$[excludeObjects]" ne "") {
    @excludeObjects = split('\n', "$[excludeObjects]");
}

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
if ($incremental) {
    ### Gather change list text
    # my $changeListFileName = "$[directory]/change_list.json";
    # print("ChangeListFileName: $changeListFileName\n");
    # # if file exists, is not a folder and is readable...
    # if (-e $changeListFileName && -f _ && -r _ ) {
    #     if (open(my $fileHandle, '<', $changeListFileName)) {
    #         read $fileHandle, $changeListText, -s $fileHandle;
    #         close($fileHandle);
    #     } else {
    #         print("Could not open $changeListFileName due to $!\n");
    #     }
    # } else {
    #     print("$changeListFileName may be a folder or unreadable or not exist.\n");
    # }
    $ec->abortOnError(0);
    $changeListText = $ec->getProperty("/myJob/change_list.json")->findvalue("//value");
    $ec->abortOnError(1);
    my $error = $ec->getError();
    if ($error ne "") {
        $changeListText = ""
    }
}
print("ChangeListText      : '$changeListText'\n");

foreach my $objectType (@nonProjectEntities) {
    my $pluralType = pluralForm($objectType);
    if (@includeObjects != 0 || @excludeObjects != 0 ) {
        my $path = "/$pluralType";
        if (@includeObjects != 0 && ! (grep ($_ eq $path, @includeObjects))) {
            print("Skip importing of $pluralType as it's not in a includeObjects list\n");
            next;
        }
        if (@excludeObjects != 0 && grep ($_ eq $path, @excludeObjects)) {
            print("Skip importing of $pluralType as it's in a excludeObjects list\n");
            next;
        }
    }
    # Check change list if it is "incremental"
    if (index($changeListText, '"what":"INCREMENTAL"') != -1) {
        # If that object type is not found in the list skip them
        my $toMatch = "$pluralType/.*/properties/";
        if (index($changeListText, "/".$objectType.".dsl\"") == -1 && ($changeListText !=~ $toMatch)) {
            print("Skip importing of $pluralType as those are not in the change list\n");
            next;
        }
    }

    my $resource = '$[pool]';
    my $shell    = 'ectool --timeout ' . $timeout . ' evalDsl --dslFile {0}.groovy --serverLibraryPath "$[/server/settings/pluginsDirectory]/$[/myProject/projectName]/dsl" $[additionalDslArguments]';

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
def includeObjectsParam = '''$[includeObjects]'''
def excludeObjectsParam = '''$[excludeObjects]'''
def includeObjects = []
if (!includeObjectsParam.isEmpty()) {
  includeObjects = includeObjectsParam.split( """\n""" )
}
def excludeObjects = []
if (!excludeObjectsParam.isEmpty()) {
  excludeObjects = excludeObjectsParam.split( """\n""" )
}
println '''  changeListText: $changeListText'''
def changeList = [:]
def jsonSlurp = new groovy.json.JsonSlurper()
if (changeListText != null && changeListText.size() > 0) {
    try {
        changeList = jsonSlurp.parseText('''$changeListText''')
    } catch (Exception ex) {
        println "Error parsing change list text: " . ex.getMessage()
    }
}
File dir      = new File(absDir, pluralForm("$objectType"))

if (dir.exists()) {
  def counters = loadObjects("$objectType", absDir, "/", [:], overwrite,
  ignoreFailed, true, includeObjects, excludeObjects, changeList)
  setProperty(propertyName: "summary", value: summaryString(counters))
} else {
  setProperty(propertyName: "summary", value: "no " + pluralForm("$objectType"))
}

// Print advisoryMessages
if (context && context.advisoryMessages && context.advisoryMessages.size() > 0) {
    for (def msg: context.advisoryMessages) {
        println "Advisory message:" + msg
    }
}
END_COMMAND
        $command2 = "def changeListText = '''" . $changeListText . "''' $command2";

        my $localMode = '$[localMode]';

        # check support of clientFiles argument
        if ($localMode eq '0' && $clientFilesCompatible) {
            $shell .= " --clientFiles \"$[/myJob/CWD]" . "/" . pluralForm($objectType) . "\""
        }
    } else {
        $command2 = <<"END_COMMAND";

setProperty(propertyName: "summary", value: "no " + pluralForm("$objectType"))
END_COMMAND
        $command2 = "def changeListText = '''''' $command2";
    }

    my $command  = "$command1" . "$command2";

    $ec->createJobStep({
        jobStepName    => "deploy $objectType",
        command        => "$command",
        timeLimit      => "$userTimeout",
        timeLimitUnits => "seconds",
        resourceName   => "$resource",
        shell          => "$shell",
        postProcessor  => "postp"});
}
