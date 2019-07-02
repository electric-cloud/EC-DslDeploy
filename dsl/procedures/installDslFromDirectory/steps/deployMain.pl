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

# "." returns the Flow installa directory instead of the workspace
$ec->setProperty("/myJob/CWD", getcwd);

my $counter=0;
opendir(my $topDslDir, ".") || die ("cannot read top level directory");

while (my $file = readdir($topDslDir)) {
  if ($file =~ m/^.*\.(dsl|groovy)$/) {
    printf ("Processing top level file $file\n");
    $ec->evalDsl({dslFile => $file});
    $counter++
  }
}
$ec->setProperty("summary", "$counter files loaded");
