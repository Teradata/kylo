var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "angular"], function (require, exports, core_1, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Adds or updates domain types.
     */
    var DomainTypeDetailsComponent = /** @class */ (function () {
        function DomainTypeDetailsComponent($mdDialog, $mdToast, AccessControlService, DomainTypeDetailService, DomainTypesService, StateService) {
            var _this = this;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.AccessControlService = AccessControlService;
            this.DomainTypeDetailService = DomainTypeDetailService;
            this.DomainTypesService = DomainTypesService;
            this.StateService = StateService;
            /**
             * Indicates if the edit buttons are visible.
             */
            this.allowEdit = false;
            this.cancelSubscription = DomainTypeDetailService.cancelled.subscribe(function () { return _this.onCancel(); });
            this.deleteSubscription = DomainTypeDetailService.deleted.subscribe(function () { return _this.onDelete(); });
            this.saveSubscription = DomainTypeDetailService.saved.subscribe(function (model) { return _this.onSave(model); });
        }
        Object.defineProperty(DomainTypeDetailsComponent.prototype, "isNew", {
            /**
             * Indicates if this domain type is newly created.
             */
            get: function () {
                return (!angular.isString(this.model.id) || this.model.id.length === 0);
            },
            enumerable: true,
            configurable: true
        });
        DomainTypeDetailsComponent.prototype.$onDestroy = function () {
            this.ngOnDestroy();
        };
        DomainTypeDetailsComponent.prototype.$onInit = function () {
            this.ngOnInit();
        };
        DomainTypeDetailsComponent.prototype.ngOnDestroy = function () {
            this.cancelSubscription.unsubscribe();
            this.deleteSubscription.unsubscribe();
            this.saveSubscription.unsubscribe();
        };
        DomainTypeDetailsComponent.prototype.ngOnInit = function () {
            var _this = this;
            // Check for edit access
            this.AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowEdit = _this.AccessControlService.hasAction(_this.AccessControlService.FEEDS_ADMIN, actionSet.actions);
            });
        };
        /**
         * Cancels the current edit operation. If a new domain type is being created then redirects to the domain types page.
         */
        DomainTypeDetailsComponent.prototype.onCancel = function () {
            if (this.isNew) {
                this.StateService.FeedManager().DomainType().navigateToDomainTypes();
            }
        };
        ;
        /**
         * Deletes the current domain type.
         */
        DomainTypeDetailsComponent.prototype.onDelete = function () {
            var _this = this;
            var confirm = this.$mdDialog.confirm()
                .title("Delete Domain Type")
                .textContent("Are you sure you want to delete this domain type? Columns using this domain type will appear to have no domain type.")
                .ariaLabel("Delete Domain Type")
                .ok("Please do it!")
                .cancel("Nope");
            this.$mdDialog.show(confirm).then(function () {
                _this.DomainTypesService.deleteById(_this.model.id)
                    .then(function () {
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent("Successfully deleted the domain type " + _this.model.title)
                        .hideDelay(3000));
                    _this.StateService.FeedManager().DomainType().navigateToDomainTypes();
                }, function (err) {
                    _this.$mdDialog.show(_this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Delete Failed")
                        .textContent("The domain type " + _this.model.title + " could not be deleted. " + err.data.message)
                        .ariaLabel("Failed to delete domain type")
                        .ok("Got it!"));
                });
            });
        };
        ;
        /**
         * Saves the current domain type.
         */
        DomainTypeDetailsComponent.prototype.onSave = function (saveModel) {
            var _this = this;
            this.DomainTypesService.save(saveModel)
                .then(function (newModel) { return _this.model = newModel; }, function (err) {
                _this.DomainTypeDetailService.fail(err.data);
                _this.$mdDialog.show(_this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Save Failed")
                    .textContent("The domain type " + _this.model.title + " could not be saved. " + err.data.message)
                    .ariaLabel("Failed to save domain type")
                    .ok("Got it!"));
            });
        };
        ;
        DomainTypeDetailsComponent.$inject = ["$mdDialog", "$mdToast", "AccessControlService", "DomainTypeDetailsService", "DomainTypesService", "StateService"];
        __decorate([
            core_1.Input(),
            __metadata("design:type", Object)
        ], DomainTypeDetailsComponent.prototype, "model", void 0);
        return DomainTypeDetailsComponent;
    }());
    exports.DomainTypeDetailsComponent = DomainTypeDetailsComponent;
    angular.module(require("feed-mgr/domain-types/module-name"))
        .component("domainTypeDetailsComponent", {
        bindings: {
            model: "<"
        },
        controller: DomainTypeDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/details.component.html"
    });
});
//# sourceMappingURL=details.component.js.map