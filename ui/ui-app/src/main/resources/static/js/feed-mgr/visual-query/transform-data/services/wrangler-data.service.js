define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    var PAGE_ROWS = 64;
    var PAGE_COLS = 1000;
    var WranglerDataService = /** @class */ (function () {
        function WranglerDataService($rootscope, $q, $timeout) {
            var _this = this;
            this.$rootscope = $rootscope;
            this.$q = $q;
            this.$timeout = $timeout;
            /**
             * The sort direction.
             */
            this.sortDirection_ = null;
            /**
             * The index of the column being sorted.
             */
            this.sortIndex_ = null;
            this.loading = false;
            this.fetchTimeoutPromise = null;
            this.fetchTimeout = function (callback, interval) {
                if (_this.fetchTimeoutPromise != null) {
                    _this.$timeout.cancel(_this.fetchTimeoutPromise);
                }
                _this.fetchTimeoutPromise = _this.$timeout(callback, interval);
            };
        }
        WranglerDataService.prototype.cellPageName = function (i, j) {
            var I = (i / PAGE_ROWS) | 0;
            var J = (j / PAGE_COLS) | 0;
            return JSON.stringify({ "state": this.state, "coords": [I, J] });
        };
        WranglerDataService.prototype.headerPageName = function (j) {
            var J = (j / PAGE_COLS) | 0;
            return JSON.stringify({ "state": this.state, "j": J });
        };
        ;
        WranglerDataService.prototype.fetchCellPage = function (pageName, cb) {
            var _this = this;
            this.fetchTimeout(function () {
                var coordsObj = JSON.parse(pageName);
                var I = coordsObj.coords[0];
                var J = coordsObj.coords[1];
                var self = _this;
                _this.asyncQuery(true, {
                    firstRow: I * PAGE_ROWS,
                    numRows: PAGE_ROWS,
                    firstCol: J * PAGE_COLS,
                    numCols: PAGE_COLS * 2
                }).then(function () {
                    cb(function (i, j) {
                        return self.getCell(i - I * PAGE_ROWS, j - J * PAGE_COLS);
                    });
                });
            }, 100);
        };
        /**
         * Gets the value for the specified cell.
         *
         * @param {number} i the row number
         * @param {number} j the column number
         * @returns {VisualQueryTableCell|null} the cell object
         */
        WranglerDataService.prototype.getCell = function (i, j) {
            var column = this.columns_[j];
            if (column != undefined && i >= 0 && i < this.rows_.length) {
                var originalIndex = (this.rows_[i].length > this.columns_.length) ? this.rows_[i][this.columns_.length] : null;
                var validation = (this.validationResults != null && originalIndex < this.validationResults.length && this.validationResults[originalIndex] != null)
                    ? this.validationResults[originalIndex].filter(function (result) { return result.field === column.headerTooltip; })
                    : null;
                return {
                    column: j,
                    field: column.name,
                    row: i,
                    validation: (validation !== null && validation.length > 0) ? validation : null,
                    value: this.rows_[i][j]
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
            if (j >= 0 && j < this.columns_.length) {
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
    angular.module(moduleName).service("WranglerDataService", ["$rootScope", "$q", "$timeout", WranglerDataService]);
});
//# sourceMappingURL=wrangler-data.service.js.map