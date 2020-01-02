import com.electriccloud.client.groovy.ElectricFlow

$[/myProject/scripts/GenerateDslHelper]

ElectricFlow ef = new ElectricFlow()
new GenerateDslBuilder(ef)
        .objectType('$[/myJob/objectType]')
        .objectName('$[/myJob/objectName]')
        .toDirectory('$[/myJob/directory]')
        .includeAllChildren('$[/myJob/includeAllChildren]'.equals('1'))
        .includeChildren('$[/myJob/includeChildren]')
        .childrenInDifferentFile('$[/myJob/childrenInDifferentFile]')
        .includeChildrenInSameFile('$[/myJob/includeChildrenInSameFile]'.equals('1'))
        .includeAcls('$[/myJob/includeAcls]'.equals('1'))
        .includeAclsInDifferentFile('$[/myJob/includeAclsInDifferentFile]'.equals('1'))
        .suppressParent('$[/myJob/suppressParent]'.equals('1'))
        .suppressNulls('$[/myJob/suppressNulls]'.equals('1'))
        .suppressDefaults('$[/myJob/suppressDefaults]'.equals('1'))
        .build()
        .generateDsl();
