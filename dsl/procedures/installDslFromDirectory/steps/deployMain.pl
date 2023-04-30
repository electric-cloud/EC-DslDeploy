#
#  deployMain.groovy - deploy the top level main.groovy if it exists
#
#  Copyright 2018-2019 Electric-Cloud Inc.
#
#  CHANGELOG
#  ----------------------------------------------------------------------------
#  2018-08-03  lrochette Initial Version
#  2019-05-30  lrochette Convert to Perl to get rid of 8K limit
#  ----------------------------------------------------------------------------
use Cwd;
$[/myProject/scripts/perlHeaderJSON]

# "." returns the Flow installation directory instead of the workspace
$ec->setProperty("/myJob/CWD", getcwd);

my $counter=0;
opendir(my $topDslDir, ".") || die ("cannot read top level directory");

# Support 0/1 and true/false values for overwrite parameter
my $ovrwrt = 0;
if (lc("$[overwrite]") eq "true" || "$[overwrite]") {
    $ovrwrt = 1;
}
print("User overwrite is: '$ovrwrt'\n");

my ($userTimeout) = ("$[additionalDslArguments]" =~ m/--timeout\s+([0-9]+)/);
print("User timeout is: '$userTimeout'\n");

my $pluginTimeout = $[/server/EC-DslDeploy/timeout];
print("Plugin timeout is: '$pluginTimeout'\n");

my $timeout = $userTimeout;
if ("$timeout" eq "") {
    $timeout = $pluginTimeout;
}
print("Timeout is: '$timeout'\n");

$ec->setTimeout($timeout);

my ($debug) = ("$[additionalDslArguments]" =~ m/--debug\s+([0-1]+)/);
print("User debug is: '$debug'\n");

if ("$debug" eq "") {
    $debug = 0;
}
print("Debug is: '$debug'\n");

$ec->{debug} = $debug;

while (my $file = readdir($topDslDir)) {
  if ($file =~ m/^.*\.(dsl|groovy)$/) {
    printf ("Processing top level file $file\n");
    $ec->evalDsl({dslFile => $file, overwrite => $ovrwrt, debug => $debug, timeout => $timeout});
    $counter++
  }
}
$ec->setProperty("summary", "$counter files loaded");
