"use strict";

describe("VisualQueryColumnDelegate", function() {
    // Include dependencies
    beforeEach(module(MODULE_FEED_MGR));

    // Setup tests
    var $mdDialog, $q, $scope;
    var VisualQueryColumnDelegate;

    beforeEach(inject(function($injector) {
        this.$injector = $injector;
        $mdDialog = $injector.get("$mdDialog");
        $q = $injector.get("$q");
        $scope = $injector.get("$rootScope");
        VisualQueryColumnDelegate = $injector.get("VisualQueryColumnDelegate");
    }));

    // hideColumn
    it("should hide a column", function(done) {
        var controller = {
            pushFormula: function(formula, context) {
                expect(formula).toBe("drop(\"col1\")");
                expect(context.formula).toBe("drop(\"col1\")");
                expect(context.icon).toBe("remove_circle");
                expect(context.name).toBe("Hide col1");
                done();
            }
        };
        var grid = {
            api: {
                core: {
                    notifyDataChange: function() {},
                    raise: {
                        columnVisibilityChanged: function() {}
                    }
                }
            },
            queueGridRefresh: function() {}
        };

        var delegate = new VisualQueryColumnDelegate("string", controller);
        delegate.hideColumn({colDef: {}, displayName: "col1", field: "col1"}, grid);
    });

    // renameColumn
    it("should rename a column", function(done) {
        // Mock dialog
        var deferred = $q.defer();
        $mdDialog.show = function() {
            return deferred.promise;
        };

        // Test rename column
        var column = {displayName: "col1", field: "col1"};
        var controller = {
            pushFormula: function(formula, context) {
                expect(formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                expect(context.formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                expect(context.icon).toBe("mode_edit");
                expect(context.name).toBe("Rename col1 to ticketprice");
                done();
            }
        };
        var grid = {
            columns: [
                {field: "username", visible: true},
                {field: "col1", visible: true},
                {field: "eventname", visible: true}
            ]
        };

        var delegate = new VisualQueryColumnDelegate("double", controller);
        delegate.renameColumn(column, grid);

        // Complete deferred
        deferred.resolve("ticketprice");
        $scope.$digest();
    });

    // transformColumn
    it("should transform a column", function(done) {
        var column = {displayName: "col1", field: "col1"};
        var controller = {
            addFunction: function(formula, context) {
                expect(formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                expect(context.formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                expect(context.icon).toBe("arrow_upward");
                expect(context.name).toBe("Uppercase col1");
                done();
            }
        };
        var grid = {
            columns: [
                {field: "username", visible: true},
                {field: "col1", visible: true},
                {field: "eventname", visible: true}
            ]
        };

        var delegate = new VisualQueryColumnDelegate("string", controller);
        delegate.transformColumn({description: "Uppercase", icon: "arrow_upward", name: "Upper Case", operation: "upper"},
                column, grid);
    });
});
