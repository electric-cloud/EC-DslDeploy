
project 'webhook-plugin-69.0', {
    procedure 'webhook-definition',{
        formalParameter 'pushEvent', {
        }
    }
    property 'ec_webhook', {
        property 'default',{
            property 'procedureName',{
                value = 'webhook-definition'
            }
            property 'script',{
                value = '''
            return [
             eventType        : 'push',
             webhookData      : ['some data': 'some data'],
             launchWebhook    : true]
            '''
            }
        }
    }
}
plugin('webhook-plugin', '69.0', 'webhook-plugin-69.0')
promotePlugin pluginName: 'webhook-plugin-69.0'