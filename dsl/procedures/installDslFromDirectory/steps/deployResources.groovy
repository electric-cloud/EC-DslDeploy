/*
  deployResources.groovy - Loop through the resources and invoke each individually

  Copyright 2019 Electric-Cloud Inc.

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

// sort projects alpahbetically
dlist=[]
new File("resources").eachDir {dlist << it }
dlist.sort({it.name}).each { resourceDir ->
  def basename=resourceDir.getName().toString()
  println "Processing resource $basename"
  def params = [
      new ActualParameter('resName', basename),
      new ActualParameter('resDir', resourceDir.absolutePath.toString().replace('\\', '/'))
  ]

  ef.createJobStep(
    jobStepName: basename,
    subproject: '$[/myProject]',
    subprocedure: 'installResource',
    actualParameters: params
  )
}
