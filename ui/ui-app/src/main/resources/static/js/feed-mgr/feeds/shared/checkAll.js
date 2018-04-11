define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var CheckAll = /** @class */ (function () {
        function CheckAll(fieldName, isChecked) {
            this.fieldName = fieldName;
            this.isChecked = isChecked;
            this.isIndeterminate = false;
            this.totalChecked = 0;
        }
        CheckAll.prototype.setup = function (editModel) {
            var _this = this;
            this.model = editModel;
            this.totalChecked = 0;
            _.each(this.model.fieldPolicies, function (field) {
                if (field[_this.fieldName]) {
                    _this.totalChecked++;
                }
            });
            this.markChecked();
        };
        CheckAll.prototype.clicked = function (checked) {
            if (checked) {
                this.totalChecked++;
            }
            else {
                this.totalChecked--;
            }
            this.markChecked();
        };
        CheckAll.prototype.markChecked = function () {
            if (angular.isDefined(this.model) && this.totalChecked == this.model.fieldPolicies.length) {
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
        CheckAll.prototype.toggleAll = function () {
            var _this = this;
            var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
            if (angular.isDefined(this.model)) {
                _.each(this.model.fieldPolicies, function (field) {
                    field[_this.fieldName] = checked;
                });
                if (checked) {
                    this.totalChecked = this.model.fieldPolicies.length;
                }
                else {
                    this.totalChecked = 0;
                }
            }
            else {
                this.totalChecked = 0;
            }
            this.markChecked();
        };
        return CheckAll;
    }());
    exports.CheckAll = CheckAll;
});
//# sourceMappingURL=checkAll.js.map