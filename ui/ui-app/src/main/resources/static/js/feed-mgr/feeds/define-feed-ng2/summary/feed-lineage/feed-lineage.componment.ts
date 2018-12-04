import {AfterViewInit, Component, Injector, Input, OnInit, ViewChild} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import * as _ from "underscore";
import * as angular from 'angular';
import {HttpClient} from "@angular/common/http";
import {LINEAGE_LINK} from "../../model/feed-link-constants";
import {KyloIcons} from "../../../../../kylo-utils/kylo-icons";
import {KyloVisNetworkComponent} from "../../../../../common/kylo-vis-network/kylo-vis-network.component";
import {CloneUtil} from "../../../../../common/utils/clone-util";

@Component({
    selector: "feed-lineage",
    styleUrls: ["./feed-lineage.component.css"],
    templateUrl: "./feed-lineage.component.html"
})
export class FeedLineageComponment extends AbstractLoadFeedComponent implements OnInit, AfterViewInit {

    static LOADER = "FeedLineage.LOADER";

    static LINK_NAME = LINEAGE_LINK;

    restUrlService: any;
    utils: any;
    kyloStateService: any;

    public kyloIcons_Links_lineage = KyloIcons.Links.lineage;




    feedLineage: any = null;
    nodes: any[];
    edges: any[];
    edgeKeys: any = {};
    processedNodes: any = {};
    loading: boolean = true;
    options: any = null;
    /**
     *
     * The default text of the node selector
     * @type {{name: string, type: string, content: {displayProperties: null}}}
     */
    SELECT_A_NODE: any = {name: 'Select a node', type: 'Feed Lineage', content: {displayProperties: null}};

    /**
     * The selected Node
     * @type {{name: string, type: string, content: null}}
     */
    selectedNode: any = this.SELECT_A_NODE;

    graphModes: any = {SIMPLE: "SIMPLE", DETAILED: "DETAILED"};

    graphMode: any = this.graphModes.DETAILED;

    data: any = {nodes: null, edges: null};

    @ViewChild("lineageGraph")
    lineageGraph:KyloVisNetworkComponent


    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService: FeedSideNavService,
                private $$angularInjector: Injector, private http: HttpClient) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
        this.utils = $$angularInjector.get("Utils");
        this.restUrlService = $$angularInjector.get("RestUrlService");
        this.kyloStateService = $$angularInjector.get("StateService");

        this.options = {
            "height": "100%",
            "width": "100%",
            "edges": {
                "arrowStrikethrough": false,
                "smooth": {
                    "enabled": true,
                    "type": "cubicBezier",
                    "roundness": 0,
                    "forceDirection": "horizontal"
                },
                "font": {
                    "align": "horizontal"
                }
            },
            "layout": {
                "randomSeed":50,
                "hierarchical": {
                    "direction": "LR",
                    "nodeSpacing": 300,
                    "sortMethod": "directed"
                }
            },
            "nodes": {
                "shape": "box",
                "font": {
                    "align": "center"
                }
            },
            "groups": {
                "feed": {
                    "shape": "box",
                    "font": {
                        "align": "center"
                    }
                },
                "datasource": {
                    "shape": "box",
                    "font": {
                        "align": "center"
                    }
                }
            },
            "interaction": {
                "hover": true,
                "navigationButtons": true,
                "keyboard": true
            },
            "physics": {
                "enabled": false,
                "solver": "hierarchicalRepulsion"
            }
        }
    }

    getLinkName() {
        return FeedLineageComponment.LINK_NAME;
    }

    navigateToFeed() {
        if (this.selectedNode.type == 'FEED' && this.selectedNode.content) {
            this.kyloStateService.FeedManager().Feed().navigateToFeedDetails(this.selectedNode.content.id, 2);
        }
    }

    onHeightChange(height:number){
        if(this.lineageGraph){
            this.lineageGraph.setHeight(height);
        }
    }

    /**
     * Called when a Node is selected
     * @param item
     */
    changed = false;
    panelOpenState = false;

    onLoad(event:any) {

    }

    onSelect(item: any) {

        if (item && item.nodes && item.nodes[0]) {
            this.changed = true;
            var firstItem = item.nodes[0];
            var feed = this.feedLineage.feedMap[firstItem];
            if (feed) {
                this.selectedNode.name = feed.displayName;
                this.selectedNode.type = 'FEED';
                this.selectedNode.content = feed;
                this.cleanProperties(feed);
                feed.displayProperties = {};
                //add in any properties of its own
                angular.extend(feed.displayProperties, feed.properties);
                this.selectedNode.content.displayProperties = feed.displayProperties
            }
            else {
                var ds = this.feedLineage.datasourceMap[firstItem];
                this.selectedNode.name = ds.name;
                this.selectedNode.type = 'DATASOURCE';
                this.selectedNode.content = ds;
            }

        }
        else {
            this.selectedNode = this.SELECT_A_NODE;
        }
    }

    onStabilized(event:any){

        let optionsUpdate = {
            physics: {enabled: false, stabilization: false,  "solver": "hierarchicalRepulsion"},
            interaction: {dragNodes: true},
            layout: {
                hierarchical: {
                    enabled: false
                }
            }
        }
        let copy = CloneUtil.deepCopy(this.options);

        let newOptions = _.extend(copy,optionsUpdate)
        if (this.lineageGraph) {
            this.lineageGraph.updateOptions(newOptions);
        }

    }

    ngAfterViewInit(){
        this._draw();
    }

    init() {
        this.getFeedLineage();
    }

    getFeedLineage() {
        this.http.get(this.restUrlService.FEED_LINEAGE_URL(this.feedId)).subscribe((response: any) => {
            this.feedLineage = response;
            this._draw();
            this.loading = false;
        });
    }

    onDrop(data: any) {
        console.log('dropped', data);
    }

    networkView(value: string) {
        this.graphMode = value;
        if (!this.loading) {
            this.redraw();
        }
        //this.options = {physics: {enabled: false, stabilization: true}};
    }

    redraw() {

        if (this.isDetailedGraph()) {
            this.onDetailedView();
        }
        else {
            this.onSimpleView();
        }
    }

    isDetailedGraph() {
        return this.graphMode == this.graphModes.DETAILED;
    }

    onDetailedView() {
        this.graphMode = this.graphModes.DETAILED;
        this._draw();

    }

    onSimpleView() {
        this.graphMode = this.graphModes.SIMPLE;
        this._draw();
    }

    _draw() {
        this.nodes = [];
        this.edges = [];
        this.edgeKeys = {};
        this.processedNodes = {};

        if(this.feedLineage && this.lineageGraph) {
            //turn on physics for layout
            this.options.physics.enabled = true;
            this.options.physics.solver = "hierarchicalRepulsion";
            this.options.physics.stabilization = true
            //turn on physics for layout
            this.buildVisJsGraph(this.feedLineage.feed);
            this.setNodeData();
            this.lineageGraph.drawNetwork();
        }
    }

    setNodeData() {
        this.data = {nodes: this.nodes, edges: this.edges};
    }

    buildVisJsGraph(feed: any) {

        var node = this.processedNodes[feed.id] == undefined ? this.feedNode(feed) : this.processedNodes[feed.id];
        if (this.processedNodes[node.id] == undefined) {
            this.processedNodes[node.id] = node;
            this.nodes.push(node);
            this.buildDataSourceGraph(feed);

            //walk the graph both ways (up and down)
            if (feed.dependentFeedIds) {
                _.each(feed.dependentFeedIds, (depFeedId: any) => {
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
                _.each(feed.usedByFeedIds, (usedByFeedId: string) => {
                    //get it from the map
                    var usedByFeed = this.feedLineage.feedMap[usedByFeedId];
                    if (this.processedNodes[usedByFeed.id] == undefined) {
                        this.buildVisJsGraph(usedByFeed);
                    }
                    var edgeKey = feed.id + usedByFeed.id;

                });
            }
        }
    }

    feedNode(feed: any) {

        var node = {id: feed.id, label: this.feedNodeLabel(feed), title: this.feedNodeLabel(feed), group: "feed"};
        var style = this.feedLineage.styles['feed'];
        this.setNodeStyle(node, style);
        if (feed.id == this.feedId) {
            var style = this.feedLineage.styles['currentFeed'];
            this.setNodeStyle(node, style);
        }
        this.cleanProperties(feed);
        feed.displayProperties = {};
        //add in any properties of its own
        angular.extend(feed.displayProperties, feed.properties);

        return node;
    }

    feedNodeLabel(feed: any) {
        var label = feed.displayName;
        return label;
    }

    buildDataSourceGraph(feed: any) {
        if (feed.sources) {
            _.each(feed.sources, (src: any) => {
                this.buildDatasource(feed, src.datasourceId, 'source');
            });
        }
        if (feed.destinations) {
            _.each(feed.destinations, (dest: any) => {
                this.buildDatasource(feed, dest.datasourceId, 'destination');
            })
        }

    }

    assignDatasourceProperties(ds: any) {
        var keysToOmit = ['@type', 'id', 'name', 'encrypted', 'compressed', 'destinationForFeeds', 'sourceForFeeds'];
        var props = _.omit(ds, keysToOmit);
        props = _.pick(props, (value: any, key: any) => {
            return !_.isObject(value);
        });
        ds.displayProperties = props
        this.cleanProperties(ds);
        //add in any properties of its own
        angular.extend(ds.displayProperties, ds.properties);

    }

    cleanProperties(item: any) {
        if (item.properties) {
            var updatedProps = _.omit(item.properties, (val: any, key: any) => {
                return key.indexOf("jcr:") == 0 || key == "tba:properties" || key == 'tba:processGroupId';
            });
            item.properties = updatedProps
        }
    }

    datasourceNodeLabel(ds: any) {
        var label = "";
        if (angular.isString(ds.datasourceType)) {
            label = this.utils.endsWith(ds.datasourceType.toLowerCase(), "datasource") ? ds.datasourceType.substring(0, ds.datasourceType.toLowerCase().lastIndexOf("datasource")) : ds.datasourceType;
            if(label && label.toLowerCase() == 'database'){
                //attempt to find the name of the database in the properties
                if(ds.properties && ds.properties['Database Connection']){
                    label = ds.properties['Database Connection'];
                }
            }
        } else if (angular.isString(ds.type)) {
            label = ds.type;
        } else {
            label = this.utils.endsWith(ds["@type"].toLowerCase(), "datasource") ? ds["@type"].substring(0, ds["@type"].toLowerCase().lastIndexOf("datasource")) : ds["@type"];
        }

        label += "\n" + ds.name;
        return label;
    }

    buildDatasource(feed: any, dsId: any, type: any) {
        if (dsId) {
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
                    _.each(ds.sourceForFeeds, (feedItem: any) => {
                        var depFeed = this.feedLineage.feedMap[feedItem.id];
                        if (depFeed.id != feed.id) {
                            var edgeKey = dsId + depFeed.id;
                            var fromId = dsId;
                            var arrows = "to";
                            var label = 'source'
                            if (!this.isDetailedGraph()) {
                                label = '';
                                edgeKey = feed.id + depFeed.id;
                                var edgeKey2 = depFeed.id + feed.id;
                                fromId = feed.id;
                                var exists = this.edgeKeys[edgeKey];
                                var exists2 = this.edgeKeys[edgeKey2];
                                if (!exists && exists2) {
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
                    _.each(ds.destinationForFeeds, (feedItem: any) => {
                        var depFeed = this.feedLineage.feedMap[feedItem.id];
                        if (depFeed.id != feed.id) {
                            var edgeKey = dsId + depFeed.id;
                            var toId = dsId;
                            var label = 'destination'
                            if (!this.isDetailedGraph()) {
                                label = ''
                                edgeKey = depFeed.id + feed.id;
                                toId = feed.id;

                                var edgeKey2 = feed.id + depFeed.id;
                                var exists = this.edgeKeys[edgeKey];
                                var exists2 = this.edgeKeys[edgeKey2];
                                if (!exists && exists2) {
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

    setNodeStyle(node: any, style: any) {
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
}

