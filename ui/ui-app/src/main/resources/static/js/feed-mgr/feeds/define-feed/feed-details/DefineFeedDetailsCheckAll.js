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
define(["require", "exports", "underscore"], function (require, exports, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var CheckAll = /** @class */ (function () {
        function CheckAll() {
            this.isChecked = true;
            this.isIndeterminate = false;
            this.totalChecked = 0;
        }
        CheckAll.prototype.clicked = function (checked, self) {
            if (checked) {
                this.totalChecked++;
            }
            else {
                this.totalChecked--;
            }
            this.markChecked(self);
        };
        ;
        CheckAll.prototype.markChecked = function (self) {
            if (this.totalChecked == self.model.table.fieldPolicies.length) {
                this.isChecked = true;
                this.isIndeterminate = false;
            }
            else if (this.totalChecked > 0) {
                this.isChecked = false;
                this.isIndeterminate = true;
            }
            else if (this.totalChecked == 0) {
                this.isChecked = false;
                this.isIndeterminate = false;
            }
        };
        ;
        return CheckAll;
    }());
    exports.CheckAll = CheckAll;
    /**
             * Toggle Check All/None on Profile column
             * Default it to true
             * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
    */
    var ProfileCheckAll = /** @class */ (function (_super) {
        __extends(ProfileCheckAll, _super);
        function ProfileCheckAll() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.isChecked = true;
            _this.isIndeterminate = false;
            return _this;
        }
        ProfileCheckAll.prototype.toggleAll = function (self) {
            var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
            _.each(self.model.table.fieldPolicies, function (field) {
                field.profile = checked;
            });
            if (checked) {
                this.totalChecked = self.model.table.fieldPolicies.length;
            }
            else {
                this.totalChecked = 0;
            }
            this.markChecked(self);
        };
        ;
        ProfileCheckAll.prototype.setup = function (self) {
            self.profileCheckAll.totalChecked = 0;
            _.each(self.model.table.fieldPolicies, function (field) {
                if (field.profile) {
                    self.profileCheckAll.totalChecked++;
                }
            });
            self.profileCheckAll.markChecked(self);
        };
        return ProfileCheckAll;
    }(CheckAll));
    exports.ProfileCheckAll = ProfileCheckAll;
    /**
             *
             * Toggle check all/none on the index column
             *
             * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
    */
    var IndexCheckAll = /** @class */ (function (_super) {
        __extends(IndexCheckAll, _super);
        function IndexCheckAll() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.isChecked = false;
            _this.isIndeterminate = false;
            return _this;
        }
        IndexCheckAll.prototype.toggleAll = function (self) {
            var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
            _.each(self.model.table.fieldPolicies, function (field) {
                field.index = checked;
            });
            this.isChecked = checked;
            if (checked) {
                this.totalChecked = self.model.table.fieldPolicies.length;
            }
            else {
                this.totalChecked = 0;
            }
            this.markChecked(self);
        };
        ;
        IndexCheckAll.prototype.setup = function (self) {
            self.indexCheckAll.totalChecked = 0;
            _.each(self.model.table.fieldPolicies, function (field) {
                if (field.index) {
                    self.indexCheckAll.totalChecked++;
                }
            });
            self.indexCheckAll.markChecked(self);
        };
        return IndexCheckAll;
    }(CheckAll));
    exports.IndexCheckAll = IndexCheckAll;
});
//# sourceMappingURL=DefineFeedDetailsCheckAll.js.map