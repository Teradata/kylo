nifi-provenance-repo-bundle
==========

### Overview

Think Big provenance repository is an adapter that allows replication of NiFi provenance events. Events are replicated to a JMS broker for consumption by the  

### Deployment

1. The NAR must be deployed to /nifi/lib/
2. The conf/nifi.properties entry must be updated to reference the Think Big provenence repository:
nifi.provenance.repository.implementation=com.thinkbiganalytics.nifi.provenance.v2.ThinkbigProvenanceEventRepository

