NiFi Flow Visitor
====
This is a visitor that will walk a Nifi ProcessGroup and construct a graph connecting all the NifiProcessors together.
This visitor walks each connection and input/output port, processgroup, and Funnel within the supplied process group. It then removes those extra objects (input/output ports, ProcessGroups, Funnels,) ane leaves a graph with a processor connecting to a Set of source processors and a Set of destination processors
The object supplied to visit is a NifiVisitableProcessGroup which connects detailed information about the NiFi flow embedding Nifi web DTO objects such as a ProcessorDTO.
If you wish to get a more lightweight graph of objects you can use the NifiFlowBuilder to construct a simplied version of this graph.
The NiFiRestClient and NiFiFlowClient (Spring client) already has methods that you can use to access this visitor


```java
   /**
    * returns the graph along with the Nifi DTO objects
    **/
   public NifiVisitableProcessGroup getFlowOrder(String processGroupId) throws NifiComponentNotFoundException
   
   /**
    * returns the lighterweight graph of objects
    **/
   public NifiFlowProcessGroup getFeedFlow(String processGroupId) throws NifiComponentNotFoundException

   /**
    * returns a list of all the Feed flows starting with the "root" NiFi canvas.
    **/
   public List<NifiFlowProcessGroup> getFeedFlows()

```
