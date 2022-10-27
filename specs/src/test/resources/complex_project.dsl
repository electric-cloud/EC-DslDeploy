package ec.backend.dsl.dsl_improvements.dsl_plugin

def projName = 'proj_name'
def compNames = [c1: 'comp_name1', c2: 'comp_name2']
def procNames = [p1: 'proc_name1', p2: 'proc_name2']
def stepNames = [s1: 'step1', s2: 'step2']
def propertyNames = [p1: 'property1', p2: 'property2']
def formalParameterNames = [p1: 'param1', p2: 'param2']

project projName, {

    component compNames.c1, {
        pluginKey = 'EC-Artifact'

        process procNames.p1, {
            processType = 'DEPLOY'

            processStep stepNames.s1, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processStep stepNames.s2, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processDependency stepNames.s1, targetProcessStepName: stepNames.s2

            property propertyNames.p1,{
                property propertyNames.p2, value: 'test', {
                    expandable = '0'
                    suppressValueTracking = '1'
                }
            }

            formalParameter formalParameterNames.p1, defaultValue: null,{
                required = '0'
                orderIndex = '1'
                type = 'entry'
            }

            formalParameter formalParameterNames.p2, defaultValue: 'test',{
                expansionDeferred = '1'
                label = 'test'
                orderIndex = '2'
                required = '1'
                type = 'entry'
            }
        }

        process procNames.p2, {
            processType = 'DEPLOY'

            processStep stepNames.s1, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processStep stepNames.s2, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processDependency stepNames.s1, targetProcessStepName: stepNames.s2

            property propertyNames.p1,{
                property propertyNames.p2, value: 'test', {
                    expandable = '0'
                    suppressValueTracking = '1'
                }
            }

            formalParameter formalParameterNames.p1, defaultValue: null,{
                required = '0'
                orderIndex = '1'
                type = 'entry'
            }

            formalParameter formalParameterNames.p2, defaultValue: 'test',{
                expansionDeferred = '1'
                label = 'test'
                orderIndex = '2'
                required = '1'
                type = 'entry'
            }
        }

        property propertyNames.p1,{
            property propertyNames.p2, value: 'test', {
                expandable = '0'
                suppressValueTracking = '1'
            }
        }

        formalParameter formalParameterNames.p1, defaultValue: null,{
            required = '0'
            orderIndex = '1'
            type = 'entry'
        }

        formalParameter formalParameterNames.p2, defaultValue: 'test',{
            expansionDeferred = '1'
            label = 'test'
            orderIndex = '2'
            required = '1'
            type = 'entry'
        }
    }

    component compNames.c2, {
        pluginKey = 'EC-Artifact'

        process procNames.p1, {
            processType = 'DEPLOY'

            processStep stepNames.s1, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processStep stepNames.s2, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processDependency stepNames.s1, targetProcessStepName: stepNames.s2

            property propertyNames.p1,{
                property propertyNames.p2, value: 'test', {
                    expandable = '0'
                    suppressValueTracking = '1'
                }
            }

            formalParameter formalParameterNames.p1, defaultValue: null,{
                required = '0'
                orderIndex = '1'
                type = 'entry'
            }

            formalParameter formalParameterNames.p2, defaultValue: 'test',{
                expansionDeferred = '1'
                label = 'test'
                orderIndex = '2'
                required = '1'
                type = 'entry'
            }
        }

        process procNames.p2, {
            processType = 'DEPLOY'

            processStep stepNames.s1, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processStep stepNames.s2, {
                dependencyJoinType = 'and'
                notificationEnabled = '1'
                notificationTemplate = 'ec_default_manual_process_step_notification_template'
                processStepType = 'manual'
                assignee = [
                        'admin',
                ]

                property propertyNames.p1,{
                    property propertyNames.p2, value: 'test', {
                        expandable = '0'
                        suppressValueTracking = '1'
                    }
                }

                formalParameter formalParameterNames.p1, defaultValue: null,{
                    required = '0'
                    orderIndex = '1'
                    type = 'entry'
                }

                formalParameter formalParameterNames.p2, defaultValue: 'test',{
                    expansionDeferred = '1'
                    label = 'test'
                    orderIndex = '2'
                    required = '1'
                    type = 'entry'
                }
            }

            processDependency stepNames.s1, targetProcessStepName: stepNames.s2

            property propertyNames.p1,{
                property propertyNames.p2, value: 'test', {
                    expandable = '0'
                    suppressValueTracking = '1'
                }
            }

            formalParameter formalParameterNames.p1, defaultValue: null,{
                required = '0'
                orderIndex = '1'
                type = 'entry'
            }

            formalParameter formalParameterNames.p2, defaultValue: 'test',{
                expansionDeferred = '1'
                label = 'test'
                orderIndex = '2'
                required = '1'
                type = 'entry'
            }
        }

        property propertyNames.p1,{
            property propertyNames.p2, value: 'test', {
                expandable = '0'
                suppressValueTracking = '1'
            }
        }

        formalParameter formalParameterNames.p1, defaultValue: null,{
            required = '0'
            orderIndex = '1'
            type = 'entry'
        }

        formalParameter formalParameterNames.p2, defaultValue: 'test',{
            expansionDeferred = '1'
            label = 'test'
            orderIndex = '2'
            required = '1'
            type = 'entry'
        }
    }
}
