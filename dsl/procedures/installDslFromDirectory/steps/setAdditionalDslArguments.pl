#
#  setAdditionalDslArguments.pl - verify resource and server versions,
#                                 and setup clienFiles argument if supported.
#
#  Copyright 2020 CloudBees, Inc.
#
use Cwd;
use XML::XPath;
$[/myProject/scripts/perlHeaderJSON]

# Save initial state of AdditionalDslArguments property
my $xpath = $ec->getProperty("/myCall/additionalDslArguments");
my $additionalDslArguments = $xpath->findvalue("//value");

# Ger assigned resource version
$xpath = $ec->getResource("$[/myResource/name]");
my $resourceVersion = $xpath->findvalue("//version");

# Ger server version
$xpath = $ec->getServerStatus();
my $serverVersion = $xpath->findvalue("//serverVersion/version");

my @supportedVersionPatterns = ("[0-9]{2}\.", "2020\.[5-9]+", "2020\.[0-9]{2}", "202[1-9]");

# Verify resource/agent and server versions
my $resourceVersionMatched; 
my $serverVersionMatched; 
foreach my $supportedVersion (@supportedVersionPatterns) {
    $resourceVersionMatched = 1 if $resourceVersion =~ /^$supportedVersion/;
    $serverVersionMatched = 1 if $serverVersion =~ /^$supportedVersion/;
}   

# Set additional DSL arguments if supported
if (!$resourceVersionMatched || !$serverVersionMatched) {
    print "WARNING: Resource with version '" . $resourceVersion . "' or server with version '" . $serverVersion . "' do not support new evalDsl argument 'clientFiles'. \n";
} else {
    print "Resource with version '" . $resourceVersion . "' and server with version '" . $serverVersion . "' support new evalDsl argument 'clientFiles'. \n";
    $ec->setProperty("/myCall/additionalDslArguments", "$additionalDslArguments" . " --clientFiles \"$[directory]\"");
}
