import java.io.File

def procName = 'testProcedure'
procedure procName, {

	description = 'val'
	tags = ["arg1", "arg2"]

	//evalDsl the main.groovy if it exists
	step 'echo',
  	command: new File(projectDir, "./procedures/$procName/steps/echo.pl").text,
  	shell: 'ec-perl',
  	description: 'val'
}
