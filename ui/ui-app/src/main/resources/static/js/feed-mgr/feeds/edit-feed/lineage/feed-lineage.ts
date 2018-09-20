import * as angular from 'angular';
import 'pascalprecht.translate';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
const BroadcastConstants = require('../../../../services/BroadcastConstants');

var directive = function () {
    return {
        restrict: "EA",
        bindToController: {},
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/edit-feed/lineage/feed-lineage.html',
        controller: "FeedLineageController",
        link: function ($scope:any, element:any, attrs:any, controller:any) {

        }

    };
}




export class FeedLineageController implements ng.IComponentController {

    model:any = this.FeedService.editFeedModel;

    /**
     * The Array of  Node Objects
     * @type {Array}
     */
    nodes:Array<any> = [];

    /**
     * the Array of  edges
     * This connects nodes together
     * @type {Array}
     */
    edges:Array<any> = [];

    /**
     * map to determine if edge link is needed
     * @type {{}}
     */
    edgeKeys:any = {};

    /**
     * Data that is used to for the network graph
     * @type {{nodes: VisDataSet, edges: VisDataSet}}
     */
    data:any = {nodes:null, edges:null};

    /**
     * Object holding a reference the node ids that have been processed and connected in the graph
     * @type {{}}
     */
    processedNodes:any = {};

    network:any = null;
    /**
     * The Response data that comes back from the server before the network is built.
     * This is a map of the various feeds {@code feedLineage.feedMap} and datasources {@code self.feedLineage.datasourceMap}
     * that can be used to assemble the graph
     */
    feedLineage:any = null;

    /**
     * flag to indicate when the graph is loading
     * @type {boolean}
     */
    loading:boolean = false;

    /**
     * Enum for modes
     *
     * @type {{SIMPLE: string, DETAILED: string}}
     */
    graphModes:any = {SIMPLE:"SIMPLE",DETAILED:"DETAILED"};

    /**
     * Set the default mode to SIMPLE view
     * @type {string}
     */
    graphMode:any = this.graphModes.DETAILED;



    /**
     * The default text of the node selector
     * @type {{name: string, type: string, content: {displayProperties: null}}}
     */
    SELECT_A_NODE:any = {name: 'Select a node', type: 'Feed Lineage', content: {displayProperties: null}};

    /**
     * The selected Node
     * @type {{name: string, type: string, content: null}}
     */
    selectedNode:any = this.SELECT_A_NODE;

    options:any = null;


    isDetailedGraph=()=>{
        return this.graphMode == this.graphModes.DETAILED;
    }

    redraw=()=>{
        if (this.isDetailedGraph()) {
            this.onDetailedView();
        }
        else {
            this.onSimpleView();
        }
    }

    /**
     * Called when the graph is loaded
     * @param network
     */
    onLoad = function(network:any){
        this.network = network;
       // stabilizationIterationsDone();
    }

    stabilizationIterationsDone = () => {
        this.options.physics.enabled = false;
        if (this.network) {
            this.network.setOptions({
                physics: {enabled: false, stabilization: false},
                interaction: {dragNodes: true},
                layout: {
                    hierarchical: {
                        enabled: false
                    }
                }
            });
        }
    }
    stabilized = () =>{
        this.stabilizationIterationsDone();
    }



    onSelect = (item:any)=>{
        if (item && item.nodes && item.nodes[0]) {
            this.changed = true;
            var firstItem = item.nodes[0];
            var feed = this.feedLineage.feedMap[firstItem];
            if(feed){
                this.selectedNode.name = feed.displayName;
                this.selectedNode.type = 'FEED';
                this.selectedNode.content=feed;
            }
            else {
                var ds = this.feedLineage.datasourceMap[firstItem];
                this.selectedNode.name = ds.name;
                this.selectedNode.type = 'DATASOURCE';
                this.selectedNode.content=ds;
            }

        }
        else {
            this.selectedNode = this.SELECT_A_NODE;
        }
        this.$scope.$apply()
        //console.log(;self.selectedNode);
        //force angular to refresh selection
        angular.element('#hiddenSelectedNode').html(this.selectedNode.name)
    };

    events = {
        onload: this.onLoad.bind(this),
        selectNode: this.onSelect.bind(this),
        stabilized:this.stabilized.bind(this)
      //  stabilizationIterationsDone: stabilizationIterationsDone
    };
    


    _draw = function () {
        this.nodes = [];
        this.edges = [];
        this.edgeKeys ={};
        this.processedDatasource = {};
        this.processedNodes = {};

        //turn on physics for layout
        this.options.physics.enabled = true;
        this.buildVisJsGraph(this.feedLineage.feed);
        this.setNodeData();
    }

    onDetailedView = function () {
        this.graphMode = this.graphModes.DETAILED;
        this._draw();

    }
    onSimpleView = function(){
        this.graphMode = this.graphModes.SIMPLE;
        this._draw();
    }

    setNodeStyle = function (node:any, style:any) {
        if (style) {
            if ((style.shape == 'icon' || !style.shape) && style.icon && style.icon.code) {
                node.shape = 'icon';
                if (angular.isObject(style.icon)) {
                    node.icon = style.icon;
                }
            }
            else if (style.shape) {
                node.shape = style.shape;
            }

            if (style.color) {
                node.color = style.color;
            }
            if (style.size) {
                node.size = style.size;
            }
            if (style.font) {
                node.font = style.font;
            }
            if (!node.font) {
                node.font = {}
            }
            node.font.background = '#ffffff'
        }


    }

    /**
     *  Builds the datasource Node for the passed in {@code dsId}
     * @param feedLineage
     * @param feed
     * @param dsId
     * @param type
     */
    buildDatasource = function (feed:any, dsId:any, type:any) {
        if(dsId) {
            var processedDatasource = this.processedNodes[dsId];

            if (processedDatasource == undefined) {
                var ds = this.feedLineage.datasourceMap[dsId];
                //skip JdbcDatasource entries
                if(ds['@type'] && ds['@type'] == 'JdbcDatasource'){
                    return;
                }

                this.assignDatasourceProperties(ds);
                //console.log('building datasource',ds.name)
                if (this.isDetailedGraph()) {
                    var node = {id: dsId, label: this.datasourceNodeLabel(ds), title: this.datasourceNodeLabel(ds), group: "datasource"};
                    this.nodes.push(node);
                    this.processedNodes[node.id] = node;
                    var style = this.feedLineage.styles[ds.datasourceType];
                    this.setNodeStyle(node, style);


                }

                //build subgraph of feed relationships
                if (ds.sourceForFeeds) {
                    _.each(ds.sourceForFeeds,  (feedItem:any) => {
                        var depFeed = this.feedLineage.feedMap[feedItem.id];
                        if (depFeed.id != feed.id) {
                            var edgeKey = dsId + depFeed.id;
                            var fromId = dsId;
                            var arrows = "to";
                            var label = 'source'
                            if (!this.isDetailedGraph()) {
                                label = '';
                                edgeKey = feed.id + depFeed.id;
                                var edgeKey2 =depFeed.id + feed.id;
                                fromId = feed.id;
                                var exists = this.edgeKeys[edgeKey];
                                var exists2 = this.edgeKeys[edgeKey2];
                                if(!exists && exists2){
                                    exists2.arrows = "to;from";
                                    edgeKey = edgeKey2;
                                }
                            }
                            if (this.edgeKeys[edgeKey] == undefined) {
                                var edge = {from: fromId, to: depFeed.id, arrows: 'to', label: label};
                                this.edgeKeys[edgeKey] = edge;
                                this.edges.push(edge);
                            }
                            this.buildVisJsGraph(depFeed);
                        }
                    });
                }
                if (ds.destinationForFeeds) {
                    _.each(ds.destinationForFeeds,  (feedItem:any) => {
                        var depFeed = this.feedLineage.feedMap[feedItem.id];
                        if (depFeed.id != feed.id) {
                            var edgeKey = dsId + depFeed.id;
                            var toId = dsId;
                            var label = 'destination'
                            if (!this.isDetailedGraph()) {
                                label = ''
                                edgeKey =  depFeed.id +feed.id;
                                toId = feed.id;

                                var edgeKey2 =feed.id + depFeed.id;
                                var exists = this.edgeKeys[edgeKey];
                                var exists2 = this.edgeKeys[edgeKey2];
                                if(!exists && exists2 ){
                                    exists2.arrows = "to;from";
                                    edgeKey = edgeKey2;
                                }
                            }
                            if (this.edgeKeys[edgeKey] == undefined) {
                                var edge = {from: depFeed.id, to: toId, arrows: 'to', label: label};
                                this.edgeKeys[edgeKey] = edge;
                                this.edges.push(edge);
                            }
                            this.buildVisJsGraph(depFeed);
                        }
                    });
                }

            }
            if (this.isDetailedGraph()) {
                if (type == 'source') {
                    var edgeKey = dsId + feed.id;
                    var fromId = dsId;
                    if (this.edgeKeys[edgeKey] == undefined) {
                        var edge = {from: fromId, to: feed.id, arrows: 'to', label: 'source'};
                        this.edgeKeys[edgeKey] = edge;
                        this.edges.push(edge);
                    }
                }
                else {
                    var edgeKey = dsId + feed.id;
                    var toId = dsId;
                    if (this.edgeKeys[edgeKey] == undefined) {
                        var edge = {from: feed.id, to: toId, arrows: 'to', label: 'destination'};
                        this.edgeKeys[edgeKey] = edge;
                        this.edges.push(edge);
                    }
                }

            }
        }
    }


                /**
             * Builds the Graph of Datasources as they are related to the incoming {@code feed}
             * @param feed
             */
            buildDataSourceGraph = function (feed:any) {
                if (feed.sources) {
                    _.each(feed.sources,  (src:any) => {
                        this.buildDatasource(feed, src.datasourceId, 'source');
                    });
                }
                    if (feed.destinations) {
                        _.each(feed.destinations, (dest:any) => {
                            this.buildDatasource(feed, dest.datasourceId, 'destination');
                        })
                    }
    
            }
    
            /**
             * Assigns some of the fields on the datasource to the properties attribute
             * @param ds
             */
            assignDatasourceProperties = function(ds:any){
                var keysToOmit = ['@type', 'id','name','encrypted','compressed','destinationForFeeds','sourceForFeeds'];
                var props = _.omit(ds, keysToOmit);
                props = _.pick(props,  (value:any, key:any) => {
                    return !_.isObject(value);
                });
                ds.displayProperties = props
                this.cleanProperties(ds);
                //add in any properties of its own
                angular.extend(ds.displayProperties, ds.properties);
    
            }
    
            cleanProperties = function (item:any) {
                if (item.properties) {
                    var updatedProps = _.omit(item.properties,  (val:any, key:any) => {
                        return key.indexOf("jcr:") == 0 || key == "tba:properties" || key == 'tba:processGroupId';
                    });
                    item.properties = updatedProps
                }
            }
    
            /**
             * Get the label for the Datasource node
             * @param ds
             * @returns {string}
             */
            datasourceNodeLabel = function(ds:any){
                var label = "";
                if (angular.isString(ds.datasourceType)) {
                    label = this.Utils.endsWith(ds.datasourceType.toLowerCase(), "datasource") ? ds.datasourceType.substring(0, ds.datasourceType.toLowerCase().lastIndexOf("datasource")) : ds.datasourceType;
                    if(label && label.toLowerCase() == 'database'){
                        //attempt to find the name of the database in the properties
                        if(ds.properties && ds.properties['Database Connection']){
                            label = ds.properties['Database Connection'];
                        }
                    }
                } else if (angular.isString(ds.type)) {
                    label = ds.type;
                } else {
                    label = this.Utils.endsWith(ds["@type"].toLowerCase(), "datasource") ? ds["@type"].substring(0, ds["@type"].toLowerCase().lastIndexOf("datasource")) : ds["@type"];
                }
    
                label += "\n" + ds.name;
                return label;
            };
    
            /**
             * Get the label for the Feed Node
             * @param feed
             * @returns {string}
             */
            feedNodeLabel = function(feed:any){
                var label = feed.displayName;
                return label;
            }
    
            /**
             * Create the Feed Node
             * @param feed
             * @returns {{id: *, label: string, title: string, group: string}}
             */
            feedNode = function(feed:any){
    
                var node = {id: feed.id, label: this.feedNodeLabel(feed), title:this.feedNodeLabel(feed), group: "feed"};
                var style = this.feedLineage.styles['feed'];
                this.setNodeStyle(node, style);
                if(feed.id == this.model.id){
                    var style = this.feedLineage.styles['currentFeed'];
                    this.setNodeStyle(node, style);
                }
                this.cleanProperties(feed);
                feed.displayProperties = {};
                //add in any properties of its own
                angular.extend(feed.displayProperties, feed.properties);
    
                return node;
            }
    
            /**
             * Build the Lineage Graph
             * @param feed
             */
            buildVisJsGraph = function (feed:any) {
    
                var node = this.processedNodes[feed.id] == undefined ? this.feedNode(feed) : this.processedNodes[feed.id];
                if(this.processedNodes[node.id] == undefined) {
                //  console.log('building feed',feed.systemName)
                    this.processedNodes[node.id] = node;
                    this.nodes.push(node);
                    this.buildDataSourceGraph(feed);
    
                    //walk the graph both ways (up and down)
                    if (feed.dependentFeedIds) {
                        _.each(feed.dependentFeedIds,  (depFeedId:any) => {
                            //get it from the map
                            var depFeed = this.feedLineage.feedMap[depFeedId];
                            if (this.processedNodes[depFeed.id] == undefined) {
                                this.buildVisJsGraph(depFeed)
                            }
                            var edgeKey = depFeed.id + feed.id;
                            if (this.edgeKeys[edgeKey] == undefined) {
                                var edge = {from: depFeed.id, to: feed.id, arrows: 'from', label: 'depends on'};
                                this.edgeKeys[edgeKey] = edge;
                                this.edges.push(edge);
                            }
                        });
    
                    }
                    if (feed.usedByFeedIds) {
                        _.each(feed.usedByFeedIds, (usedByFeedId:string) => {
                            //get it from the map
                            var usedByFeed = this.feedLineage.feedMap[usedByFeedId];
                            if (this.processedNodes[usedByFeed.id] == undefined) {
                                this.buildVisJsGraph(usedByFeed);
                            }
                            var edgeKey = feed.id + usedByFeed.id;
                            if (this.edgeKeys[edgeKey] == undefined) {
                                // edgeKeys[edgeKey] = edgeKey;
                                //taken care of from the other side
                                //   edges.push({from: feed.id, to: usedByFeed.id});
                            }
    
                        });
                    }
                }
    
            }
    
            /**
             * Get the Feed Lineage Graph and build the network
             * @param feedId
             */
            getFeedLineage = function(feedId:any) {
                this.loading = true;
    
                this.$http.get(this.RestUrlService.FEED_LINEAGE_URL(feedId)).then((response:any) => {
                    this.feedLineage = response.data;
                    this.loading = false;
                    this.redraw();
    
    
                });
            }
            /**
             * attach the nodes to the data to the graph
             */
            setNodeData = function(){
                var visNodes = new this.VisDataSet(this.nodes);
                var visEdges = new this.VisDataSet(this.edges);
                this.data = {nodes:visNodes, edges:visEdges};
            }
    
            /**
             * Called when a Node is selected
             * @param item
             */
            changed = false;
    

    
            navigateToFeed = function () {
                if (this.selectedNode.type == 'FEED' && this.selectedNode.content) {
                    this.StateService.FeedManager().Feed().navigateToFeedDetails(this.selectedNode.content.id, 2);
                }
    
            }




            constructor(private $scope:any, private $element:any, private $http:any, private $mdDialog:any, private $timeout:any, private AccessControlService:any
                , private FeedService:any, private RestUrlService:any, private VisDataSet:any, private Utils:any, private BroadcastService:any, private StateService:any) {
        
                    this.BroadcastService.subscribe(this.$scope, BroadcastConstants.CONTENT_WINDOW_RESIZED, this.redraw);
            
                    /**
                     *
                     * @type {{height: string, width: string, edges: {smooth: {forceDirection: string}}, physics: {barnesHut: {springLength: number}, minVelocity: number}, layout: {hierarchical: {enabled:
                     *     boolean}}, nodes: {shape: string, font: {align: string}}, groups: {feed: {color: {background: string}, font: {color: string}}, datasource: {color: {background: string}, font: {color:
                     *     string}}}, interaction: {hover: boolean}}}
                     */
                    this.options = {
                        height: '100%',
                        width: '100%',
                        "edges": {
                            "arrowStrikethrough": false,
                            smooth: {
                                enabled: true,
                                type: "cubicBezier",
                                roundness: 0,
                                "forceDirection": "horizontal"
                            },
                            font: {align: 'horizontal'}
                        },
                        layout: {
                            hierarchical: {
                                direction: "LR",
                                nodeSpacing:200,
                                sortMethod:'directed'
                            }
                        },
                        nodes: {
                            shape: 'box',
                            font: {
                                align: 'center'
                            }
                        },
                        groups: {
                            feed: {
                                shape: 'box',
                                font: {
                                    align: 'center'
                                }
                            },
                            datasource: {
                                shape: 'box',
                                font: {
                                    align: 'center'
                                }
                            }
                        },
                        interaction: {
                            hover: true, navigationButtons: true,
                            keyboard: true
                        }
                    };
            
            
            
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
                 * Load the Graph
                 */
                this.getFeedLineage(this.model.id);
        
            
                    
            }


}


// define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {






    angular.module(moduleName).controller('FeedLineageController', ["$scope","$element","$http","$mdDialog","$timeout","AccessControlService","FeedService","RestUrlService","VisDataSet","Utils","BroadcastService","StateService",FeedLineageController]);

    angular.module(moduleName)
        .directive('thinkbigFeedLineage', directive);



