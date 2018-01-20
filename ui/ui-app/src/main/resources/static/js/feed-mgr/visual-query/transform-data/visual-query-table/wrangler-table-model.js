var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "fattable"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var WranglerTableModel = /** @class */ (function (_super) {
        __extends(WranglerTableModel, _super);
        function WranglerTableModel(data) {
            var _this = _super.call(this) || this;
            _this.data = data;
            return _this;
        }
        /**
         * Gets the value for the specified cell.
         *
         * @param {number} i the row number
         * @param {number} j the column number
         * @returns {VisualQueryTableCell|null} the cell object
         */
        WranglerTableModel.prototype.getCellSync = function (i, j) {
            return this.data.getCellSync(i, j);
        };
        /**
         * Gets the header of the specified column.
         *
         * @param {number} j the column number
         * @returns {VisualQueryTableHeader|null} the column header
         */
        WranglerTableModel.prototype.getHeaderSync = function (j) {
            return this.data.getHeaderSync(j);
        };
        return WranglerTableModel;
    }(fattable.SyncTableModel));
    exports.WranglerTableModel = WranglerTableModel;
});
//# sourceMappingURL=wrangler-table-model.js.map