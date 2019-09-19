/* ###########################################################################
#
#  BaseObject: extension of BasePProject to allow the evaluation of any object
#
#  Author: L.Rochette
#
#  Copyright 2017-2019 Electric-Cloud Inc.
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.
#
# History
# ---------------------------------------------------------------------------
# 2019-04-02 lrochette  Inbitial version
############################################################################ */
package com.electriccloud.commander.dsl.util

import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Field
import java.io.File

import org.codehaus.groovy.control.CompilerConfiguration
import com.electriccloud.commander.dsl.DslDelegate
import com.electriccloud.commander.dsl.DslDelegatingScript

import java.util.logging.Logger


abstract class BaseObject extends DslDelegatingScript {

  private final static Logger logger = Logger.getLogger("")

  private static final String METADATA_FILE = "metadata.json"

  private static final Collection<String> ORDERED_CHILD_ENTITY_TYPES =
          Arrays.asList("catalogItems", "deployerApplications", "deployerServices",
                  "steps", "reportingFilters", "stages", "stateDefinitions", "tasks",
                  "widgets");

  def jsonSlurper = new JsonSlurper()

  // return the object.groovy or object.dsl
  //    AKA project.groovy, procedure.dsl, pipeline.groovy, ...
  // Show ignored files to make it easier to debug when a badly named file is
  // skipped
    File getObjectDSLFile(File objDir, String objType) {
    // println "Checking $objType in ${objDir.name}"
    File found=null
    this.getBinding().setVariable("pluginDeployMode", true)
    objDir.eachFileMatch(FileType.FILES, ~/(?i)^.*\.(groovy|dsl)/) { dslFile ->
      // println "Processing ${dslFile.name}"
      if (dslFile) {
        if (dslFile.name ==~ /(?i)${objType}\.(groovy|dsl)/) {
          if (found) {
            println "Multiple files match the ${objType}.groovy or ${objType}.dsl"
            setProperty(propertyName: "outcome", value: "warning")
          }
          found = dslFile
        } else {
          println "Ignoring incorrect file  ${dslFile.name} in ${objDir.name}"
          setProperty(propertyName: "outcome", value: "warning")
        }
      }
    }
    return found
  }

  def loadProject(String projectDir, String projectName, String overwriteMode = "0") {
    // load the project.groovy if it exists
    // println "Entering loadProject"
    // println "  Name:  $projectName"
    // println "  dir  : $projectDir"
    def counter=0
    File dslFile=getObjectDSLFile(new File(projectDir), "project")
    if (dslFile && dslFile?.exists()) {
      println "Processing project file projects/$projectName/${dslFile.name}"
      def proj=evalInlineDsl(dslFile.toString(),
                            [projectName: projectName, projectDir: projectDir], overwriteMode, true)
      counter ++
    } else {
      println "No project.groovy found"
    }
    return counter
  }

  def loadProjectProperties(String projectDir, String projectName) {
    // println "Entering loadProjectPropreties"
    // println "  Name:  $projectName"
    // println "  dir  : $projectDir"

    def propDir=new File(projectDir, 'properties')
    if (propDir.directory) {
      loadNestedProperties("/projects/$projectName", propDir)
    }  else {
      println "No properties directory for project $projectName"
    }
  }

  def loadProjectAcls(String projectDir, String projectName) {
    // println "Entering loadProjectAcls"
    // println "  Name:  $projectName"
    // println "  dir  : $projectDir"

    def aclDir=new File(projectDir, 'acls')
    if (aclDir.directory) {
      loadAcls(aclDir, "/projects/$projectName",
               [projectName: projectName, projectDir: projectDir])
    }  else {
      println "No acls directory for project $projectName"
    }
  }

  // Generic procedure to load an object in context
  /* ########################################################################
      loadObject: function to load a single DSL file passing the context
                  as a map
      Parameters:
        - dslFile: the absolute path to the DSL file to evaluate.
        - bindingMap: a list of properties to pass dow to evaluate the DSL
                      in context. Typically objectName and objectDir
     ######################################################################## */
  def loadObject(String dslFile, Map bindingMap = [:], String overwriteMode = "0") {
    // println "Load Object:"
    // println "  dslFile: $dslFile"
    // println "  map: " + bindingMap.toMapString(100)
    return evalInlineDsl(dslFile, bindingMap, overwriteMode)
  }

  /* ########################################################################
      loadObjects: function to load the top directory contains "obkects"
      Parameters:
        - objType: the type of object like "procedure", "persona", ...
        - dir: the location where "objects" will be found.
        - bindingMap: a list of properties to pass dow to evaluate the DSL
                      in context. Typically objectName and objectDir.
     ######################################################################## */
  def loadObjects(String objType, String topDir,
                  String objPath = "/",
                  Map bindingMap = [:], String overwriteMode = "0", Boolean pluginDeployMode = true) {

    // println "Entering loadObjects"
    // println "  Type:  $objType"
    // println "  dir  : $topDir"
    // println "  path : $objPath"
    // println "   sub : " + subObjects.join(",")
    // println "  map  : " + bindingMap.toMapString(250)

    def counters=[:]
    def nbObjs=0
    def plural=getPluralForm(objType)
    // looking for "objects" directory i.e. "procedures", "personas"
    File dir = new File(topDir, plural)
    if (dir.exists()) {

      // load metadata file if present
      def metadataFile = new File(dir, METADATA_FILE)
      def metadata = [:]
      if (metadataFile.exists()) {

        metadata = jsonSlurper.parseText(metadataFile.text)
      }
      def dlist=[]
      dir.eachDir {dlist << it }
      if (ORDERED_CHILD_ENTITY_TYPES.contains(plural) &&  dlist.size() > 1 && !metadata.order) {
        logger.warning('No order found in metadata.json for ordered entity type %objName. Objects will be loaded in alphabetical order.')
        setProperty(propertyName: "outcome", value: "warning")
      }

      if (metadata.order) {
        metadata.order.each {
          // load in specified order
          File objDir = new File(dir, it)
          loadObjectFromDirectory(objDir, objType, objPath, plural, bindingMap, overwriteMode, counters)
          nbObjs++
        }
      } else {
        // sort object alphabetically
        dlist.sort({ it.name }).each {
          loadObjectFromDirectory(it, objType, objPath, plural, bindingMap, overwriteMode, counters)
          nbObjs++
        }
      }
    }   // directory for "objects" exists
    counters.put(objType, nbObjs)
    return counters
  }

  def loadObjectFromDirectory(def childDir, String objType, String objPath, plural, Map bindingMap, String overwriteMode, counters) {
    def objName = childDir.name
    def objDir = childDir.absolutePath
    File dslFile = getObjectDSLFile(childDir, objType)
    if (dslFile == null) {
      return
    }
    println "Processing $objType file $objPath/$plural/$objName/${dslFile.name}"
    bindingMap[(objType + "Name")] = objName     //=> procedureName
    bindingMap[(objType + "Dir")] = objDir      //=> procedureDir
    def obj = loadObject(dslFile.absolutePath, bindingMap, overwriteMode)


    // Load nested properties
    def aclDir = new File(childDir, 'acls')
    if (aclDir.directory) {
      println "Found acls for $objPath/$plural/$objName"
      "${objType}" objName, {
        loadAcls(aclDir, "$objPath/$plural/$objName", bindingMap)
      }
    } else {
      println "  No acls directory for $objType $objName"
    }

    // Load nested properties
    def propDir = new File(childDir, 'properties')
    if (propDir.directory) {
      "${objType}" objName, {
        loadNestedProperties("$objPath/$plural/$objName", propDir)
      }
    } else {
      println "  No properties directory for $objType $objName"
    }

    def children = [
            application    : ['applicationTier', 'process', 'service', 'tierMap', 'environmentTemplateTierMap'],
            applicationTier: ['component'],
            catalog        : ['catalogItem'],
            component      : ['process'],
            dashboard      : ['reportingFilter', 'widget'],
            environment    : ['cluster', 'environmentTier'],
            gate           : ['task'],
            pipeline       : ['stage'],
            process        : ['processStep'],
            procedure      : ['step'],
            release        : ['pipeline', 'deployerApplication', 'deployerService'],
            service        : ['container', 'process'],
            stage          : ['gate', 'task'],
            widget         : ['reportingFilter', 'widgetFilterOverride']
    ]
    // load subObjects loadObjects (from local structure)
    if (children.containsKey(objType)) {
      // println "Found children: "
      children[objType].each { child ->
        // println "  processing $child"
        def childrenCounter
        "${objType}" objName, {
          childrenCounter = loadObjects(child, objDir,
                  "$objPath/${objType}s/$objName", bindingMap)
        }
        counters << childrenCounter
      }
    }
  }     // loadObjects


  // Helper function to load another dsl script and evaluate it in-context
  def evalInlineDsl(String dslFile, Map bindingMap, String overwriteMode = "0", Boolean pluginDeployMode = true) {
    // println "evalInlineDsl: $dslFile"
    // println "  Map: " + bindingMap
    CompilerConfiguration cc = new CompilerConfiguration();
    cc.setScriptBaseClass(InnerDelegatingScript.class.getName());
    //println "Class loader class name: ${th
    // is.scriptClassLoader.class.name}"
    // NMB-27865: Use the same groovy class loader that was used for evaluating
    // the DSL passed to evalDsl.
    GroovyShell sh = new GroovyShell(this.scriptClassLoader, bindingMap? new Binding(bindingMap) : new Binding(), cc);
    DelegatingScript script = (DelegatingScript)sh.parse(new File(dslFile))
    script.setDelegate(this.delegate);
    // add bindingMap to DslDelegate to deal with collections removing in 'overwrite' mode
    if (overwriteMode.toBoolean()) {
      script.getDelegate().getBinding().setVariable("bindingMap", bindingMap)
      script.getDelegate().getBinding().setVariable("overwrite", overwriteMode.toBoolean())
    }
    script.getDelegate().getBinding().setVariable("pluginDeployMode", pluginDeployMode)
    return script.run();
  }

  def loadAcls (File aclDir, String objPath,
                Map bindingMap) {
   println "Entering loadAcls"
   println "  dir  : $aclDir"
   println "  path : $objPath"
   println "  map  : " + bindingMap.toMapString(250)

    aclDir.eachFileMatch(FileType.FILES, ~/(?i)^.*\.(groovy|dsl)/) { dslFile ->
      println "  Processing ACL file $objPath/acls/${dslFile.name}"
      evalInlineDsl(dslFile.toString(), bindingMap)
    }
  }

  def loadNestedProperties(String propRoot, File propsDir) {
    // println "Entering loadNestedProperties($propRoot," +  propsDir.toString() + ")"
    propsDir.eachFile { dir ->
      println "  parsing " + dir.toString()
      int extension = dir.name.lastIndexOf('.')
      int endIndex = extension > -1 ? extension : dir.name.length()
      String propName = dir.name.substring(0, endIndex)
      String propPath = "${propRoot}/${propName}"
      if (dir.directory) {
        property propName, {
          loadNestedProperties(propPath, dir)
        }
      } else {
        def exists = getProperty(propPath, suppressNoSuchPropertyException: true, expand: false)
        if (exists) {
          modifyProperty propertyName: propPath, value: dir.text
        } else {
          createProperty propertyName: propPath, value: dir.text
        }
      }
    }
  }

  /**
   * NMB-27865: Intercept the DslDelegate
   * so it can be set as the delegate on the
   * dsl scripts being evaluated in context
   * of the parent dsl script.
   * Before setting the delegate, also capture
   * the script's class loader before the dslDelegate
   * highjacks the calls. This is needed to get the
   * reference to the groovy class loader that used for
   * evaluating the DSL script passed in to evalDsl.
   */
  private def delegate;
  private def scriptClassLoader;

  public void setDelegate(DslDelegate delegate) {
    this.scriptClassLoader = this.class.classLoader
    this.delegate = delegate;
    super.setDelegate(delegate)
  }

  public def getDelegate(){
    this.delegate
  }
  
  def getPluralForm(String objType){
    this.getBinding().setVariable("pluginDeployMode", true)
    switch (objType){
      case "process" :
          return 'processes'
      case "personaCategory" :
          return 'personaCategories'
      default:
          return objType + 's'
    }
  }
}
