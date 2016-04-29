"use strict";

describe("SparkShellService", function() {
    // Include dependencies
    beforeEach(module(MODULE_FEED_MGR));

    // Setup tests
    var SparkShellService;

    beforeEach(inject(function($injector) {
        this.$injector = $injector;
        SparkShellService = $injector.get("SparkShellService");
    }));

    // constructor
    it("should construct with a SQL statement", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)");
    });

    // canRedo
    it("should indicate redo when possible", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.canRedo()).toBe(false);

        service.redo_.push(service.newState());
        expect(service.canRedo()).toBe(true);
    });

    // canUndo
    it("should indicate undo when possible", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.canUndo()).toBe(false);

        service.states_.push(service.newState());
        expect(service.canUndo()).toBe(true);
    });

    // getColumnDefs
    it("should generate column type definitions", function() {
        // Create service
        var service = new SparkShellService("SELECT * FROM invalid");
        service.states_[0].columns = [
            {field: "pricepaid", hiveColumnLabel: "pricepaid"},
            {field: "commission", hiveColumnLabel: "commission"},
            {field: "qtysold", hiveColumnLabel: "qtysold"}
        ];

        // Test column defs
        var defs = service.getColumnDefs();
        expect(defs).toEqual({
            "!name": "columns",
            "!define": {
                "Column": {}
            },
            "pricepaid": "Column",
            "commission": "Column",
            "qtysold": "Column"
        });
    });

    // getColumnLabel
    it("should get column label for field name", function() {
        // Create service
        var service = new SparkShellService("SELECT * FROM invalid");
        service.states_[0].columns = [
            {field: "col1", hiveColumnLabel: "(pricepaid - commission)"}
        ];

        // Test column labels
        expect(service.getColumnLabel("invalid")).toBe(null);
        expect(service.getColumnLabel("col1")).toBe("(pricepaid - commission)")
    });

    // getScript
    it("should generate script from a column expression", function() {
        // Create service
        var service = new SparkShellService("SELECT * FROM invalid");
        service.states_[0].columns = [
            {field: "pricepaid", hiveColumnLabel: "pricepaid"},
            {field: "commission", hiveColumnLabel: "commission"},
            {field: "qtysold", hiveColumnLabel: "qtysold"}
        ];
        service.setFunctionDefs({
            "!define": {"Column": {"as": {"!spark": ".as(%s)", "!sparkType": "column"}}},
            "divide": {"!spark": "%c.divide(%c)", "!sparkType": "column"},
            "multiply": {"!spark": "%c.multiply(%c)", "!sparkType": "column"}
        });

        // Test script
        var formula = "(divide(divide(commission, pricepaid), qtysold) * 100).as(\"overhead\")";
        service.push(formula, tern.parse(formula));
        expect(service.getScript()).toBe("import org.apache.spark.sql._\n" +
            "sqlContext.sql(\"SELECT * FROM invalid\").limit(1000)" +
            ".select(new Column(\"*\"), new Column(\"commission\").divide(new Column(\"pricepaid\"))" +
            ".divide(new Column(\"qtysold\")).multiply(functions.lit(100)).as(\"overhead\"))");
    });
    it("should generate script from a filter expression", function() {
        // Create service
        var service = new SparkShellService("SELECT * FROM invalid");
        service.states_[0].columns = [
            {field: "pricepaid", hiveColumnLabel: "pricepaid"},
            {field: "commission", hiveColumnLabel: "commission"},
            {field: "qtysold", hiveColumnLabel: "qtysold"}
        ];
        service.setFunctionDefs({
            "and": {"!spark": "%c.and(%c)", "!sparkType": "column"},
            "equal": {"!spark": "%c.equalTo(%c)", "!sparkType": "column"},
            "filter": {"!spark": ".filter(%c)", "!sparkType": "dataframe"},
            "greaterThan": {"!spark": "%c.gt(%c)", "!sparkType": "column"},
            "subtract": {"!spark": "%c.minus(%c)", "!sparkType": "column"}
        });

        // Test script
        var formula = "filter(qtysold == 2 && pricepaid - commission > 200)";
        service.push(formula, tern.parse(formula));
        expect(service.getScript()).toBe("import org.apache.spark.sql._\n" +
                                         "sqlContext.sql(\"SELECT * FROM invalid\").limit(1000)" +
                                         ".filter(new Column(\"qtysold\").equalTo(functions.lit(2)).and(" +
                                         "new Column(\"pricepaid\").minus(new Column(\"commission\")).gt(functions.lit(200))))");
    });

    // limit
    it("should get and set limit", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.limit()).toBe(1000);

        service.limit(5000);
        expect(service.limit()).toBe(5000);
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(5000)");
    });

    // redo
    it("should redo the last undone transformation", function() {
        // Initialize service
        var service = new SparkShellService("SELECT * FROM invalid");

        var state = service.newState();
        state.formula = "42";
        state.script = ".withColumn(\"col1\", functions.lit(42))";
        service.redo_.push(state);

        // Test redo
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)");

        expect(service.redo()).toBe("42");
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)" +
                                         ".withColumn(\"col1\", functions.lit(42))");
    });

    // sample
    it("should get and set sample", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.sample()).toBe(1.0);

        service.sample(0.01);
        expect(service.sample()).toBe(0.01);
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\")" +
                                         ".sample(false, 0.01).limit(1000)");
    });

    // shouldLimitBeforeSample
    it("should limit before sampling", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.shouldLimitBeforeSample()).toBe(false);

        service.sample(0.01);
        service.shouldLimitBeforeSample(true);
        expect(service.shouldLimitBeforeSample()).toBe(true);
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)" +
                                         ".sample(false, 0.01)");
    });

    // undo
    it("should undo the last transformation", function() {
        // Initialize service
        var service = new SparkShellService("SELECT * FROM invalid");

        var state = service.newState();
        state.formula = "42";
        state.script = ".withColumn(\"col1\", functions.lit(42))";
        service.states_.push(state);

        // Test redo
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)" +
                                         ".withColumn(\"col1\", functions.lit(42))");

        expect(service.undo()).toBe("42");
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)");
    });
});
