import java.io.File

def procName = 'PROCEDURE NAME HERE'
procedure procName, {

	step 'step1',
    	  command: new File(pluginDir, "dsl/procedures/$procName/steps/step1.pl").text,
    	  shell: 'ec-perl'

	// add more steps here, e.g., 
	//step 'step2',
    //	  command: new File(pluginDir, "dsl/procedures/$procName/steps/step2.groovy").text,
    //	  shell: 'ec-groovy'
	  
}
  
