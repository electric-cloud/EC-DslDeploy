
project args.projectName, {

  procedure 'proc1', {

    step 'step1', {
      command = 'echo Test'
    }
  }

  procedure 'proc2', {

    step 'Checkout from GitHub', {
      command = ''
      subprocedure = 'CheckoutCode'
      subproject = '/plugins/ECSCM-Git/project'
    }
  }
}
