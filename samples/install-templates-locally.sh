#!/bin/bash

##########
#
# installs the sample templates in the Kylo sandbox
#
##########

curl -i -X POST -u dladmin:thinkbig -H "Content-Type: multipart/form-data" \
    -F "overwrite=false" \
    -F "categorySystemName=" \
    -F "importConnectingReusableFlow=NOT_SET" \
    -F "file=@/opt/kylo/setup/data/feeds/nifi-1.3/index_text_service_v2.feed.zip" \
     http://localhost:8400/proxy/v1/feedmgr/admin/import-feed

curl -i -X POST -u dladmin:thinkbig -H "Content-Type: multipart/form-data" \
    -F "file=@/opt/kylo/setup/data/templates/nifi-1.0/data_ingest.zip" \
    -F "overwrite=false" \
    -F "createReusableFlow=false" \
    -F "importConnectingReusableFlow=YES" \
    http://localhost:8400/proxy/v1/feedmgr/admin/import-template

curl -i -X POST -u dladmin:thinkbig -H "Content-Type: multipart/form-data" \
    -F "file=@/opt/kylo/setup/data/templates/nifi-1.0/data_transformation.zip" \
    -F "overwrite=false" \
    -F "createReusableFlow=false" \
    -F "importConnectingReusableFlow=YES" \
    http://localhost:8400/proxy/v1/feedmgr/admin/import-template

curl -i -X POST -u dladmin:thinkbig -H "Content-Type: multipart/form-data" \
    -F "file=@/opt/kylo/setup/data/templates/nifi-1.0/data_confidence_invalid_records.zip" \
    -F "overwrite=true" \
    -F "createReusableFlow=false" \
    -F "importConnectingReusableFlow=NOT_SET" \
    http://localhost:8400/proxy/v1/feedmgr/admin/import-template