
project args.projectName, {
    tracked = '1'

    // Custom properties

    property 'propSheet1', {
        description = 'propSheet1'
        propertyType = 'sheet'
    }

    property 'prop1', value: 'prop1', {
        description = 'prop1'
    }
}
