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
        WranglerTableModel.prototype.getHeader = function (j, cb) {
            cb(this.data.getHeader(j));
        };
        ;
        WranglerTableModel.prototype.cellPageName = function (i, j) {
            return this.data.cellPageName(i, j);
        };
        WranglerTableModel.prototype.fetchCellPage = function (pageName, cb) {
            this.data.fetchCellPage(pageName, cb);
        };
        WranglerTableModel.prototype.headerPageName = function (j) {
            return this.data.headerPageName(j);
        };
        ;
        return WranglerTableModel;
    }(fattable.PagedAsyncTableModel));
    exports.WranglerTableModel = WranglerTableModel;
});
//# sourceMappingURL=wrangler-table-model.js.map