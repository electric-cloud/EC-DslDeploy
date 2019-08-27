/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version
  2018-08-30  lrochette sorting project folder alphabetically

*/
import groovy.io.FileType
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.apis.model.*
import com.electriccloud.client.groovy.models.ActualParameter
import java.io.File

ElectricFlow ef = new ElectricFlow()

File pDir=new File("projects")
if (pDir.exists()) {
  // sort projects alpahbetically
  dlist=[]
  pDir.eachDir {dlist << it }
  dlist.sort({it.name}).each { projDir ->
    def basename=projDir.getName().toString()
    println "Processing project $basename"
    def params = [
        new ActualParameter('projName', basename),
        new ActualParameter('projDir', projDir.absolutePath.toString().replace('\\', '/')),
        new ActualParameter('overwrite', '$[overwrite]')
    ]

    ef.createJobStep(
      jobStepName: basename,
      subproject: '$[/myProject]',
      subprocedure: 'installProject',
      actualParameters: params
    )
  }
} else {
  ef.setProperty(propertyName:"summary", value:" No projects")
}
