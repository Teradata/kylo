/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
"use strict";

define(["angularMocks", "feed-mgr/visual-query/module-name", "feed-mgr/visual-query/module", "feed-mgr/visual-query/module-require"], function (mocks, moduleName) {
    describe("VisualQueryColumnDelegate", function () {
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
                queueGridRefresh: function () {
                }
            };

            mocks.inject(function (VisualQueryColumnDelegate) {
                var delegate = new VisualQueryColumnDelegate("string", controller);
                delegate.hideColumn({colDef: {}, displayName: "col1", field: "col1"}, grid);
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
            var column = {displayName: "col1", field: "col1"};
            var controller = {
                pushFormula: function (formula, context) {
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

            mocks.inject(function ($rootScope, VisualQueryColumnDelegate) {
                var delegate = new VisualQueryColumnDelegate("double", controller);
                delegate.renameColumn(column, grid);

                // Complete deferred
                deferred.resolve("ticketprice");
                $rootScope.$digest();
            });
        });

        // transformColumn
        it("should transform a column", function (done) {
            var column = {displayName: "col1", field: "col1"};
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
                    {field: "username", visible: true},
                    {field: "col1", visible: true},
                    {field: "eventname", visible: true}
                ]
            };

            mocks.inject(function (VisualQueryColumnDelegate) {
                var delegate = new VisualQueryColumnDelegate("string", controller);
                delegate.transformColumn({description: "Uppercase", icon: "arrow_upward", name: "Upper Case", operation: "upper"},
                    column, grid);
            });
        });
    });
});
