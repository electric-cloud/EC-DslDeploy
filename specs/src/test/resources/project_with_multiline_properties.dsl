project 'BEE-30105', {
    tracked = '1'

    // Custom properties

    property 'nestedSheet', {

        // Custom properties

        property 'double quote', value: 'test " test', {
            description = 'test " test'
        }

        property 'double quote multiline', value: '''test
"
test''', {
            description = '''test
"
test'''
        }

        property 'many double quotes', value: '''"""
"""
"""''', {
            description = '''"""
"""
"""'''
        }

        property 'many mixed quotes', value: '''\'\'\'
"""
\'\'\'
"""''', {
            description = '''\'\'\'
"""
\'\'\'
"""'''
        }

        property 'many single quotes', value: '''\'\'\'
\'\'\'
\'\'\'''', {
            description = '''\'\'\'
\'\'\'
\'\'\''''
        }

        property 'single quote', value: 'test \' test', {
            description = 'test \' test'
        }

        property 'single quote multiline', value: '''test
\'
test''', {
            description = '''test
\'
test'''
        }
    }
}