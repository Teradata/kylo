define(["require", "exports", "./spark-query-parser", "feed-mgr/visual-query/module", "feed-mgr/visual-query/module-require"], function (require, exports, spark_query_parser_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var mocks = require("angular-mocks");
    var moduleName = require("feed-mgr/visual-query/module-name");
    describe("Class: SparkQueryParser", function () {
        // Include dependencies
        beforeEach(mocks.module("kylo", "kylo.feedmgr", moduleName));
        // toScript
        it("should produce Spark for one table", mocks.inject(function (VisualQueryService) {
            var spark = new spark_query_parser_1.SparkQueryParser(VisualQueryService).toScript({
                "nodes": [{
                        "id": 11,
                        "name": "tickit.sales",
                        "nodeAttributes": {
                            "attributes": [{ "name": "salesid", "dataType": "int", "selected": false, "description": null }, { "name": "buyerid", "dataType": "int", "selected": false, "description": null },
                                { "name": "eventid", "dataType": "int", "selected": false, "description": null }, { "name": "dateid", "dataType": "smallint", "selected": false, "description": null },
                                { "name": "qtysold", "dataType": "string", "selected": true, "description": "number of tickets" },
                                { "name": "pricepaid", "dataType": "double", "selected": true, "description": null }, { "name": "commission", "dataType": "double", "selected": true, "description": null }],
                            "sql": "`tickit`.`sales`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 4 }, "left": { "location": "LEFT", "id": 1 }, "right": { "location": "RIGHT", "id": 2 }, "top": { "location": "TOP", "id": 3 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }], "connections": []
            }, []);
            expect(spark).toBe("val tbl11 = sqlContext.table(\"tickit.sales\").alias(\"tbl11\")\n"
                + "var df = tbl11.select(tbl11.col(\"qtysold\").as(\"qtysold\", new org.apache.spark.sql.types.MetadataBuilder().putString(\"comment\", \"number of tickets\").build()),"
                + " tbl11.col(\"pricepaid\"), tbl11.col(\"commission\"))\n");
        }));
        it("should produce Spark for joined tables", mocks.inject(function (VisualQueryService) {
            var spark = new spark_query_parser_1.SparkQueryParser(VisualQueryService).toScript({
                "nodes": [{
                        "id": 10,
                        "name": "tickit.users",
                        "nodeAttributes": {
                            "attributes": [{ "name": "userid", "dataType": "int", "selected": false, "description": null }, { "name": "username", "dataType": "string", "selected": true, "description": null },
                                { "name": "firstname", "dataType": "string", "selected": true, "description": null }, { "name": "lastname", "dataType": "string", "selected": true, "description": null }],
                            "sql": "`tickit`.`users`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 8 }, "left": { "location": "LEFT", "id": 5 }, "right": { "location": "RIGHT", "id": 6 }, "top": { "location": "TOP", "id": 7 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 11,
                        "name": "tickit.sales",
                        "nodeAttributes": {
                            "attributes": [{ "name": "salesid", "dataType": "int", "selected": false, "description": null }, { "name": "buyerid", "dataType": "int", "selected": false, "description": null },
                                { "name": "eventid", "dataType": "int", "selected": false, "description": null }, { "name": "dateid", "dataType": "smallint", "selected": false, "description": null },
                                { "name": "qtysold", "dataType": "string", "selected": true, "description": null }, { "name": "pricepaid", "dataType": "double", "selected": true, "description": null },
                                { "name": "commission", "dataType": "double", "selected": true, "description": null }],
                            "sql": "`tickit`.`sales`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 12 }, "left": { "location": "LEFT", "id": 9 }, "right": { "location": "RIGHT", "id": 10 }, "top": { "location": "TOP", "id": 11 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 12,
                        "name": "tickit.event",
                        "nodeAttributes": {
                            "attributes": [{ "name": "eventid", "dataType": "int", "selected": false, "description": null },
                                { "name": "dateid", "dataType": "smallint", "selected": false, "description": null }, { "name": "eventname", "dataType": "string", "selected": true, "description": null }],
                            "sql": "`tickit`.`event`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 16 }, "left": { "location": "LEFT", "id": 13 }, "right": { "location": "RIGHT", "id": 14 }, "top": { "location": "TOP", "id": 15 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 13,
                        "name": "tickit.venue",
                        "nodeAttributes": {
                            "attributes": [{ "name": "venueid", "dataType": "int", "selected": false, "description": null },
                                { "name": "venuename", "dataType": "string", "selected": true, "description": null }],
                            "sql": "`tickit`.`venue`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 20 }, "left": { "location": "LEFT", "id": 17 }, "right": { "location": "RIGHT", "id": 18 }, "top": { "location": "TOP", "id": 19 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }],
                "connections": [{
                        "source": { "nodeID": 11, "connectorIndex": 0, "connectorId": 9 },
                        "dest": { "nodeID": 10, "connectorIndex": 0, "connectorID": 5 },
                        "joinKeys": { "sourceKey": "userid", "destKey": "buyerid" },
                        "joinType": "INNER JOIN"
                    }, {
                        "source": { "nodeID": 12, "connectorIndex": 0, "connectorId": 13 },
                        "dest": { "nodeID": 11, "connectorIndex": 0, "connectorID": 9 },
                        "joinKeys": { "sourceKey": "eventid", "destKey": "eventid" },
                        "joinType": "INNER JOIN"
                    }, {
                        "source": { "nodeID": 13, "connectorIndex": 0, "connectorId": 17 },
                        "dest": { "nodeID": 12, "connectorIndex": 0, "connectorID": 13 },
                        "joinKeys": { "sourceKey": "venueid", "destKey": "venueid" },
                        "joinType": "INNER JOIN"
                    }]
            }, []);
            expect(spark).toBe("val tbl10 = sqlContext.table(\"tickit.users\").alias(\"tbl10\")\n"
                + "val tbl11 = sqlContext.table(\"tickit.sales\").alias(\"tbl11\")\n"
                + "val tbl12 = sqlContext.table(\"tickit.event\").alias(\"tbl12\")\n"
                + "val tbl13 = sqlContext.table(\"tickit.venue\").alias(\"tbl13\")\n"
                + "var df = tbl10.join(tbl11, tbl11.col(\"buyerid\").equalTo(tbl10.col(\"userid\")), \"inner\")"
                + ".join(tbl12, tbl12.col(\"eventid\").equalTo(tbl11.col(\"eventid\")), \"inner\")"
                + ".join(tbl13, tbl13.col(\"venueid\").equalTo(tbl12.col(\"venueid\")), \"inner\")"
                + ".select(tbl10.col(\"username\"), tbl10.col(\"firstname\"), tbl10.col(\"lastname\"), tbl11.col(\"qtysold\"), tbl11.col(\"pricepaid\"), tbl11.col(\"commission\"),"
                + " tbl12.col(\"eventname\"), tbl13.col(\"venuename\"))\n");
        }));
        it("should produce Spark for multiple tables", mocks.inject(function (VisualQueryService) {
            var spark = new spark_query_parser_1.SparkQueryParser(VisualQueryService).toScript({
                "nodes": [{
                        "id": 10,
                        "name": "sample.t1",
                        "nodeAttributes": {
                            "attributes": [{ "name": "id", "dataType": "smallint", "selected": true, "description": null }, { "name": "id_1", "dataType": "smallint", "selected": true, "description": null }],
                            "sql": "`sample`.`t1`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 24 }, "left": { "location": "LEFT", "id": 21 }, "right": { "location": "RIGHT", "id": 22 }, "top": { "location": "TOP", "id": 23 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 11,
                        "name": "sample.t2",
                        "nodeAttributes": {
                            "attributes": [{ "name": "id", "dataType": "smallint", "selected": true, "description": null }, { "name": "t1_id", "dataType": "smallint", "selected": true, "description": null },
                                { "name": "sample_t1_id", "dataType": "smallint", "selected": true, "description": null }],
                            "sql": "`sample`.`t2`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 28 }, "left": { "location": "LEFT", "id": 25 }, "right": { "location": "RIGHT", "id": 26 }, "top": { "location": "TOP", "id": 27 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }],
                "connections": []
            }, []);
            expect(spark).toBe("val tbl10 = sqlContext.table(\"sample.t1\").alias(\"tbl10\")\n"
                + "val tbl11 = sqlContext.table(\"sample.t2\").alias(\"tbl11\")\n"
                + "var df = tbl10.join(tbl11).select(tbl10.col(\"id\").as(\"id_2\"), tbl10.col(\"id_1\"), tbl11.col(\"id\").as(\"t2_id\"), tbl11.col(\"t1_id\").as(\"t2_t1_id\"),"
                + " tbl11.col(\"sample_t1_id\").as(\"t2_sample_t1_id\"))\n");
        }));
        it("should produce Spark for pre-joined tables", mocks.inject(function (VisualQueryService) {
            var spark = new spark_query_parser_1.SparkQueryParser(VisualQueryService).toScript({
                "nodes": [{
                        "id": 10,
                        "name": "tickit.users",
                        "nodeAttributes": {
                            "attributes": [{ "name": "userid", "dataType": "int", "selected": false, "description": null }, { "name": "username", "dataType": "string", "selected": true, "description": null },
                                { "name": "firstname", "dataType": "string", "selected": true, "description": null }, { "name": "lastname", "dataType": "string", "selected": true, "description": null }],
                            "sql": "`tickit`.`users`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 32 }, "left": { "location": "LEFT", "id": 29 }, "right": { "location": "RIGHT", "id": 30 }, "top": { "location": "TOP", "id": 31 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 11,
                        "name": "tickit.sales",
                        "nodeAttributes": {
                            "attributes": [{ "name": "salesid", "dataType": "int", "selected": false, "description": null }, { "name": "buyerid", "dataType": "int", "selected": false, "description": null },
                                { "name": "eventid", "dataType": "int", "selected": false, "description": null }, { "name": "dateid", "dataType": "smallint", "selected": false, "description": null },
                                { "name": "qtysold", "dataType": "string", "selected": true, "description": null }, { "name": "pricepaid", "dataType": "double", "selected": true, "description": null },
                                { "name": "commission", "dataType": "double", "selected": true, "description": null }], "sql": "`tickit`.`sales`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 36 }, "left": { "location": "LEFT", "id": 33 }, "right": { "location": "RIGHT", "id": 34 }, "top": { "location": "TOP", "id": 35 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }], "connections": [{ "source": { "nodeID": 11, "connectorIndex": 0, "connectorId": 33 }, "dest": { "nodeID": 10, "connectorIndex": 0, "connectorID": 29 }, "joinKeys": {} }]
            }, []);
            expect(spark).toBe("val tbl10 = sqlContext.table(\"tickit.users\").alias(\"tbl10\")\n"
                + "val tbl11 = sqlContext.table(\"tickit.sales\").alias(\"tbl11\")\n"
                + "var df = tbl10.join(tbl11).select(tbl10.col(\"username\"), tbl10.col(\"firstname\"), tbl10.col(\"lastname\"), tbl11.col(\"qtysold\"), tbl11.col(\"pricepaid\"),"
                + " tbl11.col(\"commission\"))\n");
        }));
        it("should produce Spark for multiple join conditions", mocks.inject(function (VisualQueryService) {
            var spark = new spark_query_parser_1.SparkQueryParser(VisualQueryService).toScript({
                "nodes": [{
                        "id": 11,
                        "name": "tickit.sales",
                        "nodeAttributes": {
                            "attributes": [{ "name": "salesid", "dataType": "int", "selected": false, "description": null }, { "name": "buyerid", "dataType": "int", "selected": false, "description": null },
                                { "name": "eventid", "dataType": "int", "selected": false, "description": null }, { "name": "dateid", "dataType": "smallint", "selected": false, "description": null },
                                { "name": "qtysold", "dataType": "string", "selected": true, "description": null }, { "name": "pricepaid", "dataType": "double", "selected": true, "description": null },
                                { "name": "commission", "dataType": "double", "selected": true, "description": null }], "sql": "`tickit`.`sales`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 40 }, "left": { "location": "LEFT", "id": 37 }, "right": { "location": "RIGHT", "id": 38 }, "top": { "location": "TOP", "id": 39 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 12,
                        "name": "tickit.event",
                        "nodeAttributes": {
                            "attributes": [{ "name": "eventid", "dataType": "int", "selected": false, "description": null },
                                { "name": "dateid", "dataType": "smallint", "selected": false, "description": null }, { "name": "eventname", "dataType": "string", "selected": true, "description": null }],
                            "sql": "`tickit`.`event`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 44 }, "left": { "location": "LEFT", "id": 41 }, "right": { "location": "RIGHT", "id": 42 }, "top": { "location": "TOP", "id": 43 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 14,
                        "name": "tickit.date",
                        "nodeAttributes": {
                            "attributes": [{ "name": "dateid", "dataType": "smallint", "selected": false, "description": null },
                                { "name": "caldate", "dataType": "date", "selected": true, "description": null }],
                            "sql": "`tickit`.`date`"
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 48 }, "left": { "location": "LEFT", "id": 45 }, "right": { "location": "RIGHT", "id": 46 }, "top": { "location": "TOP", "id": 47 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }],
                "connections": [{
                        "source": { "nodeID": 12, "connectorIndex": 0, "connectorId": 41 },
                        "dest": { "nodeID": 11, "connectorIndex": 0, "connectorID": 37 },
                        "joinKeys": { "sourceKey": "eventid", "destKey": "eventid" },
                        "joinType": "INNER JOIN"
                    }, {
                        "source": { "nodeID": 14, "connectorIndex": 0, "connectorId": 45 },
                        "dest": { "nodeID": 12, "connectorIndex": 0, "connectorID": 41 },
                        "joinKeys": { "sourceKey": "dateid", "destKey": "dateid" },
                        "joinType": "INNER JOIN"
                    }, {
                        "source": { "nodeID": 14, "connectorIndex": 0, "connectorId": 45 },
                        "dest": { "nodeID": 11, "connectorIndex": 0, "connectorID": 37 },
                        "joinKeys": { "sourceKey": "dateid", "destKey": "dateid" },
                        "joinType": "INNER JOIN"
                    }]
            }, []);
            expect(spark).toBe("val tbl11 = sqlContext.table(\"tickit.sales\").alias(\"tbl11\")\n"
                + "val tbl12 = sqlContext.table(\"tickit.event\").alias(\"tbl12\")\n"
                + "val tbl14 = sqlContext.table(\"tickit.date\").alias(\"tbl14\")\n"
                + "var df = tbl11.join(tbl12, tbl12.col(\"eventid\").equalTo(tbl11.col(\"eventid\")), \"inner\")"
                + ".join(tbl14, tbl14.col(\"dateid\").equalTo(tbl11.col(\"dateid\")).and(tbl14.col(\"dateid\").equalTo(tbl12.col(\"dateid\"))), \"inner\")"
                + ".select(tbl11.col(\"qtysold\"), tbl11.col(\"pricepaid\"), tbl11.col(\"commission\"), tbl12.col(\"eventname\"), tbl14.col(\"caldate\"))\n");
        }));
        it("should produce Spark for multiple data sources", mocks.inject(function (VisualQueryService) {
            var spark = new spark_query_parser_1.SparkQueryParser(VisualQueryService).toScript({
                "nodes": [{
                        "id": 10,
                        "name": "sample.t1",
                        "datasourceId": "HIVE",
                        "nodeAttributes": {
                            "attributes": [{ "name": "id", "dataType": "smallint", "selected": true, "description": null }, { "name": "id_1", "dataType": "smallint", "selected": true, "description": null }],
                            "reference": ["sample", "t1"]
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 24 }, "left": { "location": "LEFT", "id": 21 }, "right": { "location": "RIGHT", "id": 22 }, "top": { "location": "TOP", "id": 23 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }, {
                        "id": 11,
                        "name": "sample.t2",
                        "datasourceId": "0e63b63e-e1c3-4502-99fb-b86272bc6ede",
                        "nodeAttributes": {
                            "attributes": [{ "name": "id", "dataType": "smallint", "selected": true, "description": "t2 id" },
                                { "name": "t1_id", "dataType": "smallint", "selected": true, "description": null }, { "name": "sample_t1_id", "dataType": "smallint", "selected": true, "description": null }],
                            "reference": ["sample", "t2"]
                        },
                        "connectors": { "bottom": { "location": "BOTTOM", "id": 28 }, "left": { "location": "LEFT", "id": 25 }, "right": { "location": "RIGHT", "id": 26 }, "top": { "location": "TOP", "id": 27 } },
                        "inputConnectors": [{ "name": "" }],
                        "outputConnectors": [{ "name": "" }],
                        "width": 250
                    }],
                "connections": []
            }, []);
            expect(spark).toBe("val tbl10 = sqlContext.table(\"sample.t1\").alias(\"tbl10\")\n"
                + "val tbl11 = datasourceProvider.getTableFromDatasource(\"sample.t2\", \"0e63b63e-e1c3-4502-99fb-b86272bc6ede\", sqlContext).alias(\"tbl11\")\n"
                + "var df = tbl10.join(tbl11).select(tbl10.col(\"id\").as(\"id_2\"), tbl10.col(\"id_1\"),"
                + " tbl11.col(\"id\").as(\"t2_id\", new org.apache.spark.sql.types.MetadataBuilder().putString(\"comment\", \"t2 id\").build()), tbl11.col(\"t1_id\").as(\"t2_t1_id\"),"
                + " tbl11.col(\"sample_t1_id\").as(\"t2_sample_t1_id\"))\n");
        }));
    });
});
//# sourceMappingURL=spark-query-parser.spec.js.map