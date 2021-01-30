
report 'df', {
  description = 'Frequency of Deployments over time'
  definition = '''

    {

        "size": 0,

        "query": {

            "bool" : {

                "filter": [

                    {"term" : {  "reportEventType" : "ef_process_run_completed" }},

                    {"exists" : {  "field" : "applicationName" }}

                ]

            }

        },

        "aggregations": {

            "deployment_date": {

               "date_histogram": {

                   "field": "jobFinish",

                   "interval": "day",

                   "format": "yyyy-MM-dd",

                   "min_doc_count": "1"

               },

               "aggregations": {

                  "deployment_outcome": {

                     "terms": {

                        "script": "doc.containsKey(\'jobType\') && doc[\'jobType\'].value != null ? doc[\'jobType\'].value : doc.deploymentOutcome"

                     }

                  },

                  "deployment_date_max" : { "max" : { "field" : "jobFinish", "format" : "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'" } },

                  "deployment_date_min" : { "min" : { "field" : "jobFinish", "format" : "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'" } }

               }

           }

      }

    }

    '''
  reportObjectTypeName = 'deployment'
  title = 'Breakdown of deployments by outcome over time'
  uri = 'ef-deployment-*/_search?pretty'
}
