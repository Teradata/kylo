define(["require", "exports", "angular", "underscore", "pascalprecht.translate"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var BroadcastConstants = require('../../../../services/BroadcastConstants');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/lineage/feed-lineage.html',
            controller: "FeedLineageController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var FeedLineageController = /** @class */ (function () {
        function FeedLineageController($scope, $element, $http, $mdDialog, $timeout, AccessControlService, FeedService, RestUrlService, VisDataSet, Utils, BroadcastService, StateService) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$timeout = $timeout;
            this.AccessControlService = AccessControlService;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.VisDataSet = VisDataSet;
            this.Utils = Utils;
            this.BroadcastService = BroadcastService;
            this.StateService = StateService;
            this.model = this.FeedService.editFeedModel;
            /**
             * The Array of  Node Objects
             * @type {Array}
             */
            this.nodes = [];
            /**
             * the Array of  edges
             * This connects nodes together
             * @type {Array}
             */
            this.edges = [];
            /**
             * map to determine if edge link is needed
             * @type {{}}
             */
            this.edgeKeys = {};
            /**
             * Data that is used to for the network graph
             * @type {{nodes: VisDataSet, edges: VisDataSet}}
             */
            this.data = { nodes: null, edges: null };
            /**
             * Object holding a reference the node ids that have been processed and connected in the graph
             * @type {{}}
             */
            this.processedNodes = {};
            this.network = null;
            /**
             * The Response data that comes back from the server before the network is built.
             * This is a map of the various feeds {@code feedLineage.feedMap} and datasources {@code self.feedLineage.datasourceMap}
             * that can be used to assemble the graph
             */
            this.feedLineage = null;
            /**
             * flag to indicate when the graph is loading
             * @type {boolean}
             */
            this.loading = false;
            /**
             * Enum for modes
             *
             * @type {{SIMPLE: string, DETAILED: string}}
             */
            this.graphModes = { SIMPLE: "SIMPLE", DETAILED: "DETAILED" };
            /**
             * Set the default mode to SIMPLE view
             * @type {string}
             */
            this.graphMode = this.graphModes.DETAILED;
            /**
             * The default text of the node selector
             * @type {{name: string, type: string, content: {displayProperties: null}}}
             */
            this.SELECT_A_NODE = { name: 'Select a node', type: 'Feed Lineage', content: { displayProperties: null } };
            /**
             * The selected Node
             * @type {{name: string, type: string, content: null}}
             */
            this.selectedNode = this.SELECT_A_NODE;
            this.options = null;
            this.isDetailedGraph = function () {
                return _this.graphMode == _this.graphModes.DETAILED;
            };
            this.redraw = function () {
                if (_this.isDetailedGraph()) {
                    _this.onDetailedView();
                }
                else {
                    _this.onSimpleView();
                }
            };
            /**
             * Called when the graph is loaded
             * @param network
             */
            this.onLoad = function (network) {
                this.network = network;
                // stabilizationIterationsDone();
            };
            this.stabilizationIterationsDone = function () {
                _this.options.physics.enabled = false;
                if (_this.network) {
                    _this.network.setOptions({
                        physics: { enabled: false, stabilization: false },
                        interaction: { dragNodes: true },
                        layout: {
                            hierarchical: {
                                enabled: false
                            }
                        }
                    });
                }
            };
            this.stabilized = function () {
                _this.stabilizationIterationsDone();
            };
            this.onSelect = function (item) {
                if (item && item.nodes && item.nodes[0]) {
                    _this.changed = true;
                    var firstItem = item.nodes[0];
                    var feed = _this.feedLineage.feedMap[firstItem];
                    if (feed) {
                        _this.selectedNode.name = feed.displayName;
                        _this.selectedNode.type = 'FEED';
                        _this.selectedNode.content = feed;
                    }
                    else {
                        var ds = _this.feedLineage.datasourceMap[firstItem];
                        _this.selectedNode.name = ds.name;
                        _this.selectedNode.type = 'DATASOURCE';
                        _this.selectedNode.content = ds;
                    }
                }
                else {
                    _this.selectedNode = _this.SELECT_A_NODE;
                }
                _this.$scope.$apply();
                //console.log(;self.selectedNode);
                //force angular to refresh selection
                angular.element('#hiddenSelectedNode').html(_this.selectedNode.name);
            };
            this.events = {
                onload: this.onLoad,
                selectNode: this.onSelect,
                stabilized: this.stabilized
                //  stabilizationIterationsDone: stabilizationIterationsDone
            };
            this._draw = function () {
                this.nodes = [];
                this.edges = [];
                this.edgeKeys = {};
                this.processedDatasource = {};
                this.processedNodes = {};
                //turn on physics for layout
                this.options.physics.enabled = true;
                this.buildVisJsGraph(this.feedLineage.feed);
                this.setNodeData();
            };
            this.onDetailedView = function () {
                this.graphMode = this.graphModes.DETAILED;
                this._draw();
            };
            this.onSimpleView = function () {
                this.graphMode = this.graphModes.SIMPLE;
                this._draw();
            };
            this.setNodeStyle = function (node, style) {
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
                        node.font = {};
                    }
                    node.font.background = '#ffffff';
                }
            };
            /**
             *  Builds the datasource Node for the passed in {@code dsId}
             * @param feedLineage
             * @param feed
             * @param dsId
             * @param type
             */
            this.buildDatasource = function (feed, dsId, type) {
                var _this = this;
                if (dsId) {
                    var processedDatasource = this.processedNodes[dsId];
                    if (processedDatasource == undefined) {
                        var ds = this.feedLineage.datasourceMap[dsId];
                        this.assignDatasourceProperties(ds);
                        //console.log('building datasource',ds.name)
                        if (this.isDetailedGraph()) {
                            var node = { id: dsId, label: this.datasourceNodeLabel(ds), title: this.datasourceNodeLabel(ds), group: "datasource" };
                            this.nodes.push(node);
                            this.processedNodes[node.id] = node;
                            var style = this.feedLineage.styles[ds.datasourceType];
                            this.setNodeStyle(node, style);
                        }
                        //build subgraph of feed relationships
                        if (ds.sourceForFeeds) {
                            _.each(ds.sourceForFeeds, function (feedItem) {
                                var depFeed = _this.feedLineage.feedMap[feedItem.id];
                                if (depFeed.id != feed.id) {
                                    var edgeKey = dsId + depFeed.id;
                                    var fromId = dsId;
                                    var arrows = "to";
                                    var label = 'source';
                                    if (!_this.isDetailedGraph()) {
                                        label = '';
                                        edgeKey = feed.id + depFeed.id;
                                        var edgeKey2 = depFeed.id + feed.id;
                                        fromId = feed.id;
                                        var exists = _this.edgeKeys[edgeKey];
                                        var exists2 = _this.edgeKeys[edgeKey2];
                                        if (!exists && exists2) {
                                            exists2.arrows = "to;from";
                                            edgeKey = edgeKey2;
                                        }
                                    }
                                    if (_this.edgeKeys[edgeKey] == undefined) {
                                        var edge = { from: fromId, to: depFeed.id, arrows: 'to', label: label };
                                        _this.edgeKeys[edgeKey] = edge;
                                        _this.edges.push(edge);
                                    }
                                    _this.buildVisJsGraph(depFeed);
                                }
                            });
                        }
                        if (ds.destinationForFeeds) {
                            _.each(ds.destinationForFeeds, function (feedItem) {
                                var depFeed = _this.feedLineage.feedMap[feedItem.id];
                                if (depFeed.id != feed.id) {
                                    var edgeKey = dsId + depFeed.id;
                                    var toId = dsId;
                                    var label = 'destination';
                                    if (!_this.isDetailedGraph()) {
                                        label = '';
                                        edgeKey = depFeed.id + feed.id;
                                        toId = feed.id;
                                        var edgeKey2 = feed.id + depFeed.id;
                                        var exists = _this.edgeKeys[edgeKey];
                                        var exists2 = _this.edgeKeys[edgeKey2];
                                        if (!exists && exists2) {
                                            exists2.arrows = "to;from";
                                            edgeKey = edgeKey2;
                                        }
                                    }
                                    if (_this.edgeKeys[edgeKey] == undefined) {
                                        var edge = { from: depFeed.id, to: toId, arrows: 'to', label: label };
                                        _this.edgeKeys[edgeKey] = edge;
                                        _this.edges.push(edge);
                                    }
                                    _this.buildVisJsGraph(depFeed);
                                }
                            });
                        }
                    }
                    if (this.isDetailedGraph()) {
                        if (type == 'source') {
                            var edgeKey = dsId + feed.id;
                            var fromId = dsId;
                            if (this.edgeKeys[edgeKey] == undefined) {
                                var edge = { from: fromId, to: feed.id, arrows: 'to', label: 'source' };
                                this.edgeKeys[edgeKey] = edge;
                                this.edges.push(edge);
                            }
                        }
                        else {
                            var edgeKey = dsId + feed.id;
                            var toId = dsId;
                            if (this.edgeKeys[edgeKey] == undefined) {
                                var edge = { from: feed.id, to: toId, arrows: 'to', label: 'destination' };
                                this.edgeKeys[edgeKey] = edge;
                                this.edges.push(edge);
                            }
                        }
                    }
                }
            };
            /**
         * Builds the Graph of Datasources as they are related to the incoming {@code feed}
         * @param feed
         */
            this.buildDataSourceGraph = function (feed) {
                var _this = this;
                if (feed.sources) {
                    _.each(feed.sources, function (src) {
                        _this.buildDatasource(feed, src.datasourceId, 'source');
                    });
                }
                if (feed.destinations) {
                    _.each(feed.destinations, function (dest) {
                        _this.buildDatasource(feed, dest.datasourceId, 'destination');
                    });
                }
            };
            /**
             * Assigns some of the fields on the datasource to the properties attribute
             * @param ds
             */
            this.assignDatasourceProperties = function (ds) {
                var keysToOmit = ['@type', 'id', 'name', 'encrypted', 'compressed', 'destinationForFeeds', 'sourceForFeeds'];
                var props = _.omit(ds, keysToOmit);
                props = _.pick(props, function (value, key) {
                    return !_.isObject(value);
                });
                ds.displayProperties = props;
                this.cleanProperties(ds);
                //add in any properties of its own
                angular.extend(ds.displayProperties, ds.properties);
            };
            this.cleanProperties = function (item) {
                if (item.properties) {
                    var updatedProps = _.omit(item.properties, function (val, key) {
                        return key.indexOf("jcr:") == 0 || key == "tba:properties" || key == 'tba:processGroupId';
                    });
                    item.properties = updatedProps;
                }
            };
            /**
             * Get the label for the Datasource node
             * @param ds
             * @returns {string}
             */
            this.datasourceNodeLabel = function (ds) {
                var label = "";
                if (angular.isString(ds.datasourceType)) {
                    label = this.Utils.endsWith(ds.datasourceType.toLowerCase(), "datasource") ? ds.datasourceType.substring(0, ds.datasourceType.toLowerCase().lastIndexOf("datasource")) : ds.datasourceType;
                }
                else if (angular.isString(ds.type)) {
                    label = ds.type;
                }
                else {
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
            this.feedNodeLabel = function (feed) {
                var label = feed.displayName;
                return label;
            };
            /**
             * Create the Feed Node
             * @param feed
             * @returns {{id: *, label: string, title: string, group: string}}
             */
            this.feedNode = function (feed) {
                var node = { id: feed.id, label: this.feedNodeLabel(feed), title: this.feedNodeLabel(feed), group: "feed" };
                var style = this.feedLineage.styles['feed'];
                this.setNodeStyle(node, style);
                if (feed.id == this.model.id) {
                    var style = this.feedLineage.styles['currentFeed'];
                    this.setNodeStyle(node, style);
                }
                this.cleanProperties(feed);
                feed.displayProperties = {};
                //add in any properties of its own
                angular.extend(feed.displayProperties, feed.properties);
                return node;
            };
            /**
             * Build the Lineage Graph
             * @param feed
             */
            this.buildVisJsGraph = function (feed) {
                var _this = this;
                var node = this.processedNodes[feed.id] == undefined ? this.feedNode(feed) : this.processedNodes[feed.id];
                if (this.processedNodes[node.id] == undefined) {
                    //  console.log('building feed',feed.systemName)
                    this.processedNodes[node.id] = node;
                    this.nodes.push(node);
                    this.buildDataSourceGraph(feed);
                    //walk the graph both ways (up and down)
                    if (feed.dependentFeedIds) {
                        _.each(feed.dependentFeedIds, function (depFeedId) {
                            //get it from the map
                            var depFeed = _this.feedLineage.feedMap[depFeedId];
                            if (_this.processedNodes[depFeed.id] == undefined) {
                                _this.buildVisJsGraph(depFeed);
                            }
                            var edgeKey = depFeed.id + feed.id;
                            if (_this.edgeKeys[edgeKey] == undefined) {
                                var edge = { from: depFeed.id, to: feed.id, arrows: 'from', label: 'depends on' };
                                _this.edgeKeys[edgeKey] = edge;
                                _this.edges.push(edge);
                            }
                        });
                    }
                    if (feed.usedByFeedIds) {
                        _.each(feed.usedByFeedIds, function (usedByFeedId) {
                            //get it from the map
                            var usedByFeed = _this.feedLineage.feedMap[_this.usedByFeedId];
                            if (_this.processedNodes[usedByFeed.id] == undefined) {
                                _this.buildVisJsGraph(usedByFeed);
                            }
                            var edgeKey = feed.id + usedByFeed.id;
                            if (_this.edgeKeys[edgeKey] == undefined) {
                                // edgeKeys[edgeKey] = edgeKey;
                                //taken care of from the other side
                                //   edges.push({from: feed.id, to: usedByFeed.id});
                            }
                        });
                    }
                }
            };
            /**
             * Get the Feed Lineage Graph and build the network
             * @param feedId
             */
            this.getFeedLineage = function (feedId) {
                var _this = this;
                this.loading = true;
                this.$http.get(this.RestUrlService.FEED_LINEAGE_URL(feedId)).then(function (response) {
                    _this.feedLineage = response.data;
                    _this.loading = false;
                    _this.redraw();
                });
            };
            /**
             * attach the nodes to the data to the graph
             */
            this.setNodeData = function () {
                var visNodes = new this.VisDataSet(this.nodes);
                var visEdges = new this.VisDataSet(this.edges);
                this.data = { nodes: visNodes, edges: visEdges };
            };
            /**
             * Called when a Node is selected
             * @param item
             */
            this.changed = false;
            this.navigateToFeed = function () {
                if (this.selectedNode.type == 'FEED' && this.selectedNode.content) {
                    this.StateService.FeedManager().Feed().navigateToFeedDetails(this.selectedNode.content.id, 2);
                }
            };
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
                    font: { align: 'horizontal' }
                },
                layout: {
                    hierarchical: {
                        direction: "LR",
                        nodeSpacing: 200,
                        sortMethod: 'directed'
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
        return FeedLineageController;
    }());
    exports.FeedLineageController = FeedLineageController;
    // define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {
    angular.module(moduleName).controller('FeedLineageController', ["$scope", "$element", "$http", "$mdDialog", "$timeout", "AccessControlService", "FeedService", "RestUrlService", "VisDataSet", "Utils", "BroadcastService", "StateService", FeedLineageController]);
    angular.module(moduleName)
        .directive('thinkbigFeedLineage', directive);
});
//# sourceMappingURL=feed-lineage.js.map