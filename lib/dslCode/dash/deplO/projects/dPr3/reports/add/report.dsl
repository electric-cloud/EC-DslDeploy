
report 'add', {
  definition = '''

    {

        "size": 0,

        "query": {

            "bool" : {

                "filter": [

                    {"term" : { "reportEventType" : "ef_process_run_completed" }},

                    {"term" : { "deploymentOutcome" : "success" }},

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

                 "avg_duration": {

                     "avg" : {

                       "script" : {

                           "inline" : "doc.jobFinish.value.toInstant().toEpochMilli() - doc.jobStart.value.toInstant().toEpochMilli()"

                       }

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
  title = 'Average Deployment Duration'
  uri = 'ef-deployment-*/_search?pretty'
}
