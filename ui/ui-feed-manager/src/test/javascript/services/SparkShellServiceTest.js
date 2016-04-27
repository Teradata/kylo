"use strict";

describe("SparkShellService", function () {
    // Include dependencies
    beforeEach(module(MODULE_FEED_MGR));

    // Setup tests
    var SparkShellService;

    beforeEach(inject(function ($injector) {
        this.$injector = $injector;
        SparkShellService = $injector.get("SparkShellService");
    }));

    // constructor
    it("should construct with a SQL statement", function () {
        var service = new SparkShellService("SELECT * FROM invalid");
        expect(service.getScript()).toBe("import org.apache.spark.sql._\nsqlContext.sql(\"SELECT * FROM invalid\").limit(1000)");
    });

    // getScript
    it("should generate script from a column expression", function () {
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
});
