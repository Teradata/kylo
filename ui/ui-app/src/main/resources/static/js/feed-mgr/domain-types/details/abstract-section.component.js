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
     * Base class for individual sections of the {@link DomainTypeDetailsComponent}.
     */
    var AbstractSectionComponent = /** @class */ (function () {
        function AbstractSectionComponent(DomainTypeDetailsService) {
            var _this = this;
            this.DomainTypeDetailsService = DomainTypeDetailsService;
            /**
             * Domain type model for the edit view.
             */
            this.editModel = {};
            /**
             * Indicates if the edit view is displayed.
             */
            this.isEditable = false;
            this.failureSubscription = this.DomainTypeDetailsService.failed.subscribe(function (model) { return _this.onFailure(model); });
        }
        Object.defineProperty(AbstractSectionComponent.prototype, "isNew", {
            /**
             * Indicates if this domain type is newly created.
             */
            get: function () {
                return (!angular.isString(this.model.id) || this.model.id.length === 0);
            },
            enumerable: true,
            configurable: true
        });
        AbstractSectionComponent.prototype.$onDestroy = function () {
            this.ngOnDestroy();
        };
        AbstractSectionComponent.prototype.$onInit = function () {
            this.ngOnInit();
        };
        AbstractSectionComponent.prototype.ngOnDestroy = function () {
            this.failureSubscription.unsubscribe();
        };
        AbstractSectionComponent.prototype.ngOnInit = function () {
            if (this.isNew) {
                this.onEdit();
                this.isEditable = true;
            }
        };
        /**
         * Cancels edit mode and switches to read-only mode.
         */
        AbstractSectionComponent.prototype.onCancel = function () {
            this.DomainTypeDetailsService.cancelEdit(this.model);
        };
        /**
         * Deletes the domain type.
         */
        AbstractSectionComponent.prototype.onDelete = function () {
            this.DomainTypeDetailsService.deleteDomainType(this.model);
        };
        /**
         * Creates a copy of the domain type model for editing.
         */
        AbstractSectionComponent.prototype.onEdit = function () {
            this.editModel = angular.copy(this.model);
        };
        /**
         * Called when the save operation failed.
         */
        AbstractSectionComponent.prototype.onFailure = function (model) {
            if (model.$$saveId === this.lastSaveId) {
                this.isEditable = true;
            }
        };
        /**
         * Updates the domain type with the specified properties.
         */
        AbstractSectionComponent.prototype.onUpdate = function (properties) {
            var saveModel = angular.extend(angular.copy(this.model), properties);
            this.lastSaveId = saveModel.$$saveId = new Date().getTime();
            this.DomainTypeDetailsService.saveEdit(saveModel);
        };
        __decorate([
            core_1.Input(),
            __metadata("design:type", Boolean)
        ], AbstractSectionComponent.prototype, "allowEdit", void 0);
        __decorate([
            core_1.Input(),
            __metadata("design:type", Object)
        ], AbstractSectionComponent.prototype, "model", void 0);
        return AbstractSectionComponent;
    }());
    exports.AbstractSectionComponent = AbstractSectionComponent;
});
//# sourceMappingURL=abstract-section.component.js.map