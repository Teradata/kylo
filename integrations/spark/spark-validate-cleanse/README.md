Spark Validate-Cleanse Job
==========

### Overview

A Spark job capable of performing data validation and cleansing/standardization of data. This is a key component of the standard ingest pipeline.  

### How it works

This Spark job assumes Hive table naming conventions following standard-ingest processing standards.  

1. Data is read from a source table <entity>-feed and partition
2. Standardization policies are applied to the data
3. Validation is performed and data inserted into two different tables <entity>-valid and <entity>-invalid with a new partition matching the source (typically a processing timestamp).   

The policies for standardization and validation are supplied externally via a JSON field-policies file (see /core/field-policy).

### Execution

The project is a spark-submit job:

./bin/spark-submit \
  --class com.thinkbiganalytics.spark.datavalidator.Validator \
  --master yarn-client \
  /path/to/jar/kylo-spark-validate-cleanse-\<version>-jar-with-dependencies.jar \
  \<targetDatabase> \<entity> \<partition> \</path/to/policy/file.json>

Command-line arguments:
* targetDatabase - name of the Hive database
* entity - root name of the Hive table (e.g. employee). The actual table names will be derived based on convention.  e.g. employee_feed
* partition - name of the partition in the source
* path-to-policy-file - path to the json policy file

### Example Policy file


```javascript
[
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"registration_dttm",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"id",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"first_name",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"last_name",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"email",
    "standardization":null,
    "validation":[
      {
        "name":"Email",
        "displayName":"Email",
        "description":"Valid email address",
        "properties":[

        ],
        "objectClassType":"com.thinkbiganalytics.policy.validation.EmailValidator",
        "regex":null,
        "type":null
      }
    ]
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"gender",
    "standardization":null,
    "validation":[
      {
        "name":"Lookup",
        "displayName":"Lookup",
        "description":"Must be contained in the list",
        "properties":[
          {
            "name":"List",
            "displayName":"List",
            "value":"Male,Female",
            "placeholder":"",
            "type":"string",
            "hint":"Comma separated list of values",
            "objectProperty":"lookupList",
            "selectableValues":[

            ]
          }
        ],
        "objectClassType":"com.thinkbiganalytics.policy.validation.LookupValidator",
        "regex":null,
        "type":null
      }
    ]
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"ip_address",
    "standardization":null,
    "validation":[
      {
        "name":"IP Address",
        "displayName":"IP Address",
        "description":"Valid IP Address",
        "properties":[

        ],
        "objectClassType":"com.thinkbiganalytics.policy.validation.IPAddressValidator",
        "regex":null,
        "type":null
      }
    ]
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"cc",
    "standardization":[
      {
        "name":"Mask Credit Card",
        "displayName":"Mask Credit Card",
        "description":"Preserves last 4 digits",
        "properties":[

        ],
        "objectClassType":"com.thinkbiganalytics.policy.standardization.MaskLeavingLastFourDigitStandardizer"
      }
    ],
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"country",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"birthdate",
    "standardization":[
      {
        "name":"Date/Time",
        "displayName":"Date/Time",
        "description":"Converts any date to ISO8601",
        "properties":[
          {
            "name":"Date Format",
            "displayName":"Date Format",
            "value":"MM/dd/YYYY",
            "placeholder":"",
            "type":"string",
            "hint":"Format Example: MM/DD/YYYY",
            "objectProperty":"inputDateFormat",
            "selectableValues":[

            ]
          },
          {
            "name":"Output Format",
            "displayName":"Output Format",
            "value":"DATE_ONLY",
            "placeholder":"",
            "type":"select",
            "hint":"Choose an output format",
            "objectProperty":"outputFormat",
            "selectableValues":[
              {
                "label":"DATE_ONLY",
                "value":"DATE_ONLY"
              },
              {
                "label":"DATETIME",
                "value":"DATETIME"
              },
              {
                "label":"DATETIME_NOMILLIS",
                "value":"DATETIME_NOMILLIS"
              }
            ]
          }
        ],
        "objectClassType":"com.thinkbiganalytics.policy.standardization.DateTimeStandardizer"
      }
    ],
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"salary",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"title",
    "standardization":null,
    "validation":null
  },
  {
    "partition":false,
    "profile":true,
    "index":false,
    "fieldName":"comments",
    "standardization":null,
    "validation":null
  }
]
```