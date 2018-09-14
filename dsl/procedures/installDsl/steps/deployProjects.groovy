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

File projectsFolder = new File("projects")
// sort projects alpahbetically
dlist=[]
new File("projects").eachDir {dlist << it }
dlist.sort({it.name}).each { projDir ->
  println "Processing directory $projDir"
  def basename=projDir.getName().toString()
  def params = [
      new ActualParameter('projName', basename),
      new ActualParameter('projDir', projDir.absolutePath.toString())
  ]

  ef.createJobStep(
    jobStepName: basename,
    subproject: '$[/myProject]',
    subprocedure: 'installProject',
    actualParameters: params
  )
}
