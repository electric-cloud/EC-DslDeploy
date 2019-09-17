import java.io.File
import java.util.logging.Logger
import groovy.json.JsonOutput

class GenerateDslHelper {

    private static final String METADATA_FILE = "metadata.json"

    //
    private def electricFlow

    private String objectType
    private String objectName
    private File toDirectory

    private boolean includeAcls
    private boolean includeAllChildren
    private List<String> includeChildren
    private boolean includeChildrenInSameFile
    private boolean suppressNulls
    private boolean suppressDefault
    private boolean suppressParent
    private final Deque<EntityTypeDetail> childrenStack = new LinkedList<>()
    private  FileTemplateItem difFileTemplateRoot = new FileTemplateItem()

    GenerateDslHelper(def ef,
                      String objType,
                      String objName,
                      String toDir,
                      boolean suppressNulls,
                      boolean suppressDefault,
                      boolean suppressParent,
                      boolean includeAcls,
                      boolean includeAllChildren,
                      boolean includeChildrenInSameFile,
                      String childrenInDifFile,
                      List<String> includeChildren) {

        electricFlow = ef

        objectType = objType
        objectName = objName
        toDirectory = new File(toDir)

        this.includeAcls = includeAcls
        this.includeChildrenInSameFile = includeChildrenInSameFile
        this.suppressNulls = suppressNulls
        this.suppressDefault = suppressDefault
        this.suppressParent = suppressParent
        this.includeAllChildren = includeAllChildren
        this.includeChildren = includeChildren
        parseChildrenInDifferentFile(childrenInDifFile)
    }

    def generateDsl() {
        if (!toDirectory.exists()) {
            toDirectory.mkdirs()
        }

        println "Target directory for generated DSL: " + toDirectory.getAbsolutePath() + "\n"

        // store full path to the directory in a job property:
        electricFlow.setProperty(propertyName: 'directoryFullPath',
                value: toDirectory.getAbsolutePath(), jobId: '$[/myJob/id]')

        //
        def structure = electricFlow.getObjectDslStructure(objectType: objectType, objectName: objectName)

        if (structure && structure.object) {
            def obj = structure.object
            File objDir = new File (toDirectory, obj.path)

            if (objDir.exists()) {
                objDir.deleteDir()
            }
            objDir.mkdirs()

            handleObject(obj, objDir, true)
        }

    }

    def handleObjectType (def objectTypeDetail, def parentDir) {

        println String.format("Generate DSL for %s (%d objects)",
                objectTypeDetail.collectionName,
                objectTypeDetail.objects.object.size)

        childrenStack.push(
                new EntityTypeDetail(objectTypeDetail.name, objectTypeDetail.collectionName))
        //
        File objTypeDir = new File (parentDir, objectTypeDetail.collectionName)
        objTypeDir.mkdir()

        if (objectTypeDetail.ordered == '1') {
            //create metadata.json file with order info
            def data = [:]
            def order = []

            objectTypeDetail.objects.object.each{
                order.add(it.name)
            }

            data << [order: order]
            def json_str = JsonOutput.toJson(data)

            File metadataFile = new File(objTypeDir, METADATA_FILE)
            metadataFile << json_str
        }

        //
        objectTypeDetail.objects.object.each{
            handleChildObject(it, objTypeDir)}

        //
        childrenStack.pollLast()
    }

    static def anyObjectWithFileRef(def objTypeDetail) {
        return objTypeDetail.objects.object.any {it.fileRefInfo && it.fileRefInfo.size > 0}

    }

    def handleObject(def obj, def objDir,
                     boolean topLevel = false) {

        File objDslFile = new File (objDir, obj.type + ".groovy")
        if (topLevel && includeAllChildren && includeChildrenInSameFile) {

            println String.format("generate DSL for the '%s' %s and all it's nested objects in a same file %s",
                    obj.name, obj.type, objDslFile.getAbsolutePath())

            //All in one file including all commands
            objDslFile << electricFlow.generateDsl(path: obj.path, suppressNulls: suppressNulls, withAcls: includeAcls,
                    suppressDefaults: suppressDefault, suppressParent: suppressParent).value
            return
        }

        def inSameFile = []
        def inDiffFile = []
        def childrenToCheckFileRef = []

        if (obj.children && obj.children.objectType) {

            obj.children.objectType.each {

                if (it.alwaysIncluded == '1') {
                    childrenToCheckFileRef.add(it)
                } else if (!topLevel || includeAllChildren || includeChildren.contains(it.collectionName)) {

                    if (inDifFile(it)) {
                        inDiffFile.add(it)

                        if (anyObjectWithFileRef(it)) {
                            // if a child type object has file ref
                            childrenToCheckFileRef.add(it)
                        }

                    } else {
                        inSameFile.add(it)
                        childrenToCheckFileRef.add(it)
                    }

                }

            }
        }

        //handle included objects with file refs
        def hasFileRefInFile = createCommandFiles(childrenToCheckFileRef, objDir, inSameFile)

        // create includeChildren list for generateDsl
        def includeChildrenList = inSameFile.collect{it.collectionName}

        def includeChildrenValue = includeChildrenList.join(', ')

        // generate DSL for a current object

        println String.format("Generate DSL for '%s' %s in %s.", obj.name, obj.type, objDslFile.getAbsolutePath())
        def dsl = electricFlow.generateDsl(path: obj.path,
                includeChildren: includeChildrenValue,
                suppressNulls: suppressNulls,
                suppressDefaults: suppressDefault,
                suppressChildren: true,
                suppressParent: suppressParent,
                withAcls: includeAcls,
                useFileReferences: true).value

        if (hasFileRefInFile || obj.fileRefInfo && obj.fileRefInfo.size > 0) {
            objDslFile << 'import java.io.File\n\n'
            dsl = dsl.replaceAll("'(new File\\(.*,.*\\).text)'", "\$1")
        }

        objDslFile << dsl


        // create property file structure if needed
        if (obj.propertiesOwner == '1' &&  !(includeAllChildren && includeChildrenInSameFile) /*&& propertiesInDifFile*/) {
            generateProperties(objDir, obj)
        }

        // generate DSL for child entities in different files
        inDiffFile.each {
            if (it.alwaysIncluded != '1') {
                handleObjectType(it, objDir)
            }
        }
    }

    private def createCommandFiles(List childrenToCheckFileRef, objDir, inSameFile) {
        def hasFileRefInFile

        childrenToCheckFileRef.each {
            def heirHasFileRef = handleEntitiesWithFileRef(objDir, it)
            hasFileRefInFile = hasFileRefInFile ||
                    heirHasFileRef && (inSameFile.contains(it) || it.alwaysIncluded == '1')
        }
        return hasFileRefInFile
    }

    def handleEntitiesWithFileRef(def parentDir, def objectTypeDetail) {
        File objTypeDir = new File (parentDir, objectTypeDetail.collectionName)

        def hasFileRefInFile = false
        objectTypeDetail.objects.object.each {
            if (it.fileRefInfo && it.fileRefInfo.size > 0) {
                objTypeDir.mkdirs()
                def fileRefInfo = it.fileRefInfo[0]
                File file = new File(objTypeDir, it.name + '.' + fileRefInfo.extension)
                String propertyPath = fileRefInfo.propertyPath

                def property = electricFlow.getProperty(propertyName: propertyPath, expand: false)

                if (property && property.property ){
                    file << property.property.value
                    hasFileRefInFile = true
                }
            }

            if (it.children && it.children.objectType) {
                for (def childType : it.children.objectType) {

                    File objDir = new File(objTypeDir, it.name)
                    hasFileRefInFile = handleEntitiesWithFileRef(objDir, childType) || hasFileRefInFile
                }
            }

        }

        return hasFileRefInFile

    }

    def generateProperties(def objDir, def obj) {
        def result = electricFlow.getProperties(path: obj.path, recurse: true, expand: false)

        File propertiesDir = new File(objDir, 'properties')
        propertiesDir.mkdir()

        handleProperties(propertiesDir, result.propertySheet.property)
    }

    private void handleProperties(File propertiesDir, def properties) {
        for (def property :  properties) {
            if (property.propertySheet) {
                File dir = new File(propertiesDir, property.propertyName)
                dir.mkdir()
                handleProperties(dir, property.propertySheet.property)
            } else {
                File file = new File(propertiesDir, property.propertyName + '.txt')
                file << property.value
            }
        }
    }

    boolean inDifFile(def childType) {

        return inDifFile(childType.name, childType.collectionName)
    }

    boolean inDifFile(def name, def collectionName) {

        if (difFileTemplateRoot.getChildren().isEmpty() && !includeChildrenInSameFile) {
            return true
        }

        def types = []
        types.addAll(childrenStack)
        types.add(new EntityTypeDetail(name, collectionName))

        return difFileTemplateRoot.match(types)

    }

    def handleChildObject(def obj, def parentDir) {

        def dirName = obj.name
        File objDir = new File (parentDir, dirName)
        objDir.mkdir()
        handleObject(obj, objDir, false)
    }

    def parseChildrenInDifferentFile(String param) {

        if (!param && param.isEmpty()) {
            return
        }

        List<String> templates = Arrays.asList(param.split('[\\s,;]+'))
        for (String template: templates) {
            difFileTemplateRoot.addTemplate(template)
        }
    }

}

class EntityTypeDetail {
    private def name
    private def collectionName

    EntityTypeDetail(def name, def collectionName) {
        this.name = name
        this.collectionName = collectionName
    }
}


class FileTemplateItem {

    String name

    Map<String, FileTemplateItem> children = new HashMap<>()

    FileTemplateItem(def name) {
        this.name = name
    }

    FileTemplateItem(){}

    def addTemplate(String template) {
        List<String> templateItems = Arrays.asList(template.split('\\.'))

        def parent = this
        for(def templateItemName: templateItems) {
            def child = parent.addTemplateItem(templateItemName)

            if (!child || child.isWildcard()) {
                return
            }
            parent = child
        }

    }

    def addTemplateItem(def templateItemName) {
        def templateItem
        if ('*' == templateItemName) {
            // all children in diff files
            children.clear()
            templateItem = new FileTemplateItem(templateItemName)
            children.put(templateItemName, templateItem)
        } else if (!children.get('*')) {
            templateItem = children.get(templateItemName)
            if (!templateItem) {
                templateItem = new FileTemplateItem(templateItemName)
                children.put(templateItemName, templateItem)
            }
        }

        return templateItem
    }

    def getChildren() {
        return children
    }

    def getName() {
        return name;
    }

    def isWildcard() {
        return '*' == name
    }


    def match(def types) {

        def parent  = this
        for (def i = 0; i< types.size() ; i++) {
            def type = types.get(i)
            def children  = parent.getChildren()
            def child = children.get(type.collectionName) ?: children.get(type.name)

            if (child) {
                parent = child
            } else if (children.get('*')) {
                return true
            }
            else {
                return false
            }
        }
        return true

    }
}

class GenerateDslBuilder {
    private def electricFlow

    // required parameters
    private String objectType
    private String objectName
    private String toDirectory


    //Optional parameters
    private String childrenInDifferentFile
    private boolean includeAcls
    private boolean includeAllChildren
    private String includeChildren
    private boolean includeChildrenInSameFile
    private boolean suppressNulls = true
    private boolean suppressDefault
    private boolean suppressParent


    GenerateDslBuilder(def ef) {
        electricFlow = ef
    }

    GenerateDslHelper build() {
        if (!objectType?.trim()) {
            throw new IllegalArgumentException("objectType must be provided")
        }
        if (!objectName?.trim()) {
            throw new IllegalArgumentException("objectName must be provided")
        }
        if (!toDirectory?.trim()) {
            throw new IllegalArgumentException("toDirectory must be provided")
        }

        return new GenerateDslHelper(electricFlow,
                objectType,
                objectName,
                toDirectory,
                suppressNulls,
                suppressDefault,
                suppressParent,
                includeAcls,
                includeAllChildren,
                includeChildrenInSameFile,
                childrenInDifferentFile,
                Arrays.asList(includeChildren.split('[, ]+')))

    }

    GenerateDslBuilder objectType(String objType) {
        objectType = objType
        return this
    }

    GenerateDslBuilder objectName(String objName) {
        objectName = objName
        return this
    }

    GenerateDslBuilder includeAcls(boolean include) {
        includeAcls = include
        return this
    }

    GenerateDslBuilder includeAllChildren(boolean include) {
        includeAllChildren = include
        return this
    }

    GenerateDslBuilder includeChildren(String children) {
        includeChildren = children
        return this
    }

    GenerateDslBuilder includeChildrenInSameFile(boolean include) {
        includeChildrenInSameFile = include
        return this
    }

    GenerateDslBuilder suppressNulls(boolean suppress) {
        suppressNulls = suppress
        return this
    }

    GenerateDslBuilder suppressDefaults(boolean suppress) {
        suppressDefault = suppress
        return this
    }

    GenerateDslBuilder suppressParent(boolean suppress) {
        suppressParent = suppress
        return this
    }

    GenerateDslBuilder toDirectory(String directory) {
        toDirectory = directory
        return this
    }

    GenerateDslBuilder childrenInDifferentFile(String children) {
        childrenInDifferentFile = children
        return this
    }

}


