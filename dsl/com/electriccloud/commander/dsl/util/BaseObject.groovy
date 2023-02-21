/* ###########################################################################
#
#  BaseObject: extension of BasePProject to allow the evaluation of any object
#
#  Author: L.Rochette
#
#  Copyright 2017-2022 Electric-Cloud Inc.
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
# 2022-03-25 mayasse    Add ability to process a change list from git
# 2019-04-02 lrochette  Initial version
############################################################################ */
package com.electriccloud.commander.dsl.util

import com.electriccloud.commander.dsl.DslDelegate
import com.electriccloud.commander.dsl.DslDelegatingScript
import com.electriccloud.commander.dsl.DslYamlProcessor

import groovy.io.FileType
import groovy.json.JsonSlurper
import org.codehaus.groovy.control.CompilerConfiguration

import java.time.Duration
import java.util.logging.Logger

abstract class BaseObject
      extends DslDelegatingScript
{

  private final static Logger logger = Logger.getLogger("")

  private static final String METADATA_FILE = "metadata.json"

  private static final Collection<String> ORDERED_CHILD_ENTITY_TYPES =
          Arrays.asList("catalogItems", "deployerApplications", "deployerServices",
                  "steps", "reportingFilters", "stages", "stateDefinitions", "tasks",
                  "widgets")

  private static final Map<String, String> ENCODE_MAP = ["/": "@2F", "\\": "@5C"] as HashMap
  private static final Map<String, String> DECODE_MAP = ["@2F": "/", "@5C": "\\"] as HashMap

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

  def loadProject(String projectDir, String projectName, String overwriteMode = "0", changeList = [:], String dslFormat = "groovy") {
    // load the project.groovy if it exists
    // println "Entering loadProject"
    // println "  projectDir    : $projectDir"
    // println "  projectName   : $projectName"
    // println "  overwriteMode : $overwriteMode"
    // println "  changeList    : $changeList"

    def counter=0
    File dslFile=getObjectDSLFile(new File(projectDir), "project")
    if (dslFile && dslFile?.exists()) {
      if (changeCheck("projects/$projectName/${dslFile.name}", changeList, ["added", "changed"])) {
        println "Processing project file projects/$projectName/${dslFile.name}"
        def proj=evalInlineDsl(dslFile.toString(),
            [projectName: projectName, projectDir: projectDir], overwriteMode, true, dslFormat)
        counter ++
      }
    } else {
      println "No project.groovy or project.dsl found"
    }
    return counter
  }

  def loadProjectProperties(String projectDir, String projectName, String overwrite = '0', changeList = [:], String dslFormat = "groovy") {
    // println "Entering loadProjectProperties"
    // println "  projectDir  : $projectDir"
    // println "  projectName : $projectName"
    // println "  overwrite   : $overwrite"
    // println "  changeList  : $changeList"

    def propDir=new File(projectDir, 'properties')
    if (propDir.directory) {
      def propertySheet = getProperties path: "/projects/$projectName"
      def propSheetId = propertySheet.propertySheetId.toString()

      loadNestedProperties("/projects/$projectName", propDir, overwrite,
              propSheetId, true, changeList, dslFormat)
    }  else {
      println "No properties directory for project $projectName"
    }
  }

  def loadProjectAcls(String projectDir, String projectName, changeList = [:], String dslFormat = "groovy") {
    // println "Entering loadProjectAcls"
    // println "  projectDir  : $projectDir"
    // println "  projectName : $projectName"
    // println "  changeList  : $changeList"

    def aclDir=new File(projectDir, 'acls')
    if (aclDir.directory) {
      loadAcls(aclDir, "/projects/$projectName",
               [projectName: projectName, projectDir: projectDir], changeList. dslFormat)
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
  def loadObject(String dslFile, Map bindingMap = [:], String overwriteMode = "0", String dslFormat = "groovy") {
    // println "Entering loadObject:"
    // println "  dslFile       : $dslFile"
    // println "  bindingMap    : " + bindingMap.toMapString(100)
    // println "  overwriteMode : $overwriteMode"
    return evalInlineDsl(dslFile, bindingMap, overwriteMode, true, dslFormat)
  }

  /* ########################################################################
      loadObjects: function to load the top directory contains "objects"
      Parameters:
        - objType: the type of object like "procedure", "persona", ...
        - topDir: the location where "objects" will be found.
        - bindingMap: a list of properties to pass dow to evaluate the DSL
                      in context. Typically objectName and objectDir.
     ######################################################################## */
  def loadObjects(String objType,
                  String topDir,
                  String objPath = "/",
                  Map bindingMap = [:],
                  String overwriteMode = "0",
                  String ignoreFailed = "0",
                  Boolean pluginDeployMode = true,
                  def includeObjects = [],
                  def excludeObjects = [],
                  def changeList = [:],
                  String dslFormat = "groovy") {

//     println "Entering loadObjects"
//     println "  objType          : $objType"
//     println "  topDir           : $topDir"
//     println "  objPath          : $objPath"
//     println "  bindingMap       : " + bindingMap.toMapString(250)
//     println "  overwriteMode    : $overwriteMode"
//     println "  ignoreFailed     : $ignoreFailed"
//     println "  pluginDeployMode : $pluginDeployMode"
//     println "  includeObjects   : $includeObjects"
//     println "  excludeObjects   : $excludeObjects"
//     println "  changeList       : $changeList"
//     println "  dslFormat        : dslFormat"

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

      try {
        Exception exc = null
        if (metadata.order) {
          metadata.order.each {
            // load in specified order
            File objDir = new File(dir, encode(it))
            if (!objDir.exists()) {
              println String.format("Trying '%s' since encoded was not found", it)
              objDir = new File(dir, it)
            }
            try {
              if (loadObjectFromDirectory(objDir, objType, objPath, plural,
                      bindingMap, overwriteMode, counters,
                      includeObjects, excludeObjects, changeList, dslFormat)) {
                nbObjs++
              }
            } catch (Exception e) {
              if (ignoreFailed.toBoolean()) {
                exc = e
              } else {
                throw e
              }
            }
          }
        } else {
          // sort object alphabetically
          dlist.sort({ it.name }).each {
            try {
              if (loadObjectFromDirectory(it, objType, objPath, plural,
                      bindingMap, overwriteMode, counters,
                      includeObjects, excludeObjects, changeList, dslFormat)) {
                nbObjs++
              }
            } catch (Exception e) {
              if (ignoreFailed.toBoolean()) {
                exc = e
              } else {
                throw e
              }
            }
          }
        }

        if (exc != null) {
          throw exc
        }
      } catch (FileNotFoundException e) {
        // BEE-24274: we should not throw this exception for backward compatibility
        println("Error: " + e)
        counters.put("Error", e.message)
      } catch (Exception e) {
        println("Error: " + e)
        counters.put("Error", e.message)
        throw e
      }
    }   // directory for "objects" exists
    counters.put(objType, nbObjs)
    return counters
  }

  def loadObjectFromDirectory(def childDir, String objType, String objPath,
                              plural, Map bindingMap, String overwriteMode,
                              counters,
                              includeObjects = [],
                              excludeObjects = [],
                              changeList = [:],
                              String dslFormat = "groovy") {
//     println "Entering loadObjectFromDirectory"
//     println "  childDir       : $childDir"
//     println "  objType        : $objType"
//     println "  objPath        : $objPath"
//     println "  plural         : $plural"
//     println "  bindingMap     : " + bindingMap.toMapString(250)
//     println "  overwriteMode  : $overwriteMode"
//     println "  counters       : $counters"
//     println "  includeObjects : $includeObjects"
//     println "  excludeObjects : $excludeObjects"
//     println "  excludeObjects : $excludeObjects"
//     println "  dslFormat      : dslFormat"

    def objName = decode(childDir.name)
    def objPathSize = objPath.split('/').size()
    def pathToCheck = objPath +
            (objPath != '/' ?  "/" : "") + plural + "/" +
            objName
    def loaded = false

    if (objPathSize < 4 && !isIncluded(includeObjects, excludeObjects,
            pathToCheck)) {
      println("Skip import of " + pathToCheck + " as it doesn't match " +
              "includeObjects/excludeObjects parameters")
      return loaded
    }

    def objDir = childDir.absolutePath
    File dslFile = getObjectDSLFile(childDir, objType)
    if (dslFile == null) {
      return loaded
    }
    //println "Processing $objType file $objPath/$plural/$objName/${dslFile.name}"
    bindingMap[(objType + "Name")] = objName     //=> procedureName
    bindingMap[(objType + "Dir")] = objDir      //=> procedureDir

    def path = "$objPath".endsWith('/')
                  ? ("$objPath" + "$plural/$objName")
                  : "$objPath/$plural/$objName"
    if(changeCheck("$path/${dslFile.name}", changeList, ["added", "changed"])) {
      def obj = loadObject(dslFile.absolutePath, bindingMap, overwriteMode, dslFormat)

      if (obj == null) {
        // if response is null then it means an object wasn't imported because some error
        // (for instance completed release can't be imported and just ignored)
        println("Skip import of the object in $objPath/$plural/$objName/${dslFile.name} " +
                "and all nested objects according to restrictions")

        return false
      }

      loaded = true
    }


    // skip overwrite mode for parent object when
    // handle children
    if (bindingMap.get('skipOverwrite') == null) {
      bindingMap.put('skipOverwrite', new HashSet<String>())
    }
    // special case for task to support group subtasks
    String objKey = objType != 'task' ? objType : objType + '-' + objName
    ((Set<String>)bindingMap.get('skipOverwrite')).add(objKey)

    try {
      // Load ACLs
      def aclDir = new File(childDir, 'acls')
      if (aclDir.directory) {
        println "Found acls for $path"
        "${objType}" objName, {
          loadAcls(aclDir, "$path", bindingMap, changeList. dslFormat)
        }
      } else {
        println "No acls directory for $objType $objName"
      }

      // Load nested properties
      def propDir = new File(childDir, 'properties')
      if (propDir.directory) {
        def propertySheet = getProperties path: "$objPath/$plural[$objName]"
        def propSheetId = propertySheet.propertySheetId.toString()
        "${objType}" objName, {
          loadNestedProperties("$path", propDir,
                overwriteMode, propSheetId, false, changeList, dslFormat)
        }
      } else {
        println "No properties directory for $objType $objName"
      }

      def children = [
              application    : ['applicationTier', 'microservice', 'process', 'tierMap', 'environmentTemplateTierMap', 'snapshot', 'trigger'],
              applicationTier: ['component'],
              catalog        : ['catalogItem'],
              catalogItem    : ['trigger'],
              component      : ['process'],
              dashboard      : ['reportingFilter', 'widget'],
              environment    : ['cluster', 'environmentTier'],
              gate           : ['task'],
              pipeline       : ['stage', 'trigger'],
              process        : ['processStep'],
              procedure      : ['step', 'emailNotifier', 'trigger'],
              release        : ['pipeline', 'deployerApplication', 'trigger'],
              stage          : ['gate', 'task'],
              task           : ['task'],
              step           : ['emailNotifier'],
              widget         : ['reportingFilter', 'widgetFilterOverride']
      ]

      // load subObjects loadObjects (from local structure)
      if (children.containsKey(objType)) {
        // println "Found children: "
        children[objType].each { child ->
          // println "  processing $child"
          // println "OUTER DSL for processing ${objType}'s $child - ${objType}: " + objName
          // Note change to use getPluralForm() because hard-coded "s" would have skipped "processes"
          def childrenCounter
          "${objType}" objName, {
            childrenCounter = loadObjects(
                child,
                objDir,
                objPath + "/" + getPluralForm(objType) + "['$objName']",
                bindingMap,
                overwriteMode,
                "0",
                true,
                includeObjects,
                excludeObjects,
                changeList,
                dslFormat)
          }

          if (childrenCounter) {
            childrenCounter.each { key, value ->
              def countersValue = counters.get(key)
              if (countersValue) {
                counters.put(key, value + countersValue)
              } else {
                counters.put(key, value)
              }
            }
          }
        }
      }
    } finally {
      // allow overwrite mode for parent type
      ((Set<String>) bindingMap.get('skipOverwrite')).remove(objKey)
    }
    return loaded
  }     // loadObjects


  def evalInlineDslWoContext(String dslFile, Map bindingMap, String dslFormat = "groovy") {
    // We should save current DSL evaluation context and restore it right after import properties
    def tmpCurrent
    def tmpBindingMap
    def tmpStack = new LinkedList<>()

    try {
      tmpCurrent   = this.current
      this.current = null

      tmpStack.addAll(this.stack)
      this.stack.clear()

      tmpBindingMap = this.binding.bindingMap

      evalInlineDsl(dslFile, bindingMap, "", true, dslFormat)
    } finally {
      this.current = tmpCurrent
      this.stack.addAll(tmpStack)
      this.binding.bindingMap = tmpBindingMap
    }
  }

  // Helper function to load another dsl script and evaluate it in-context
  def evalInlineDsl(String dslFile, Map bindingMap, String overwriteMode = "0", Boolean pluginDeployMode = true, String dslFormat = "groovy") {
     println "Entering evalInlineDsl"
     println "  dslFile          : $dslFile"
     println "  bindingMap       : " + bindingMap
     println "  overwriteMode    : $overwriteMode"
     println "  pluginDeployMode : $pluginDeployMode"
     println "  dslFormat        : $dslFormat"

    def dsl = null
    if ("yaml" == dslFormat) {
      dsl = this.dslYamlProcessor.convert(new File(dslFile).text, [:])
    }

    CompilerConfiguration cc = new CompilerConfiguration()
    cc.setScriptBaseClass(InnerDelegatingScript.class.getName())
    //println "Class loader class name: ${th
    // is.scriptClassLoader.class.name}"
    // NMB-27865: Use the same groovy class loader that was used for evaluating
    // the DSL passed to evalDsl.
    GroovyShell sh = new GroovyShell(this.scriptClassLoader, bindingMap? new Binding(bindingMap) : new Binding(), cc)
    DelegatingScript script = "yaml" == dslFormat
                              ? (DelegatingScript)sh.parse(dsl)
                              : (DelegatingScript)sh.parse(new File(dslFile))

    script.setDelegate(this.delegate)
    // script.setDslYamlProcessor(this.dslYamlProcessor)

    // add bindingMap to DslDelegate to deal with collections removing in 'overwrite' mode
    if (overwriteMode.toBoolean()) {
      println "  Add overwrite flag to DSLDelegate vars: " + overwriteMode.toBoolean()
      script.getDelegate().getBinding().setVariable("overwrite", overwriteMode.toBoolean())
    }

    // add bindingMap to DslDelegate to deal with YAML format
    if ("yaml" == dslFormat) {
      println "  Add YAML format option to DSLDelegate vars: " + dslFormat
      script.getDelegate().getBinding().setVariable("format", dslFormat)
    }

    //println "  Add binding map to DSLDelegate vars: " + bindingMap
    script.getDelegate().getBinding().setVariable("bindingMap", bindingMap)
    script.getDelegate().getBinding().setVariable("pluginDeployMode", pluginDeployMode)

    long startTime = System.nanoTime()

    try {
      return script.run()
    }
    finally {
      if (debug) {
        long elapsedTimeInMS = Duration.ofNanos(System.nanoTime() - startTime).toMillis()
        logger.fine("Imported dsl file: '$dslFile' with binding map: '$bindingMap', elapsed time: $elapsedTimeInMS ms")
      }
    }
  }

  def loadAcls (File aclDir, String objPath, Map bindingMap, changeList = [:], String dslFormat = "groovy") {
//    println "Entering loadAcls"
//    println "  aclDir     : $aclDir"
//    println "  objPath    : $objPath"
//    println "  bindingMap : " + bindingMap.toMapString(250)
//    println "  changeList : $changeList"
//    println "  dslFormat  : dslFormat"

    aclDir.eachFileMatch(FileType.FILES, ~/(?i)^.*\.(groovy|dsl)/) { dslFile ->
      if (changeCheck("$objPath/acls/${dslFile.name}", changeList, ["changed", "added"])) {
        println "  Processing ACL file $objPath/acls/${dslFile.name}"
        evalInlineDsl(dslFile.toString(), bindingMap, "0", true, dslFormat)
      }
    }
  }

  def loadNestedProperties(String propRoot, File propsDir, String overwrite = '0', String pSheetId,
                           boolean projectRootProps=false, changeList = [:], String dslFormat = "groovy") {
    // println "Entering loadNestedProperties"
    // println "  propRoot         : $propRoot"
    // println "  propsDir         : $propsDir"
    // println "  overwrite        : $overwrite"
    // println "  pSheetId         : $pSheetId"
    // println "  projectRootProps : $projectRootProps"
    // println "  changeList       : $changeList"

    def allProperties = []
    propsDir.eachFile { dir ->

      if (dir.name.toString() in ["property.dsl", "propertySheet.dsl"]
          || (dir.directory && new File(dir, "property.dsl").exists())) {
        // BEE-18910: Skip processing files and folders with special meaning directly,
        // these optional files and folders are handled directly.
        return
      }

      println "  parsing " + dir.toString()
      int extension = dir.name.lastIndexOf('.')
      int endIndex = extension > -1 ? extension : dir.name.length()
      String propName = dir.name.substring(0, endIndex)
      String propPath = "${propRoot}/${propName}"
      String propPath2 = "${propRoot}/properties/${propName}"
      allProperties<<propName

      try {
        def existsProp = getProperty(objectId: "propertySheet-$pSheetId",
                                     propertyName: propName,
                                     expand: false)

        if (dir.directory) {
          if (changeCheck("$propPath", changeList, ["added", "changed"])
              || changeCheck("$propPath2", changeList, ["added", "changed"])) {

            def propSheetId
            if (existsProp) {
              propSheetId = existsProp.propertySheetId
            }
            else {
              def res = createProperty(objectId: "propertySheet-$pSheetId",
                                       propertyName: propName,
                                       propertyType: 'sheet')
              propSheetId = res.propertySheetId
            }

            // BEE-18910: Evaluate optional property sheet DSL file to restore description, etc.
            File propertySheetDslFile = new File(dir, "propertySheet.dsl")
            if (propertySheetDslFile.exists()) {
              println "  Processing property sheet file $propertySheetDslFile.absolutePath as a $propPath"

              // Map bindingMap = [propertySheetId: "$pSheetId", propertyType: 'sheet']
              Map bindingMap = [objectId: "propertySheet-$pSheetId", propertyType: 'sheet', propsDir: propsDir]
              evalInlineDslWoContext(propertySheetDslFile.absolutePath, bindingMap, dslFormat)
            }

            loadNestedProperties(propPath, dir, overwrite, propSheetId, projectRootProps, changeList, dslFormat)
          }
        } else {
          if (changeCheck("${propPath}.txt", changeList, ["added", "changed"])
              || changeCheck("${propPath2}.txt", changeList, ["added", "changed"])) {

            if (existsProp) {
              modifyProperty(propertyName: propName,
                             value: dir.text,
                             objectId: "propertySheet-$pSheetId")
            }
            else {
              createProperty(propertyName: propName,
                             value: dir.text,
                             objectId: "propertySheet-$pSheetId")
            }

            // BEE-18910: Evaluate optional property.dsl file with property details.
            File propertyDslFile = new File(propsDir, "$propName/property.dsl")
            if (propertyDslFile.exists()) {
              println "  Processing property file $propertyDslFile.absolutePath as a $propPath"

              // Map bindingMap = [propertySheetId: "$pSheetId", propertyType: 'string']
              Map bindingMap = [objectId: "propertySheet-$pSheetId", propertyType: 'string', propsDir: propsDir]
              evalInlineDslWoContext(propertyDslFile.absolutePath, bindingMap, dslFormat)
            }
          }
        }
      } catch (Exception e) {
        println(String.format("Error: cannot load property %s", propPath, e.getMessage()))
        setProperty(propertyName: "outcome", value: "warning")
      }
    }

    if (overwrite == '1' && !projectRootProps) {
      //cleanup nonexistent properties
      def properties = getProperties propertySheetId: "$pSheetId"
      properties.property.each {
        if (!allProperties.contains(it.name)) {
          println "Delete property '${propRoot}/${it.name}'"
          deleteProperty propertyName: "${it.name}", objectId: "propertySheet-$pSheetId"
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
  private def delegate
  private def dslYamlProcessor
  private def scriptClassLoader

  def getDelegate()
  {
    this.delegate
  }

  def getDslYamlProcessor()
  {
    this.dslYamlProcessor
  }

  void setDelegate(DslDelegate delegate)
  {
    this.scriptClassLoader = this.class.classLoader
    this.delegate = delegate
    super.setDelegate(delegate)
  }

  void setDslYamlProcessor(DslYamlProcessor dslYamlProcessor)
  {
    this.dslYamlProcessor = dslYamlProcessor
    super.setDslYamlProcessor(dslYamlProcessor)
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

  /**
   * Is the input file named in the change list
   *   in order to operate on the change list, the change list must declare itself to be "INCREMENTAL"
   *   otherwise we assume it is "INITIAL" even if it is badly formed
   */
  def changeCheck(String filePath, changeList = [:], changeTypes = ["changed", "added"]) {
    // The change list will only apply if it is marked "INCREMENTAL" and if the supplied file is found in the list
    //  Otherwise the change list does NOT apply (make all changes by applying all files)
    println "changeCheck: look for path '$filePath' in $changeList"
    boolean change = true
    if (changeList?.what == "INCREMENTAL") {
      // The change list applies and this file may be found or not
      change = changeTypes.any{changeType ->
        changeList[changeType].any{fileName ->
          // Using contains allows us to check nested properties better
          removeSlashesAtStart(fileName).contains(removeSlashesAtStart(filePath))
        }
      }
    }
    println "found change: $change"
    return change
  }

  static String removeSlashesAtStart(String value)
  {
    while (value.startsWith("/")) {
      value = value.substring(1)
    }

    return value
  }
}
