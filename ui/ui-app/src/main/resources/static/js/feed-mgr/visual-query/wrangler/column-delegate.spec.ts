import "feed-mgr/visual-query/module"
import "feed-mgr/visual-query/module-require";
import {TransformDataComponent} from "../transform-data/transform-data.component";
import {ColumnDelegate} from "./column-delegate";

const mocks: angular.IMockStatic = require("angular-mocks");
const moduleName: string = require("feed-mgr/visual-query/module-name");

describe("Class: ColumnDelegate", function () {
    // Include dependencies
    beforeEach(mocks.module("kylo", "kylo.feedmgr", moduleName));

    // hideColumn
    it("should hide a column", function (done) {
        const controller = {
            pushFormula: function (formula: string, context: any) {
                expect(formula).toBe("drop(\"col1\")");
                expect(context.formula).toBe("drop(\"col1\")");
                expect(context.icon).toBe("remove_circle");
                expect(context.name).toBe("Hide col1");
                done();
            }
        } as TransformDataComponent;
        const grid = {
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

        mocks.inject(function (uiGridConstants: any) {
            const delegate = new ColumnDelegate("string", controller, null, uiGridConstants);
            delegate.hideColumn({colDef: {}, displayName: "col1", field: "col1"}, grid);
        });
    });

    // renameColumn
    it("should rename a column", function (done) {
        // Mock dialog
        let deferred: angular.IDeferred<any>;
        mocks.inject(function ($mdDialog: angular.material.IDialogService, $q: angular.IQService) {
            deferred = $q.defer();
            $mdDialog.show = function () {
                return deferred.promise;
            };
        });

        // Test rename column
        const column = {displayName: "col1", field: "col1"};
        const controller = {
            addFunction: function (formula: string, context: any) {
                expect(formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                expect(context.formula).toBe("select(username, col1.as(\"ticketprice\"), eventname)");
                expect(context.icon).toBe("mode_edit");
                expect(context.name).toBe("Rename col1 to ticketprice");
                done();
            }
        } as TransformDataComponent;
        const grid = {
            columns: [
                {field: "username", visible: true},
                {field: "col1", visible: true},
                {field: "eventname", visible: true}
            ]
        };

        mocks.inject(function ($rootScope: angular.IRootScopeService, $mdDialog: angular.material.IDialogService, uiGridConstants: any) {
            const delegate = new ColumnDelegate("double", controller, $mdDialog, uiGridConstants);
            delegate.renameColumn(column, grid);

            // Complete deferred
            deferred.resolve("ticketprice");
            $rootScope.$digest();
        });
    });

    // transformColumn
    it("should transform a column", function (done) {
        const column = {displayName: "col1", field: "col1"};
        const controller = {
            addFunction: function (formula: string, context: any) {
                expect(formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                expect(context.formula).toBe("select(username, upper(col1).as(\"col1\"), eventname)");
                expect(context.icon).toBe("arrow_upward");
                expect(context.name).toBe("Uppercase col1");
                done();
            }
        } as TransformDataComponent;
        const grid = {
            columns: [
                {field: "username", visible: true},
                {field: "col1", visible: true},
                {field: "eventname", visible: true}
            ]
        };

        mocks.inject(function (uiGridConstants: any) {
            const delegate = new ColumnDelegate("string", controller, null, uiGridConstants);
            delegate.transformColumn({description: "Uppercase", icon: "arrow_upward", name: "Upper Case", operation: "upper"},
                column, grid);
        });
    });
});
