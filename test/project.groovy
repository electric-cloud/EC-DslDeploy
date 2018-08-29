project 'EC-DslDeploy_test',
  description: "Testing project to make call easier through the plugin",
{
  procedure "test", {
    step "call",
      subproject: '/plugins/EC-DslDeploy/project',
      subprocedure: 'installDsl',
      actualParameter: [
        artName: 'com.electriccloud:EC-dslDeploy',
        artVersion: '1.0.5',
        broadcast: 'false',
        pool: 'local'
      ]
  }
}
