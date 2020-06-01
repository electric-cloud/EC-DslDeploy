if ( $promoteAction eq "promote" ) {
  # Use createProperty (and ignore errors) so that we do not overwrite existing properties
  $commander->abortOnError(0);
  $commander->createProperty("/server/@PLUGIN_KEY@/timeout",
     {description=>'Timeout for evalDsl that can be increased if files are too long',
      value=>'600'});
  # Reset error handling at this point
  $commander->abortOnError(1);
}
