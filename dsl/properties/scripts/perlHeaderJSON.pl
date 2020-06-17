#############################################################################
#
# Copyright 2013 Electric-Cloud Inc.
#
#############################################################################
use strict;
use English;
use ElectricCommander;
use Data::Dumper;
$| = 1;

my $DEBUG=0;

# Create a single instance of the Perl access to ElectricCommander
my $ec = new ElectricCommander({'format' => "json"});

# Check for the OS Type
my $osIsWindows = $^O =~ /MSWin/;

#### Line 20 ####

sub pluralForm($) {
    my ($objectType) = @_;

    if ("process" eq "$objectType") {
        $objectType = "processes";
    } elsif ("personaCategory" eq "$objectType") {
        $objectType = "personaCategories";
    } else {
        $objectType .= "s";
    }

    return $objectType;
}

sub checkClientFilesCompatibility() {
    # Ger assigned resource version
    my $xpath = $ec->getResource("$[/myResource/name]");
    my $resourceVersion = $xpath->findvalue("//version");
    
    # Ger server version
    $xpath = $ec->getVersions();
    my $serverVersion = $xpath->findvalue("//serverVersion/version");
    
    my @supportedVersionPatterns = ("[0-9]{2}\.", "2020\.[5-9]+", "2020\.[0-9]{2}", "202[1-9]");
    
    # Verify resource/agent and server versions
    my $resourceVersionMatched; 
    my $serverVersionMatched; 
    foreach my $supportedVersion (@supportedVersionPatterns) {
        $resourceVersionMatched = 1 if $resourceVersion =~ /^$supportedVersion/;
        $serverVersionMatched = 1 if $serverVersion =~ /^$supportedVersion/;
    }   
    
    if (!$resourceVersionMatched || !$serverVersionMatched) {
        print "WARNING: Resource with version '" . $resourceVersion . "' or server with version '" . $serverVersion . "' do not support new evalDsl argument 'clientFiles'. \n";
        return 0;
    } else {
        print "Resource with version '" . $resourceVersion . "' and server with version '" . $serverVersion . "' support new evalDsl argument 'clientFiles'. \n";
        return 1;
    }
}
