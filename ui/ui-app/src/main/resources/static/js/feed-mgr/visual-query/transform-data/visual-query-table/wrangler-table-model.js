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
    var TableCache = /** @class */ (function () {
        function TableCache(size) {
            this.size = 100;
            this.data = {};
            this.lru_keys = [];
            this.size = size;
        }
        TableCache.prototype.has = function (k) {
            return this.data.hasOwnProperty(k);
        };
        TableCache.prototype.get = function (k) {
            return this.data[k];
        };
        TableCache.prototype.clear = function () {
            this.data = {};
            this.lru_keys = [];
        };
        TableCache.prototype.set = function (k, v) {
            var idx, removeKey;
            idx = this.lru_keys.indexOf(k);
            if (idx >= 0) {
                this.lru_keys.splice(idx, 1);
            }
            this.lru_keys.push(k);
            if (this.lru_keys.length >= this.size) {
                removeKey = this.lru_keys.shift();
                delete this.data[removeKey];
            }
            return this.data[k] = v;
        };
        ;
        return TableCache;
    }());
    exports.TableCache = TableCache;
    var WranglerTableModel = /** @class */ (function (_super) {
        __extends(WranglerTableModel, _super);
        function WranglerTableModel(data) {
            var _this = _super.call(this) || this;
            _this.data = data;
            _this.cacheSize = 100;
            _this.pageCache = new TableCache(_this.cacheSize);
            _this.headerPageCache = new TableCache(_this.cacheSize);
            _this.fetchCallbacks = {};
            _this.headerFetchCallbacks = {};
            return _this;
        }
        WranglerTableModel.prototype.hasCell = function (i, j) {
            var pageName;
            pageName = this.cellPageName(i, j);
            return this.pageCache.has(pageName);
        };
        ;
        WranglerTableModel.prototype.getCell = function (i, j, cb) {
            var pageName;
            var self = this;
            if (cb == null) {
                cb = (function (data) {
                });
            }
            pageName = this.cellPageName(i, j);
            if (this.pageCache.has(pageName)) {
                return cb(this.pageCache.get(pageName)(i, j));
            }
            else if (this.fetchCallbacks[pageName] != null) {
                return this.fetchCallbacks[pageName].push([i, j, cb]);
            }
            else {
                this.fetchCallbacks[pageName] = [[i, j, cb]];
                return this.fetchCellPage(pageName, (function (self) {
                    return function (page) {
                        var len, n, ref, ref1;
                        self.pageCache.set(pageName, page);
                        ref = self.fetchCallbacks[pageName];
                        for (n = 0, len = ref.length; n < len; n++) {
                            ref1 = ref[n], i = ref1[0], j = ref1[1], cb = ref1[2];
                            cb(page(i, j));
                        }
                        return delete self.fetchCallbacks[pageName];
                    };
                })(this));
            }
        };
        ;
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
    }(fattable.TableModel));
    exports.WranglerTableModel = WranglerTableModel;
});
//# sourceMappingURL=wrangler-table-model.js.map