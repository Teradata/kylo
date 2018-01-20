import * as angular from "angular";
import * as mocks from "angular-mocks";
import * as _ from "underscore";

import "feed-mgr/visual-query/module";
import "feed-mgr/visual-query/module-require";
import {QueryEngineFactory} from "../wrangler/query-engine-factory.service";

const moduleName: string = require("feed-mgr/visual-query/module-name");

describe("Component: BuildQueryComponent", function () {
    // Include dependencies
    beforeEach((mocks as angular.IMockStatic).module("kylo", "kylo.feedmgr", moduleName));

    // Setup tests
    beforeEach((mocks as angular.IMockStatic).inject(function ($injector: angular.auto.IInjectorService) {
        const $scope = $injector.get("$rootScope").$new(false);
        const engine = ($injector.get("VisualQueryEngineFactory") as QueryEngineFactory).getEngine("spark");
        this.controller = ($injector.get("$componentController") as angular.IComponentControllerService)("thinkbigVisualQueryBuilder", {$scope: $scope}, {engine: engine, model: {}});
        this.controller.setupFlowChartModel();

        this.$http = $injector.get("$httpBackend");
    }));

    /** List of selected tickit.date columns */
    const DATE_COLUMNS = [{column: "caldate", alias: "tbl14", tableName: "tickit.date", tableColumn: "caldate", dataType: "date"}];

    /** A node for the tickit.date table */
    const DATE_NODE = {
        id: 14,
        name: "tickit.date",
        nodeAttributes: {
            attributes: [
                {name: "dateid", dataType: "smallint", selected: false},
                {name: "caldate", dataType: "date", selected: true}
            ],
            sql: "`tickit`.`date`"
        },
        connectors: {bottom: {}, left: {}, right: {}, top: {}},
        inputConnectors: [{name: ""}],
        outputConnectors: [{name: ""}]
    };

    /** List of selected tickit.event columns */
    const EVENT_COLUMNS = [{column: "eventname", alias: "tbl12", tableName: "tickit.event", tableColumn: "eventname", dataType: "string"}];

    /** A node for the tickit.event table */
    const EVENT_NODE = {
        id: 12,
        name: "tickit.event",
        nodeAttributes: {
            attributes: [
                {name: "eventid", dataType: "int", selected: false},
                {name: "dateid", dataType: "smallint", selected: false},
                {name: "eventname", dataType: "string", selected: true}
            ],
            sql: "`tickit`.`event`"
        },
        connectors: {bottom: {}, left: {}, right: {}, top: {}},
        inputConnectors: [{name: ""}],
        outputConnectors: [{name: ""}]
    };

    /** List of selected tickit.sales columns */
    const SALES_COLUMNS = [
        {column: "qtysold", alias: "tbl11", tableName: "tickit.sales", tableColumn: "qtysold", dataType: "string"},
        {column: "pricepaid", alias: "tbl11", tableName: "tickit.sales", tableColumn: "pricepaid", dataType: "double"},
        {column: "commission", alias: "tbl11", tableName: "tickit.sales", tableColumn: "commission", dataType: "double"}
    ];

    /** A node for the tickit.sales table */
    const SALES_NODE = {
        id: 11,
        name: "tickit.sales",
        nodeAttributes: {
            attributes: [
                {name: "salesid", dataType: "int", selected: false},
                {name: "buyerid", dataType: "int", selected: false},
                {name: "eventid", dataType: "int", selected: false},
                {name: "dateid", dataType: "smallint", selected: false},
                {name: "qtysold", dataType: "string", selected: true},
                {name: "pricepaid", dataType: "double", selected: true},
                {name: "commission", dataType: "double", selected: true}
            ],
            sql: "`tickit`.`sales`"
        },
        connectors: {bottom: {}, left: {}, right: {}, top: {}},
        inputConnectors: [{name: ""}],
        outputConnectors: [{name: ""}]
    };

    /** List of selected tickit.users columns */
    const USERS_COLUMNS = [
        {column: "username", alias: "tbl10", tableName: "tickit.users", tableColumn: "username", dataType: "string"},
        {column: "firstname", alias: "tbl10", tableName: "tickit.users", tableColumn: "firstname", dataType: "string"},
        {column: "lastname", alias: "tbl10", tableName: "tickit.users", tableColumn: "lastname", dataType: "string"}
    ];

    /** A node for the tickit.users table */
    const USERS_NODE = {
        id: 10,
        name: "tickit.users",
        nodeAttributes: {
            attributes: [
                {name: "userid", dataType: "int", selected: false},
                {name: "username", dataType: "string", selected: true},
                {name: "firstname", dataType: "string", selected: true},
                {name: "lastname", dataType: "string", selected: true}
            ],
            sql: "`tickit`.`users`"
        },
        connectors: {bottom: {}, left: {}, right: {}, top: {}},
        inputConnectors: [{name: ""}],
        outputConnectors: [{name: ""}]
    };

    /** List of selected tickit.venue columns */
    const VENUE_COLUMNS = [
        {column: "venuename", alias: "tbl13", tableName: "tickit.venue", tableColumn: "venuename", dataType: "string"}
    ];

    /** A node for the tickit.venue table */
    const VENUE_NODE = {
        id: 13,
        name: "tickit.venue",
        nodeAttributes: {
            attributes: [
                {name: "venueid", dataType: "int", selected: false},
                {name: "venuename", dataType: "string", selected: true}
            ],
            sql: "`tickit`.`venue`"
        },
        connectors: {bottom: {}, left: {}, right: {}, top: {}},
        inputConnectors: [{name: ""}],
        outputConnectors: [{name: ""}]
    };

    /**
     * Connects the specified tables.
     *
     * <p>Note that the source and destination are switched by the
     * {@link flowchart.ChartViewModel#createNewConnection()} call.</p>
     *
     * @param {flowchart.ChartViewModel} chartViewModel the flow chart view model
     * @param {number} srcNodeId the source node id
     * @param {string|null} srcJoinKey the source join column, or null if not defined
     * @param {number} dstNodeId the destination node id
     * @param {string|null} dstJoinKey the destination join column, or null if not defined
     */
    function connectTables(chartViewModel: any, srcNodeId: number, srcJoinKey: string | null, dstNodeId: number, dstJoinKey: string | null) {
        // Add connection
        const dstConnector = chartViewModel.findConnector(dstNodeId, 0);
        const srcConnector = chartViewModel.findConnector(srcNodeId, 0);

        chartViewModel.createNewConnection(srcConnector, dstConnector);

        // Set join info
        const connection = chartViewModel.connections[chartViewModel.connections.length - 1];
        connection.data.joinKeys = {};

        if (srcJoinKey !== null) {
            connection.data.joinKeys.sourceKey = srcJoinKey;
        }
        if (dstJoinKey !== null) {
            connection.data.joinKeys.destKey = dstJoinKey;
        }

        if (srcJoinKey !== null || dstJoinKey !== null) {
            connection.data.joinType = "INNER JOIN";
        }
    }

    // getSQLModel
    it("should produce SQL for one table", function () {
        this.$http.whenGET("js/feeds/feeds-table.html").respond(200, "");

        // Test SQL
        this.controller.chartViewModel.addNode(SALES_NODE);

        let expected = "SELECT tbl11.`qtysold`, tbl11.`pricepaid`, tbl11.`commission` FROM `tickit`.`sales` tbl11";
        expect(this.controller.getSQLModel()).toBe(expected);

        // Test selected columns
        expect(this.controller.selectedColumnsAndTables).toEqual(SALES_COLUMNS);
    });

    it("should produce SQL for joined tables", function () {
        this.$http.whenGET("js/feeds/feeds-table.html").respond(200, "");
        this.$http.whenGET("js/visual-query/visual-query-builder-connection-dialog.html").respond(200, "");

        // Add tables
        const chartViewModel = this.controller.chartViewModel;

        chartViewModel.addNode(USERS_NODE);
        chartViewModel.addNode(SALES_NODE);
        chartViewModel.addNode(EVENT_NODE);
        chartViewModel.addNode(VENUE_NODE);

        connectTables(chartViewModel, 10, "userid", 11, "buyerid");
        connectTables(chartViewModel, 11, "eventid", 12, "eventid");
        connectTables(chartViewModel, 12, "venueid", 13, "venueid");

        // Test SQL
        let expected: any = "SELECT tbl10.`username`, tbl10.`firstname`, tbl10.`lastname`, tbl11.`qtysold`, tbl11.`pricepaid`, tbl11.`commission`, tbl12.`eventname`, tbl13.`venuename` "
            + "FROM `tickit`.`users` tbl10 INNER JOIN `tickit`.`sales` tbl11 ON tbl11.`buyerid` = tbl10.`userid` INNER JOIN `tickit`.`event` tbl12 ON tbl12.`eventid` = tbl11.`eventid` "
            + "INNER JOIN `tickit`.`venue` tbl13 ON tbl13.`venueid` = tbl12.`venueid`";
        expect(this.controller.getSQLModel()).toBe(expected);

        // Test selected columns
        expected = _.flatten([USERS_COLUMNS, SALES_COLUMNS, EVENT_COLUMNS, VENUE_COLUMNS], true);
        expect(this.controller.selectedColumnsAndTables).toEqual(expected);
    });

    it("should produce SQL for multiple tables", function () {
        this.$http.whenGET("js/feeds/feeds-table.html").respond(200, "");

        // Add tables
        this.controller.chartViewModel.addNode({
            id: 10,
            name: "sample.t1",
            nodeAttributes: {
                attributes: [
                    {name: "id", dataType: "smallint", selected: true},
                    {name: "id_1", dataType: "smallint", selected: true}  // collision with sample.t1.id -> t1_id -> sample_t1_id -> id_1
                ],
                sql: "`sample`.`t1`"
            },
            connectors: {bottom: {}, left: {}, right: {}, top: {}},
            inputConnectors: [{name: ""}],
            outputConnectors: [{name: ""}]
        });
        this.controller.chartViewModel.addNode({
            id: 11,
            name: "sample.t2",
            nodeAttributes: {
                attributes: [
                    {name: "id", dataType: "smallint", selected: true},  // collision with sample.t1.id
                    {name: "t1_id", dataType: "smallint", selected: true},  // collision with sample.t1.id -> t1_id
                    {name: "sample_t1_id", dataType: "smallint", selected: true}  // collision with sample.t1.id -> t1_id -> sample_t1_id
                ],
                sql: "`sample`.`t2`"
            },
            connectors: {bottom: {}, left: {}, right: {}, top: {}},
            inputConnectors: [{name: ""}],
            outputConnectors: [{name: ""}]
        });

        // Test SQL
        let expected = "SELECT tbl10.`id` AS `id_2`, tbl10.`id_1`, tbl11.`id` AS `t2_id`, tbl11.`t1_id` AS `t2_t1_id`, tbl11.`sample_t1_id` AS `t2_sample_t1_id` FROM `sample`.`t1` tbl10, "
            + "`sample`.`t2` tbl11";
        expect(this.controller.getSQLModel()).toBe(expected);
    });

    it("should produce SQL for pre-joined tables", function () {
        this.$http.whenGET("js/feeds/feeds-table.html").respond(200, "");

        // Add tables
        this.controller.chartViewModel.addNode(USERS_NODE);
        this.controller.chartViewModel.addNode(SALES_NODE);

        connectTables(this.controller.chartViewModel, 10, null, 11, null);

        // Test SQL
        let expected: any = "SELECT tbl10.`username`, tbl10.`firstname`, tbl10.`lastname`, tbl11.`qtysold`, tbl11.`pricepaid`, tbl11.`commission`" +
            " FROM `tickit`.`users` tbl10 JOIN `tickit`.`sales` tbl11";
        expect(this.controller.getSQLModel()).toBe(expected);

        // Test selected columns
        expected = _.flatten([USERS_COLUMNS, SALES_COLUMNS], true);
        expect(this.controller.selectedColumnsAndTables).toEqual(expected);
    });

    it("should produce SQL for multiple join conditions", function () {
        this.$http.whenGET("js/feeds/feeds-table.html").respond(200, "");
        this.$http.whenGET("js/visual-query/visual-query-builder-connection-dialog.html").respond(200, "");

        // Add tables
        const chartViewModel = this.controller.chartViewModel;

        chartViewModel.addNode(SALES_NODE);
        chartViewModel.addNode(EVENT_NODE);
        chartViewModel.addNode(DATE_NODE);

        connectTables(chartViewModel, 11, "eventid", 12, "eventid");
        connectTables(chartViewModel, 12, "dateid", 14, "dateid");
        connectTables(chartViewModel, 11, "dateid", 14, "dateid");

        // Test SQL
        let expected: any = "SELECT tbl11.`qtysold`, tbl11.`pricepaid`, tbl11.`commission`, tbl12.`eventname`, tbl14.`caldate` FROM `tickit`.`sales` tbl11 INNER JOIN `tickit`.`event` tbl12 ON "
            + "tbl12.`eventid` = tbl11.`eventid` INNER JOIN `tickit`.`date` tbl14 ON tbl14.`dateid` = tbl11.`dateid` AND tbl14.`dateid` = tbl12.`dateid`";
        expect(this.controller.getSQLModel().replace(/INNER/g, "\nINNER")).toBe(expected.replace(/INNER/g, "\nINNER"));

        // Test selected columns
        expected = _.flatten([SALES_COLUMNS, EVENT_COLUMNS, DATE_COLUMNS], true);
        expect(this.controller.selectedColumnsAndTables).toEqual(expected);
    });
});
