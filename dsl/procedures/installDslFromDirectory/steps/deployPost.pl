#
#  deployPost.groovy - deploy the files from the "post" directory after
#     everything else
#
#  Copyright 2018-2019 Electric-Cloud Inc.
#
#  CHANGELOG
#  ----------------------------------------------------------------------------
#  2019-04-02  lrochette Initial Version
#  2019-05-30  lrochette Convert to Perl to get rid of 8K limit
#  ----------------------------------------------------------------------------

$[/myProject/scripts/perlHeaderJSON]

my $counter=0;
opendir(my $pDir, "post");
while (my $file = readdir($pDir)) {
  if ($file =~ m/^.*\.(dsl|groovy)$/) {
    printf ("Processing post file post/$file\n");
    $ec->evalDsl({dslFile => "post/$file"});
    $counter++
  }
}
if ($counter) {
  $ec->setProperty("summary", "$counter post file loaded");
} else {
  $ec->setProperty("summary", "No post files");
}
