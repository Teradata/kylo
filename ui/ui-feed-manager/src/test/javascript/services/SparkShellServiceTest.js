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

    // getColumnDefs
    it("should generate column type definitions", function() {
        // Create service
        var service = new SparkShellService("SELECT * FROM invalid");
        service.columns_ = [[
            {field: "pricepaid", hiveColumnLabel: "pricepaid"},
            {field: "commission", hiveColumnLabel: "commission"},
            {field: "qtysold", hiveColumnLabel: "qtysold"}
        ]];

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
        service.columns_ = [[
            {field: "col1", hiveColumnLabel: "(pricepaid - commission)"}
        ]];

        // Test column labels
        expect(service.getColumnLabel("invalid")).toBe(null);
        expect(service.getColumnLabel("col1")).toBe("(pricepaid - commission)")
    });

    // getScript
    it("should generate script from a column expression", function() {
        // Create service
        var service = new SparkShellService("SELECT * FROM invalid");
        service.columns_ = [[
            {field: "pricepaid", hiveColumnLabel: "pricepaid"},
            {field: "commission", hiveColumnLabel: "commission"},
            {field: "qtysold", hiveColumnLabel: "qtysold"}
        ]];
        service.setFunctionDefs({
            "!define": {"Column": {"as": {"!spark": ".as(%s)", "!sparkType": "column"}}},
            "divide": {"!spark": "%c.divide(%c)", "!sparkType": "column"},
            "multiply": {"!spark": "%c.multiply(%c)", "!sparkType": "column"}
        });

        // Test script
        service.push(tern.parse("(divide(divide(commission, pricepaid), qtysold) * 100).as(\"overhead\")"));
        expect(service.getScript()).toBe("import org.apache.spark.sql._\n" +
            "sqlContext.sql(\"SELECT * FROM invalid\").limit(1000)" +
            ".select(new Column(\"*\"), new Column(\"commission\").divide(new Column(\"pricepaid\"))" +
            ".divide(new Column(\"qtysold\")).multiply(functions.lit(100)).as(\"overhead\"))");
    });

    // limit
    it("should get and set limit", function() {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.limit()).toBe(1000);

        service.limit(5000);
        expect(service.limit()).toBe(5000);
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(5000)");
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
});
