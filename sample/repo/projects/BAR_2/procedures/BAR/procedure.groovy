import java.io.File

def procName = 'BAR'
procedure procName, {

	//evalDsl the main.groovy if it exists
	step 'echo',
    command: new File(projectDir, "./procedures/$procName/steps/echo.pl").text,
    shell: 'ec-perl'

}
