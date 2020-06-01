#############################################################################
#
# Invoke a API call
#
#############################################################################
sub InvokeCommander {

    my $optionFlags = shift;
    my $commanderFunction = shift;
    my $result;
    my $success = 1;
	  my $errMsg;
	  my $errCode;

    my $bSuppressLog = $optionFlags =~ /SuppressLog/i;
    my $bSuppressResult = $bSuppressLog || $optionFlags =~ /SuppressResult/i;
    my $bIgnoreError = $optionFlags =~ /IgnoreError/i;

    # Run the command
    # print "Request to Commander: $commanderFunction\n" unless ($bSuppressLog);

    $ec->abortOnError(0) if $bIgnoreError;
    $result = $ec->$commanderFunction(@_);
    $ec->abortOnError(1) if $bIgnoreError;

    # Check for error return
    if (defined ($result->{responses}->[0]->{error})) {
    	$errCode=$result->{responses}->[0]->{error}->{code};
    	$errMsg =$result->{responses}->[0]->{error}->{message};
    }

    if ($errMsg ne "") {
        $success = 0;
    }
    if ($result) {
        print "Return data from Commander:\n" .
               Dumper($result) . "\n"
            unless $bSuppressResult;
    }

    # Return the result
    return ($success, $result, $errMsg, $errCode);
}
