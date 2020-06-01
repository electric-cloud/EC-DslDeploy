##############################################################################
# retrieveArtifacts.pl - Need a explicit step instead of a plugin call
#       due to the act you can not broadcast a plugin call nor choose the
#       resource
#
# Copyright 2018 Electric-Cloud Inc.
#
# CHANGELOG
# ----------------------------------------------------------------------------
# 2018-09-27  lrochette Initial Version
# 2019-06-24  lrochette Remove additionbal single   quote in step name
##############################################################################
$[/myProject/scripts/perlHeaderJSON]

#
# Parameters
#
my $pool='$[pool]';
my $broadcast='$[allNodes]';
#
# retrieve the artifact version on one resource
sub retrieve {
  my $res=shift;
  $ec->createJobStep({
    jobStepName  => $res,
    resourceName => $res,
    subproject   => '/plugins/EC-Artifact/project',
    subprocedure => 'retrieve',
    actualParameter => [
			{actualParameterName => 'artifactName', value => '$[artName]'},
			{actualParameterName => 'versionRange', value => '$[artVersion]'},
			{actualParameterName => 'artifactVersionLocationProperty', value => "/myJob/retrievedArtifactVersions/$res"},
			{actualParameterName => 'retrieveToDirectory', value => "."}
 	]
  });
}

my $assignedResourceName='$[/myJobStep/assignedResourceName]';
$ec->setProperty("/myJob/assignedResourceName", $assignedResourceName);
#
# If no broadcast,simply pick one resource out of the pool
if ($broadcast eq "false") {
    $ec->setProperty("summary", "No broadcast");
    retrieve($assignedResourceName);
    exit(0);
}

my ($success, $result, $errMsg, $errCode) =
  InvokeCommander("IgnoreError", 'getResourcePool', $pool);

# A simple resource
if (! $success) {
  $ec->setProperty("summary", "Broadcast on simple resource $pool");
  retrieve($pool);
  exit(0);
}

$ec->setProperty("summary", "Broadcast on pool $pool");
foreach my $res ($result->findnodes('//resourceName')) {
  retrieve($res)
}

$[/myProject/scripts/perlLibJSON]
