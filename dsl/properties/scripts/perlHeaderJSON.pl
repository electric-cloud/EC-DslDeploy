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
