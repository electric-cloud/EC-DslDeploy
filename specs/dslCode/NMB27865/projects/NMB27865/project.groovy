project 'NMB27865', {


  property 'Changes', {
    description = 'Open Changes in Service Manager'

    // Custom properties

    property 'C2834144', {

      // Custom properties
      SM_Change_Approved = 'false'
      SM_Execute_Allowed = 'false'
    }

    property 'C2835095', {

      // Custom properties

      property '31d95495-024b-11e9-81a2-005056940f0e', value: 'jenkinsDrivenK8s', {
        expandable = '1'
      }
      EJ_ServiceManager_EventinLastSeq = '1'
      SM_Change_Approved = 'false'
      SM_Execute_Allowed = 'false'

      property 'f65662c2-024b-11e9-b443-005056940f0e', value: 'jenkinsDrivenK8s', {
        expandable = '1'
      }
    }

    property 'C2835107', {

      // Custom properties
      SM_Change_Approved = 'false'
      SM_Execute_Allowed = 'false'
    }
  }

  property 'Changes_Watchlist', {
    description = 'Pipelines cctively attached to changes'
    propertyType = 'sheet'
  }

  property 'Framework', {

    // Custom properties
    property 'frmSvcImageTag', value:'1.0.80'
  }

  property 'QC', {
    description = 'Quality Center'

    // Custom properties
    qc_base_path = '/qcbin'
    qc_rest_authentication_point = '/authentication-point/authenticate'
    qc_rest_base_path = '/qcbin/rest'
    qc_rest_hostname = 'qc'
    qc_rest_protocol = 'http'
  }

  property 'Teams', {
    description = 'Support team links to Applications'
    propertyType = 'sheet'
  }

}
