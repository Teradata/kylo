define(["require", "exports", "angular", "underscore", "../../wrangler/query-engine"], function (require, exports, angular, _, query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    var PAGE_ROWS = 64;
    var PAGE_COLS = 1000;
    var WranglerDataService = /** @class */ (function () {
        function WranglerDataService($rootscope, $q) {
            this.$rootscope = $rootscope;
            this.$q = $q;
            /**
             * The sort direction.
             */
            this.sortDirection_ = null;
            /**
             * The index of the column being sorted.
             */
            this.sortIndex_ = null;
        }
        WranglerDataService.prototype.cellPageName = function (i, j) {
            var I = (i / PAGE_ROWS) | 0;
            var J = (j / PAGE_COLS) | 0;
            var name = JSON.stringify({ "state": this.state, "coords": [I, J] });
            return name;
        };
        WranglerDataService.prototype.headerPageName = function (j) {
            var J = (j / PAGE_COLS) | 0;
            return JSON.stringify({ "state": this.state, "j": J });
        };
        ;
        WranglerDataService.prototype.fetchCellPage = function (pageName, cb) {
            var _this = this;
            var asyncFn = _.debounce(function () {
                var coordsObj = JSON.parse(pageName);
                var I = coordsObj.coords[0];
                var J = coordsObj.coords[1];
                var self = _this;
                var firstRow = I * PAGE_ROWS;
                var firstCol = J * PAGE_COLS;
                _this.asyncQuery(new query_engine_1.PageSpec({
                    firstRow: firstRow,
                    numRows: PAGE_ROWS,
                    firstCol: firstCol,
                    numCols: PAGE_COLS
                })).then(function (result) {
                    _this.state = result.tableState;
                    var rows = result.rows;
                    var validationResults = result.validationResults;
                    cb(function (i, j) {
                        return self.getCell(i - I * PAGE_ROWS, j - J * PAGE_COLS, rows, validationResults);
                    });
                });
            }, 100);
            asyncFn();
        };
        /**
         * Gets the value for the specified cell.
         *
         * @param {number} i the row number
         * @param {number} j the column number
         * @returns {VisualQueryTableCell|null} the cell object
         */
        WranglerDataService.prototype.getCell = function (i, j, rows, validationResults) {
            var column = this.columns_[j];
            if (column != undefined && i >= 0 && rows && i < rows.length) {
                var validation = (validationResults != null && i < validationResults.length && validationResults[i] != null)
                    ? validationResults[i].filter(function (result) {
                        return (result.field === column.displayName);
                    })
                    : null;
                return {
                    column: j,
                    field: column.name,
                    row: i,
                    validation: (validation !== null && validation.length > 0) ? validation : null,
                    value: rows[i][j]
                };
            }
            else {
                return null;
            }
        };
        /**
         * Gets the header of the specified column.
         *
         * @param {number} j the column number
         * @returns {VisualQueryTableHeader|null} the column header
         */
        WranglerDataService.prototype.getHeader = function (j) {
            if (j >= 0 && this.columns_ && j < this.columns_.length) {
                return angular.extend(this.columns_[j], {
                    field: this.columns_[j].name,
                    index: j,
                    sort: {
                        direction: (this.sortIndex_ === j) ? this.sortDirection_ : null
                    }
                });
            }
            return null;
        };
        return WranglerDataService;
    }());
    exports.WranglerDataService = WranglerDataService;
    angular.module(moduleName).service("WranglerDataService", ["$rootScope", "$q", WranglerDataService]);
});
//# sourceMappingURL=wrangler-data.service.js.map