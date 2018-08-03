/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version

*/
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.apis.model.*
import com.electriccloud.client.groovy.apis.model.ActualParameter

ElectricFlow ef = new ElectricFlow()

File projectsFolder = new File("projects")
projectsFolder.eachFile { projDir ->
  if (projDir.directory) {
    ef.createJobStep(
      jobStepName: projDir,
      subprocedure: installProject,
      actualParameter: [
        projName: projDir,
        projDir: "./projects/" + projDir
      ]
    )
  }
}

  }
