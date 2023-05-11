
def projName = 'proj_name'
def compName= 'comp_name'
def procName = 'proc_name'
def propertyName = 'property_name'

project projName, {

    component compName, {
        pluginKey = 'EC-Artifact'

        process procName, {
            processType = 'DEPLOY'

            property propertyName, value: 'test', {
                expandable = '0'
                suppressValueTracking = '1'
            }
        }
    }
}
