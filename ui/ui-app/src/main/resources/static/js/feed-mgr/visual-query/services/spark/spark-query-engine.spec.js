define(["require", "exports", "./spark-query-engine", "feed-mgr/visual-query/module", "feed-mgr/visual-query/module-require"], function (require, exports, spark_query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var mocks = require("angular-mocks");
    var moduleName = require("feed-mgr/visual-query/module-name");
    describe("Class: SparkQueryEngine", function () {
        // Include dependencies
        beforeEach(mocks.module("kylo", "kylo.feedmgr", moduleName));
        // constructor
        it("should construct with a SQL statement", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid", []);
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df\n");
        }));
        // canRedo
        it("should indicate redo when possible", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid", []);
            expect(service.canRedo()).toBe(false);
            service.redo_.push(service.newState());
            expect(service.canRedo()).toBe(true);
        }));
        // canUndo
        it("should indicate undo when possible", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid", []);
            expect(service.canUndo()).toBe(false);
            service.states_.push(service.newState());
            expect(service.canUndo()).toBe(true);
        }));
        // getColumnDefs
        it("should generate column type definitions", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            // Create service
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid", []);
            service.states_[0].columns = [
                { field: "pricepaid", hiveColumnLabel: "pricepaid" },
                { field: "commission", hiveColumnLabel: "commission" },
                { field: "qtysold", hiveColumnLabel: "qtysold" }
            ];
            // Test column defs
            var defs = service.getColumnDefs();
            expect(defs).toEqual({
                "!name": "columns",
                "pricepaid": "Column",
                "commission": "Column",
                "qtysold": "Column"
            });
        }));
        // getColumnLabel
        it("should get column label for field name", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            // Create service
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid", []);
            service.states_[0].columns = [
                { field: "col1", hiveColumnLabel: "(pricepaid - commission)" }
            ];
            // Test column labels
            expect(service.getColumnLabel("invalid")).toBe(null);
            expect(service.getColumnLabel("col1")).toBe("(pricepaid - commission)");
        }));
        // getFeedScript
        it("should get a feed script", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            // Create service
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid", []);
            service.states_[0].columns = [
                { field: "pricepaid", hiveColumnLabel: "pricepaid" },
                { field: "commission", hiveColumnLabel: "commission" },
                { field: "qtysold", hiveColumnLabel: "qtysold" }
            ];
            service.setFunctionDefs({
                "!define": { "Column": { "as": { "!spark": ".as(%s)", "!sparkType": "column" } } },
                "divide": { "!spark": "%c.divide(%c)", "!sparkType": "column" },
                "multiply": { "!spark": "%c.multiply(%c)", "!sparkType": "column" }
            });
            // Test script
            var formula = "(divide(divide(commission, pricepaid), qtysold) * 100).as(\"overhead\")";
            service.push(tern.parse(formula), {});
            expect(service.getFeedScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df\n"
                + "df = df.select(df(\"*\"), df(\"commission\").divide(df(\"pricepaid\")).divide(df(\"qtysold\")).multiply(functions.lit(100)).as(\"overhead\"))\n"
                + "df\n");
        }));
        // getFields
        describe("get fields", function () {
            it("from applied transformation", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid", []);
                service.states_[0].columns = [
                    { dataType: "string", hiveColumnLabel: "username", comment: null },
                    { dataType: "decimal(8,2)", hiveColumnLabel: "(pricepaid - commission) / qtysold", comment: "avg tkt price" }
                ];
                expect(service.getFields()).toEqual([
                    { name: "username", description: null, dataType: "string", primaryKey: false, nullable: false, sampleValues: [], derivedDataType: "string" },
                    {
                        name: "(pricepaid - commission) / qtysold", description: "avg tkt price", dataType: "decimal", primaryKey: false, nullable: false, sampleValues: [], precisionScale: "8,2",
                        derivedDataType: "decimal"
                    }
                ]);
            }));
            it("from null columns", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid", []);
                expect(service.getFields()).toBe(null);
            }));
        });
        // getScript
        describe("get script", function () {
            it("from an array expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.setFunctionDefs({
                    "vectorAssembler": { "!spark": "new VectorAssembler().setInputCols(%@s).setOutputCol(%s).transform", "!sparkType": "transform" }
                });
                // Test empty array
                service.push(tern.parse("vectorAssembler([], \"out\")"), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.transform(new VectorAssembler().setInputCols(Array()).setOutputCol(\"out\").transform)\n"
                    + "df\n");
                // Test array of one element
                service.pop();
                service.push(tern.parse("vectorAssembler([\"in\"], \"out\")"), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.transform(new VectorAssembler().setInputCols(Array(\"in\")).setOutputCol(\"out\").transform)\n"
                    + "df\n");
                // Test array of multiple elements
                service.pop();
                service.push(tern.parse("vectorAssembler([\"in1\", \"in2\"], \"out\")"), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.transform(new VectorAssembler().setInputCols(Array(\"in1\", \"in2\")).setOutputCol(\"out\").transform)\n"
                    + "df\n");
            }));
            it("from a column expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.states_[0].columns = [
                    { field: "pricepaid", hiveColumnLabel: "pricepaid" },
                    { field: "commission", hiveColumnLabel: "commission" },
                    { field: "qtysold", hiveColumnLabel: "qtysold" }
                ];
                service.setFunctionDefs({
                    "!define": { "Column": { "as": { "!spark": ".as(%s)", "!sparkType": "column" } } },
                    "divide": { "!spark": "%c.divide(%c)", "!sparkType": "column" },
                    "multiply": { "!spark": "%c.multiply(%c)", "!sparkType": "column" }
                });
                // Test script
                var formula = "(divide(divide(commission, pricepaid), qtysold) * 100).as(\"overhead\")";
                service.push(tern.parse(formula), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.select(df(\"*\"), df(\"commission\").divide(df(\"pricepaid\")).divide(df(\"qtysold\")).multiply(functions.lit(100)).as(\"overhead\"))\n"
                    + "df\n");
            }));
            it("from a DataFrame expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.states_[0].columns = [
                    { field: "pricepaid", hiveColumnLabel: "pricepaid" },
                    { field: "label", hiveColumnLabel: "label" }
                ];
                service.setFunctionDefs({
                    "!define": {
                        "LogisticRegression": {
                            "run": { "!spark": ".fit(%r).transform", "!sparkType": "transform" },
                            "setMaxIter": { "!spark": ".setMaxIter(%d)", "!sparkType": "LogisticRegression" },
                            "setRegParam": { "!spark": ".setRegParam(%f)", "!sparkType": "LogisticRegression" }
                        }
                    },
                    "LogisticRegression": { "!spark": "new LogisticRegression()", "!sparkType": "LogisticRegression" },
                    "sample": { "!spark": ".sample(false, %f)", "!sparkType": "dataframe" },
                    "vectorAssembler": { "!spark": "new VectorAssembler().setInputCols(%@s).setOutputCol(%s).transform", "!sparkType": "transform" }
                });
                // Test script
                service.push(tern.parse("vectorAssembler([\"pricepaid\"], \"features\")"), {});
                service.push(tern.parse("LogisticRegression().setMaxIter(10).setRegParam(0.01).run(sample(0.1))"), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.transform(new VectorAssembler().setInputCols(Array(\"pricepaid\")).setOutputCol(\"features\").transform)\n"
                    + "df = df.transform(new LogisticRegression().setMaxIter(10).setRegParam(0.01).fit(df.sample(false, 0.1)).transform)\n"
                    + "df\n");
            }));
            it("from a filter expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.states_[0].columns = [
                    { field: "pricepaid", hiveColumnLabel: "pricepaid" },
                    { field: "commission", hiveColumnLabel: "commission" },
                    { field: "qtysold", hiveColumnLabel: "qtysold" }
                ];
                service.setFunctionDefs({
                    "and": { "!spark": "%c.and(%c)", "!sparkType": "column" },
                    "equal": { "!spark": "%c.equalTo(%c)", "!sparkType": "column" },
                    "filter": { "!spark": ".filter(%c)", "!sparkType": "dataframe" },
                    "greaterThan": { "!spark": "%c.gt(%c)", "!sparkType": "column" },
                    "subtract": { "!spark": "%c.minus(%c)", "!sparkType": "column" }
                });
                // Test script
                var formula = "filter(qtysold == 2 && pricepaid - commission > 200)";
                service.push(tern.parse(formula), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.filter(df(\"qtysold\").equalTo(functions.lit(2)).and(df(\"pricepaid\").minus(df(\"commission\")).gt(functions.lit(200))))\n"
                    + "df\n");
            }));
            it("from an object expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.states_[0].columns = [
                    { field: "eventname", hiveColumnLabel: "eventname" },
                    { field: "venuecity", hiveColumnLabel: "venuecity" }
                ];
                service.setFunctionDefs({
                    "!define": { "Column": { "over": { "!spark": ".over(%o)", "!sparkType": "column" } } },
                    "partitionBy": { "!spark": "Window.partitionBy(%*c)", "!sparkType": "WindowSpec" }
                });
                // Test script
                service.push(tern.parse("eventname.over(partitionBy(venuecity))"), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.select(df(\"*\"), df(\"eventname\").over(Window.partitionBy(df(\"venuecity\"))))\n"
                    + "df\n");
            }));
            it("from an optional expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.setFunctionDefs({
                    "rand": { "!spark": "functions.rand(%?d)", "!sparkType": "column" }
                });
                // Test script
                var formula = "rand()";
                service.push(tern.parse(formula), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.select(df(\"*\"), functions.rand())\n"
                    + "df\n");
            }));
            it("from a vararg expression", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
                // Create service
                var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
                service.setQuery("SELECT * FROM invalid");
                service.states_[0].columns = [
                    { field: "username", hiveColumnLabel: "username" },
                    { field: "eventname", hiveColumnLabel: "eventname" },
                    { field: "qtysold", hiveColumnLabel: "qtysold" },
                    { field: "pricepaid", hiveColumnLabel: "pricepaid" }
                ];
                service.setFunctionDefs({
                    "!define": {
                        "GroupedData": {
                            "sum": { "!spark": ".sum(%s%,*s)", "!sparkType": "dataframe" }
                        }
                    },
                    "groupBy": { "!spark": ".groupBy(%*c)", "!sparkType": "GroupedData" }
                });
                // Test script
                var formula = "groupBy(username, eventname).sum(\"qtysold\", \"pricepaid\")";
                service.push(tern.parse(formula), {});
                expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                    + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                    + "df = df.limit(1000)\n"
                    + "df = df.groupBy(df(\"username\"), df(\"eventname\")).sum(\"qtysold\", \"pricepaid\")\n"
                    + "df\n");
            }));
        });
        // limit
        it("should get and set limit", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid");
            expect(service.limit()).toBe(1000);
            service.limit(5000);
            expect(service.limit()).toBe(5000);
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(5000)\n"
                + "df\n");
        }));
        // redo
        it("should redo the last undone transformation", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            // Initialize service
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid");
            var state = service.newState();
            state.context = { formula: "42", icon: "code", name: "answer" };
            state.script = ".withColumn(\"col1\", functions.lit(42))";
            service.redo_.push(state);
            // Test redo
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df\n");
            expect(service.redo()).toEqual({ formula: "42", icon: "code", name: "answer" });
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df = df.withColumn(\"col1\", functions.lit(42))\n"
                + "df\n");
        }));
        // sample
        it("should get and set sample", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid");
            expect(service.sample()).toBe(1.0);
            service.sample(0.01);
            expect(service.sample()).toBe(0.01);
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.sample(false, 0.01).limit(1000)\n"
                + "df\n");
        }));
        // shouldLimitBeforeSample
        it("should limit before sampling", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid");
            expect(service.shouldLimitBeforeSample()).toBe(false);
            service.sample(0.01);
            service.shouldLimitBeforeSample(true);
            expect(service.shouldLimitBeforeSample()).toBe(true);
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000).sample(false, 0.01)\n"
                + "df\n");
        }));
        // splice
        it("should splice transformation array", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            // Add formulas
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid");
            service.push(tern.parse("42"), {});
            service.push(tern.parse("\"thinkbig\""), {});
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df = df.select(df(\"*\"), functions.lit(42))\n"
                + "df = df.select(df(\"*\"), functions.lit(\"thinkbig\"))\n"
                + "df\n");
            // Delete first formula
            service.splice(1, 1);
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df = df.select(df(\"*\"), functions.lit(\"thinkbig\"))\n"
                + "df\n");
        }));
        // undo
        it("should undo the last transformation", mocks.inject(function ($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            // Initialize service
            var service = new spark_query_engine_1.SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService);
            service.setQuery("SELECT * FROM invalid");
            var state = service.newState();
            state.context = { formula: "42", icon: "code", name: "answer" };
            state.script = ".withColumn(\"col1\", functions.lit(42))";
            service.states_.push(state);
            // Test redo
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df = df.withColumn(\"col1\", functions.lit(42))\n"
                + "df\n");
            expect(service.undo()).toEqual({ formula: "42", icon: "code", name: "answer" });
            expect(service.getScript()).toBe("import org.apache.spark.sql._\n"
                + "var df = sqlContext.sql(\"SELECT * FROM invalid\")\n"
                + "df = df.limit(1000)\n"
                + "df\n");
        }));
    });
});
//# sourceMappingURL=spark-query-engine.spec.js.map