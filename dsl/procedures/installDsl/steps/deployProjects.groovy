/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version

*/
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.apis.model.*
import com.electriccloud.client.groovy.models.ActualParameter

ElectricFlow ef = new ElectricFlow()

File projectsFolder = new File("projects")
projectsFolder.eachFile { projDir ->
  if (projDir.directory) {
    println "Processing directory $projDir"
    def basename=projDir.getName().toString()
    def params = [
        new ActualParameter('projName', basename),
        new ActualParameter('projDir', projDir.toString())
    ]

    ef.createJobStep(
      jobStepName: basename,
      subproject: '$[/myProject]',
      subprocedure: 'installProject',
      actualParameters: params
    )
  } else {
    println "Ignoring file projects/$projDir"
  }
}
