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
            addFunction: function(name, icon, formula) {
                expect(name).toBe("Hide col1");
                expect(icon).toBe("remove_circle");
                expect(formula).toBe("drop(\"col1\")");
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

        var delegate = new VisualQueryColumnDelegate(controller);
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
            pushFormula: function(name, icon, formula) {
                expect(name).toBe("Rename col1 to ticketprice");
                expect(icon).toBe("mode_edit");
                expect(formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                done();
            }
        };
        var grid = {
            columns: [
                {field: "username"},
                {field: "col1"},
                {field: "eventname"}
            ]
        };

        var delegate = new VisualQueryColumnDelegate(controller);
        delegate.renameColumn(column, grid);

        // Complete deferred
        deferred.resolve("ticketprice");
        $scope.$digest();
    });

    // transformColumn
    it("should transform a column", function(done) {
        var column = {displayName: "col1", field: "col1"};
        var controller = {
            addFunction: function(name, icon, formula) {
                expect(name).toBe("Uppercase col1");
                expect(icon).toBe("arrow_upward");
                expect(formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                done();
            }
        };
        var grid = {
            columns: [
                {field: "username"},
                {field: "col1"},
                {field: "eventname"}
            ]
        };

        var delegate = new VisualQueryColumnDelegate(controller);
        delegate.transformColumn("Uppercase", "arrow_upward", "upper", column, grid);
    });
});
