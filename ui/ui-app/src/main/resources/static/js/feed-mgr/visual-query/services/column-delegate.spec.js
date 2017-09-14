define(["require", "exports", "./column-delegate", "feed-mgr/visual-query/module", "feed-mgr/visual-query/module-require"], function (require, exports, column_delegate_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var mocks = require("angular-mocks");
    var moduleName = require("feed-mgr/visual-query/module-name");
    describe("Class: ColumnDelegate", function () {
        // Include dependencies
        beforeEach(mocks.module("kylo", "kylo.feedmgr", moduleName));
        // hideColumn
        it("should hide a column", function (done) {
            var controller = {
                pushFormula: function (formula, context) {
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
                        notifyDataChange: function () {
                        },
                        raise: {
                            columnVisibilityChanged: function () {
                            }
                        }
                    }
                },
                onColumnsChange: function () {
                },
                queueGridRefresh: function () {
                },
                refresh: function () {
                }
            };
            mocks.inject(function (uiGridConstants) {
                var delegate = new column_delegate_1.ColumnDelegate("string", controller, null, uiGridConstants);
                delegate.hideColumn({ colDef: {}, displayName: "col1", field: "col1" }, grid);
            });
        });
        // renameColumn
        it("should rename a column", function (done) {
            // Mock dialog
            var deferred;
            mocks.inject(function ($mdDialog, $q) {
                deferred = $q.defer();
                $mdDialog.show = function () {
                    return deferred.promise;
                };
            });
            // Test rename column
            var column = { displayName: "col1", field: "col1" };
            var controller = {
                addFunction: function (formula, context) {
                    expect(formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                    expect(context.formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                    expect(context.icon).toBe("mode_edit");
                    expect(context.name).toBe("Rename col1 to ticketprice");
                    done();
                }
            };
            var grid = {
                columns: [
                    { field: "username", visible: true },
                    { field: "col1", visible: true },
                    { field: "eventname", visible: true }
                ]
            };
            mocks.inject(function ($rootScope, $mdDialog, uiGridConstants) {
                var delegate = new column_delegate_1.ColumnDelegate("double", controller, $mdDialog, uiGridConstants);
                delegate.renameColumn(column, grid);
                // Complete deferred
                deferred.resolve("ticketprice");
                $rootScope.$digest();
            });
        });
        // transformColumn
        it("should transform a column", function (done) {
            var column = { displayName: "col1", field: "col1" };
            var controller = {
                addFunction: function (formula, context) {
                    expect(formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                    expect(context.formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                    expect(context.icon).toBe("arrow_upward");
                    expect(context.name).toBe("Uppercase col1");
                    done();
                }
            };
            var grid = {
                columns: [
                    { field: "username", visible: true },
                    { field: "col1", visible: true },
                    { field: "eventname", visible: true }
                ]
            };
            mocks.inject(function (uiGridConstants) {
                var delegate = new column_delegate_1.ColumnDelegate("string", controller, null, uiGridConstants);
                delegate.transformColumn({ description: "Uppercase", icon: "arrow_upward", name: "Upper Case", operation: "upper" }, column, grid);
            });
        });
    });
});
//# sourceMappingURL=column-delegate.spec.js.map