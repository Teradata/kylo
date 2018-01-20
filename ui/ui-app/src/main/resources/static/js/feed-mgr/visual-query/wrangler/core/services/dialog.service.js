var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "@covalent/core", "../columns/date-format.component", "rxjs/add/operator/filter"], function (require, exports, core_1, core_2, date_format_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Opens modal dialogs for alerting the user or receiving user input.
     */
    var WranglerDialogService = /** @class */ (function () {
        function WranglerDialogService(dialog) {
            this.dialog = dialog;
        }
        /**
         * Opens a modal dialog for the user to input a date format string.
         *
         * @param config - dialog configuration
         * @returns the date format string
         */
        WranglerDialogService.prototype.openDateFormat = function (config) {
            return this.dialog.open(date_format_component_1.DateFormatDialog, { data: config, panelClass: "full-screen-dialog" })
                .afterClosed()
                .filter(function (value) { return typeof value !== "undefined"; });
        };
        WranglerDialogService = __decorate([
            core_1.Injectable(),
            __metadata("design:paramtypes", [core_2.TdDialogService])
        ], WranglerDialogService);
        return WranglerDialogService;
    }());
    exports.WranglerDialogService = WranglerDialogService;
});
//# sourceMappingURL=dialog.service.js.map