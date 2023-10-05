import java.io.File

def procName = 'BAR'
procedure procName, {
  step 'echo', {
    command = new File(projectDir, "./procedures/$procName/steps/echo.pl").text
    shell = 'cb-perl'

    // Added for testing NMB-27865.
    // Check that both these property sheets
    // are created with the nested properties
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
  }

}
