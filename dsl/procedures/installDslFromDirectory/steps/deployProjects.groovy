/*
  deployProjects.groovy - Loop through the projects and invoke each individually

  Copyright 2018 Electric-Cloud Inc.

  CHANGELOG
  ----------------------------------------------------------------------------
  2018-08-03  lrochette Initial Version
  2018-08-30  lrochette sorting project folder alphabetically

*/
import groovy.io.FileType
import groovy.json.StringEscapeUtils
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.apis.model.*
import com.electriccloud.client.groovy.models.ActualParameter
import java.io.File

$[/myProject/scripts/Utils]

ElectricFlow ef = new ElectricFlow()

File pDir=new File("projects")
if (pDir.exists()) {
  // sort projects alpahbetically
  dlist=[]
  pDir.eachDir {dlist << it }
  dlist.sort({it.name}).each { projDir ->
    def basename=decode(projDir.getName().toString())
    println "Processing project $basename"
    def escapedProjName = StringEscapeUtils.escapeJava(basename)
    def params = [
        new ActualParameter('projName', escapedProjName),
        new ActualParameter('projDir', projDir.absolutePath.toString().replace('\\', '/')),
        new ActualParameter('overwrite', '$[overwrite]'),
        new ActualParameter('additionalDslArguments', '$[additionalDslArguments]'),
        new ActualParameter('ignoreFailed', '$[ignoreFailed]'),
        new ActualParameter('localMode', '$[localMode]')
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
