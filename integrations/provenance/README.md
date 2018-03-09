Provenance API
==============

This is the API and 2 implementations for making custom Provenance data, outside of NiFi, available for Kylo Operations Manager
This is will use the [nifi-provenance-model](../nifi/nifi-provenance-model) to help create the events to publish



| Module        | Description           |
| ------------- |-------------|
| [provenance-api](provenance-api) | This is the interface that is used to send the events
| [provenance-jms](provenance-jms) | JMS implementation of the provenance-api. This will connect to Kylo's JMS queues and send the events and stats for Kylo Ops Manager to receive
| [provenance-kafka](provenance-kafka) | Kafka implementation of the provenance-api.  This will connect to Kafka and publish the events to two Kafka topic's.  This is useful if you want to generate custom provenance events within your cluster (i.e. a spark job).  You will need a subsequent Kafka Consumer, or a NiFi flow to handle receiving the Kafka messages and publishing them to JMS for Kylo Ops Manager.
| [provenance-rest](provenance-rest) | REST implementation of the provenance-api. This will connect to Kylo via REST and publish the events for Ops Maanger


## Complete Examples
   Refer to the [Kylo Documentation](http://kylo.readthedocs.io/en/master/how-to-guides/CustomProvenanceEvents.html) for a detailed example.


