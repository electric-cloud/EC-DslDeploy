import com.electriccloud.client.groovy.ElectricFlow

$[/myProject/scripts/GenerateDslHelper]

ElectricFlow ef = new ElectricFlow()

// BEE-19320: Escape single quote character in the entity name like 'te'st'
def objectName = ef.getProperty(propertyName: "objectName")?.property?.value
if (objectName) {
    objectName = objectName.replace('\'','\\\'')
}

new GenerateDslBuilder(ef)
        .objectType('$[objectType]')
        .objectName("$objectName")
        .toDirectory('$[directory]')
        .includeAllChildren('$[includeAllChildren]'.equals('1'))
        .includeChildren('$[includeChildren]')
        .childrenInDifferentFile('$[childrenInDifferentFile]')
        .includeChildrenInSameFile('$[includeChildrenInSameFile]'.equals('1'))
        .includeAcls('$[includeAcls]'.equals('1'))
        .includeAclsInDifferentFile('$[includeAclsInDifferentFile]'.equals('1'))
        .suppressParent('$[suppressParent]'.equals('1'))
        .suppressNulls('$[suppressNulls]'.equals('1'))
        .suppressDefaults('$[suppressDefaults]'.equals('1'))
        .build()
        .generateDsl();
