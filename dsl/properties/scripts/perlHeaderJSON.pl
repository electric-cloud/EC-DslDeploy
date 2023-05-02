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

use utf8;

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

sub isIncluded {
    my ($includeObj, $excludeObj, $patternToCheck) = @_;
    my @includeObjects = ();
    if ($includeObj ne "") {
      @includeObjects = split('\n', $includeObj);
    }
    my @excludeObjects = ();
    if ($excludeObj ne "") {
        @excludeObjects = split('\n', $excludeObj);
    }

    if (@includeObjects == 0 && @excludeObjects == 0 ) {
        #print("no include/exclude objects\n");
        return 1;
    }

    my $include = 0;
    my $mostSpecificIncludePath;
    if (@includeObjects != 0) {
        foreach my $path (@includeObjects) {
            if ( $path eq $patternToCheck) {
                #print ">>>included $path $patternToCheck\n";
                return 1;
            }

            my @pathElements = split('/', $patternToCheck);
            my @includePathElements = split('/', $path);

            my $pathElementsLenght = @pathElements;

            my $includePathElementsLength = @includePathElements;

            my $min = $pathElementsLenght <=
                                   $includePathElementsLength ?
                                   $pathElementsLenght :
                                   $includePathElementsLength;
            my $match = 1;

            for(my $i = 0; $i < $min; $i++){
                my $includePathElement = $includePathElements[$i];
                my $pathElement = $pathElements[$i];

                #print ">>>Check $includePathElement match $pathElement (i=$i)\n";
                if ($pathElement ne $includePathElement
                        && !($i == 2 && "*" eq $includePathElement)) {
                    #wildcard allowed just for the name of project
                    $match = 0;
                    last;
                }
            }

            if ($match) {
                if (moreSpecific($path, $mostSpecificIncludePath)) {
                    #print ">>> set most specific include path to $path\n";
                    $mostSpecificIncludePath = $path;
                }
            }
        }
    }

    if (!@includeObjects || $mostSpecificIncludePath) {
        #print ">>> set include=true\n";
        $include = 1;
    }

    if ($include && @excludeObjects) {
            #print(">>>exclude $patternToCheck?\n");
            foreach my $path (@excludeObjects) {
                if ($path eq $patternToCheck) {
                    return 0;
                }
                #print(">>>" . $path . "\n");

                my @pathElements = split('/', $patternToCheck);

                my @excludePathElements = split('/', $path);

                my $pathElementsLenght = @pathElements;
                my $excludePathElementsLength = @excludePathElements;

                # do not take into account more specific exclude path
                next if ($excludePathElementsLength > $pathElementsLenght);

                my $exclude = 1;
                for (my $i = 0; $i < $excludePathElementsLength; $i++) {
                    my $excludePathElement = $excludePathElements[$i];
                    my $pathElement = $pathElements[$i];

                    if ($pathElement ne $excludePathElement &&
                            !($i == 2 && "*" eq $excludePathElement)) {
                        # wildcard allowed just for the name of project
                        $exclude = 0;
                        last;
                    }
                }

                if ($exclude && moreSpecific($path, $mostSpecificIncludePath)) {
                    #print(">>>exclude\n");
                    return 0;
                }
            }
        }


    return $include;

}

sub moreSpecific {
         my ($path, $lastMatchPath) = @_;
        if (!$lastMatchPath) {
            return 1;
        }

        my @pathElements = split('/', $path);
        my @lastPathElements = split('/', $lastMatchPath);
        my $pathElementsLenght = @pathElements;
        my $lastPathElementsLenght = @lastPathElements;
        if ($path =~ /^\/projects/) {

            if ($pathElementsLenght > 2 && $lastPathElementsLenght > 2) {

                if ($pathElements[2] ne '*' && $lastPathElements[2] eq '*') {
                    return 1;
                }

                if ($pathElements[2] eq '*' && $lastPathElements[2] ne '*') {
                    return 0;
                }

                #return $pathElementsLenght >= $lastPathElementsLenght;
            }

        }

        return $pathElementsLenght >= $lastPathElementsLenght;
    }


