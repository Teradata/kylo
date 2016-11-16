(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/lineage/feed-lineage.html',
            controller: "FeedLineageController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $element, $http,$mdDialog, $timeout, AccessControlService, FeedService, RestUrlService,VisDataSet) {

        this.model = FeedService.editFeedModel;

        /**
         * Reference to this controller
         * @type {controller}
         */
        var self = this;
        /**
         * The Array of  Node Objects
         * @type {Array}
         */
        var nodes = [];

        /**
         * the Array of  edges
         * This connects nodes together
         * @type {Array}
         */
        var edges = [];

        /**
         * map to determine if edge link is needed
         * @type {{}}
         */
        var edgeKeys = {};

        /**
         * Data that is used to for the network graph
         * @type {{nodes: VisDataSet, edges: VisDataSet}}
         */
        self.data = {nodes:null, edges:null};

        /**
         * Object holding a reference the node ids that have been processed and connected in the graph
         * @type {{}}
         */
        var processedNodes = {};

        /**
         * The Response data that comes back from the server before the network is built.
         * This is a map of the various feeds {@code self.feedLineage.feedMap} and datasources {@code self.feedLineage.datasourceMap}
         * that can be used to assemble the graph
         */
        self.feedLineage = null;

        /**
         * flag to indicate when the graph is loading
         * @type {boolean}
         */
        self.loading = false;

        var graphModes = {SIMPLE:"SIMPLE",DETAILED:"DETAILED"};

        var graphMode = graphModes.SIMPLE;


        /**
         * The selected Node
         * @type {{name: string, type: string, content: null}}
         */
        self.selectedNode = {name:'Select a node',type:'Feed Lineage',content:null}

        /**
         *
         * @type {{height: string, width: string, edges: {smooth: {forceDirection: string}}, physics: {barnesHut: {springLength: number}, minVelocity: number}, layout: {hierarchical: {enabled:
         *     boolean}}, nodes: {shape: string, font: {align: string}}, groups: {feed: {color: {background: string}, font: {color: string}}, datasource: {color: {background: string}, font: {color:
         *     string}}}, interaction: {hover: boolean}}}
         */
        self.options = {
            height: '100%',
            width: '100%',
            "edges": {
                "smooth": {
                    "forceDirection": "none"
                }
            },
            "physics": {
                "barnesHut": {
                    "springLength":300
                },
                "minVelocity": 0.75
            },
            layout: {
                hierarchical: {
                    enabled: false
                },
                randomSeed:2
            },
            nodes: {
                shape: 'box',
                font: {
                     align: 'left'
                }

            },
            groups:{
            feed:{
                color:{background:'#3483BA'},
                font:{color:'white'}
            },
                datasource:{
                    color:{background:'yellow'},
                    font:{color:'black'}
                }
            },
            interaction:{hover:true}
        };

        var isDetailedGraph = function(){
            return graphMode == graphModes.DETAILED;
        }


        self.onDetailedView = function(){
            graphMode = graphModes.DETAILED;
            console.log('DETAILED')
            nodes = [];
            edges = [];
            edgeKeys ={};
            processedDatasource = {};
            processedNodes = {};

            buildVisJsGraph(self.feedLineage,self.feedLineage.feed);
            setNodeData();
        }
        self.onSimpleView = function(){
            graphMode = graphModes.SIMPLE;
            console.log('SIMPLE')
            nodes = [];
            edges = [];
            edgeKeys ={};
            processedDatasource = {};
            processedNodes = {};

            buildVisJsGraph(self.feedLineage,self.feedLineage.feed);
            setNodeData();
        }
        /**
         *  Builds the datasource Node for the passed in {@code dsId}
         * @param feedLineage
         * @param feed
         * @param dsId
         * @param type
         */
        var buildDatasource = function(feedLineage,feed,dsId, type){
            if(dsId) {
                var processedDatasource = processedNodes[dsId];

                if (processedDatasource == undefined) {
                    var ds = feedLineage.datasourceMap[dsId];
                    assignDatasourceProperties(ds);
                    //console.log('building datasource',ds.name)
                    if (isDetailedGraph()) {
                        var node = {id: dsId, label: datasourceNodeLabel(ds), group: "datasource"};
                        nodes.push(node);
                        processedNodes[node.id] = node;
                    }

                    //build subgraph of feed relationships
                    if (ds.sourceForFeeds) {
                        _.each(ds.sourceForFeeds, function (feedItem) {
                            var depFeed = feedLineage.feedMap[feedItem.id];
                            if (depFeed.id != feed.id) {
                                var edgeKey = dsId + depFeed.id;
                                var fromId = dsId;
                                var arrows = "to";
                                var label = 'source'
                                if (!isDetailedGraph()) {
                                    label = '';
                                   edgeKey = feed.id + depFeed.id;
                                   var edgeKey2 =depFeed.id + feed.id;
                                   fromId = feed.id;
                                   var exists = edgeKeys[edgeKey];
                                    var exists2 = edgeKeys[edgeKey2];
                                    if(!exists && exists2){
                                        exists2.arrows = "to;from";
                                        edgeKey = edgeKey2;
                                    }
                                }
                                if (edgeKeys[edgeKey] == undefined) {
                                    var edge = {from: fromId, to: depFeed.id, arrows: 'to', label: label};
                                    edgeKeys[edgeKey] = edge;
                                    edges.push(edge);
                                }
                                buildVisJsGraph(feedLineage, depFeed);
                            }
                        });
                    }
                    if (ds.destinationForFeeds) {
                        _.each(ds.destinationForFeeds, function (feedItem) {
                            var depFeed = feedLineage.feedMap[feedItem.id];
                            if (depFeed.id != feed.id) {
                                var edgeKey = dsId + depFeed.id;
                                var toId = dsId;
                                var label = 'destination'
                                if (!isDetailedGraph()) {
                                    label = ''
                                    edgeKey =  depFeed.id +feed.id;
                                    toId = feed.id;

                                    var edgeKey2 =feed.id + depFeed.id;
                                    var exists = edgeKeys[edgeKey];
                                    var exists2 = edgeKeys[edgeKey2];
                                    if(!exists && exists2 ){
                                        exists2.arrows = "to;from";
                                        edgeKey = edgeKey2;
                                    }
                                }
                                if (edgeKeys[edgeKey] == undefined) {
                                    var edge = {from: depFeed.id, to: toId, arrows: 'to', label: label};
                                    edgeKeys[edgeKey] = edge;
                                    edges.push(edge);
                                }
                                buildVisJsGraph(feedLineage, depFeed);
                            }
                        });
                    }

                }
                if (isDetailedGraph()) {
                    if (type == 'source') {
                        var edgeKey = dsId + feed.id;
                        var fromId = dsId;
                        if (edgeKeys[edgeKey] == undefined) {
                            var edge = {from: fromId, to: feed.id, arrows: 'to', label: 'source'};
                            edgeKeys[edgeKey] = edge;
                            edges.push(edge);
                        }
                    }
                    else {
                        var edgeKey = dsId + feed.id;
                        var toId = dsId;
                        if (edgeKeys[edgeKey] == undefined) {
                            var edge = {from: feed.id, to: toId, arrows: 'to', label: 'destination'};
                            edgeKeys[edgeKey] = edge;
                            edges.push(edge);
                        }
                    }

                }
            }
        }

        /**
         * Builds the Graph of Datasources as they are related to the incoming {@code feed}
         * @param feed
         */
         var buildDataSourceGraph = function(feedLineage,feed) {
            if (feed.sources) {
                _.each(feed.sources, function (src) {
                    buildDatasource(feedLineage, feed, src.datasourceId, 'source');
                });
            }
                if (feed.destinations) {
                    _.each(feed.destinations, function (dest) {
                        buildDatasource(feedLineage, feed, dest.datasourceId, 'destination');
                    })
                }

        }

        /**
         * Assigns some of the fields on the datasource to the properties attribute
         * @param ds
         */
        var assignDatasourceProperties = function(ds){
            var keysToOmit = ['@type', 'id','name','encrypted','compressed','destinationForFeeds','sourceForFeeds'];
            ds.properties  = _.omit(ds,keysToOmit);

        }

        /**
         * Get the label for the Datasource node
         * @param ds
         * @returns {string}
         */
        var datasourceNodeLabel = function(ds){
            var label = "Datasource\n"+ds.name+"\n";
            return label;
        }

        /**
         * Get the label for the Feed Node
         * @param feed
         * @returns {string}
         */
        var feedNodeLabel = function(feed){
            var label = "Feed\n"+feed.displayName+" \n";
            //label +="State: "+feed.state;
            return label;
        }

        /**
         * Create the Feed Node
         * @param feed
         * @returns {{id: *, label: string, title: string, group: string}}
         */
        var feedNode = function(feed){

            var node = {id: feed.id, label: feedNodeLabel(feed), title:feedNodeLabel(feed), group: "feed"};
            if(feed.id == self.model.id){
                if(node.font == undefined){
                    node.font = {};
                }
                node.font.size=18;
                node.font.color='white';
                node.color = {background:'#F08C38'}
            }
            return node;
        }

        /**
         * Build the Lineage Graph
         * @param feedLineage
         * @param feed
         */
        var buildVisJsGraph = function(feedLineage, feed){

            var node = processedNodes[feed.id] == undefined ? feedNode(feed) : processedNodes[feed.id];
          if(processedNodes[node.id] == undefined) {
            //  console.log('building feed',feed.systemName)
              processedNodes[node.id] = node;
              nodes.push(node);
              buildDataSourceGraph(feedLineage, feed);

              //walk the graph both ways (up and down)
              if (feed.dependentFeedIds) {
                  _.each(feed.dependentFeedIds, function (depFeedId) {
                      //get it from the map
                      var depFeed = feedLineage.feedMap[depFeedId];
                      if (processedNodes[depFeed.id] == undefined) {
                          buildVisJsGraph(feedLineage, depFeed)
                      }
                      var edgeKey = depFeed.id + feed.id;
                      if (edgeKeys[edgeKey] == undefined) {
                          var edge = {from: depFeed.id, to: feed.id, arrows: 'from', label: 'depends on'};
                          edgeKeys[edgeKey] = edge;
                          edges.push(edge);
                      }
                  });

              }
              if (feed.usedByFeedIds) {
                  _.each(feed.usedByFeedIds, function (usedByFeedId) {
                      //get it from the map
                      var usedByFeed = feedLineage.feedMap[usedByFeedId];
                      if (processedNodes[usedByFeed.id] == undefined) {
                          buildVisJsGraph(feedLineage, usedByFeed);
                      }
                      var edgeKey = feed.id + usedByFeed.id;
                      if (edgeKeys[edgeKey] == undefined) {
                          // edgeKeys[edgeKey] = edgeKey;
                          //taken care of from the other side
                          //   edges.push({from: feed.id, to: usedByFeed.id});
                      }

                  });
              }
          }

        }

/*
        self.clusterByDatasource = function() {

            var clusterOptionsByData = {
                joinCondition:function(childOptions) {
                    return childOptions.datasource == 1;
                },
                clusterNodeProperties: {id:'cidCluster', borderWidth:3, shape:'database'}
            };
            network.cluster(clusterOptionsByData);
        }
        */

        /**
         * Get the Feed Lineage Graph and build the network
         * @param feedId
         */
        var getFeedLineage = function(feedId) {
            self.loading = true;

            $http.get(RestUrlService.FEED_LINEAGE_URL(feedId)).then(function(response){
                self.feedLineage = response.data;
                self.loading = false;
            buildVisJsGraph(response.data,response.data.feed);
           setNodeData();


            });
        }
        /**
         * attach the nodes to the data to the graph
         */
        var setNodeData = function(){
            var visNodes = new VisDataSet(nodes);
            var visEdges = new VisDataSet(edges);
            self.data = {nodes:visNodes, edges:visEdges};
        }

        /**
         * Called when a Node is selected
         * @param item
         */
        var onSelect = function (item) {
            if(item && item.nodes && item.nodes[0]){
                var firstItem = item.nodes[0];
                var feed = self.feedLineage.feedMap[firstItem];
                if(feed){
                    self.selectedNode.name = feed.displayName;
                    self.selectedNode.type = 'FEED';
                    self.selectedNode.content=feed;
                }
                else {
                    var ds = self.feedLineage.datasourceMap[firstItem];
                    self.selectedNode.name = ds.name;
                    self.selectedNode.type = 'DATASOURCE';
                    self.selectedNode.content=ds;
                }

            }
            else {
                self.selectedNode.name = 'Select a node'
                self.selectedNode.type = 'Feed Lineage';
                self.selectedNode.content=null;
            }
        };

        /**
         * Called when the graph is loaded
         * @param network
         */
        var onLoad = function(network){
           // console.log(network);
        }


        self.events = {
            onLoad:onLoad,
            selectNode: onSelect
        };

        /**
         * Load the Graph
         */
        getFeedLineage(self.model.id);





    };

    angular.module(MODULE_FEED_MGR).controller('FeedLineageController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedLineage', directive);

})();
