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
define(["require", "exports", "angular", "../abstract-section.component"], function (require, exports, angular, abstract_section_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Patterns section of the {@link DomainTypeDetailsComponent}.
     */
    var DomainTypeMatchersDetailsComponent = /** @class */ (function (_super) {
        __extends(DomainTypeMatchersDetailsComponent, _super);
        function DomainTypeMatchersDetailsComponent(DomainTypeDetailsService) {
            return _super.call(this, DomainTypeDetailsService) || this;
        }
        /**
         * Updates the read-only copy when the model changes.
         */
        DomainTypeMatchersDetailsComponent.prototype.$onChanges = function () {
            this.columnNameValue = this.model.fieldNamePattern ? new RegExp(this.model.fieldNamePattern, this.model.fieldNameFlags) : null;
            this.sampleDataValue = this.model.regexPattern ? new RegExp(this.model.regexPattern, this.model.regexFlags) : null;
        };
        /**
         * Creates a copy of the model for editing.
         */
        DomainTypeMatchersDetailsComponent.prototype.onEdit = function () {
            _super.prototype.onEdit.call(this);
            this.columnNameEditValue = this.columnNameValue;
            this.sampleDataEditValue = this.sampleDataValue;
        };
        /**
         * Saves changes to the model.
         */
        DomainTypeMatchersDetailsComponent.prototype.onSave = function () {
            this.onUpdate({
                fieldNameFlags: this.columnNameEditValue ? this.columnNameEditValue.flags : null,
                fieldNamePattern: this.columnNameEditValue ? this.columnNameEditValue.source : null,
                regexFlags: this.sampleDataEditValue ? this.sampleDataEditValue.flags : null,
                regexPattern: this.sampleDataEditValue ? this.sampleDataEditValue.source : null
            });
        };
        DomainTypeMatchersDetailsComponent.$inject = ["DomainTypeDetailsService"];
        return DomainTypeMatchersDetailsComponent;
    }(abstract_section_component_1.AbstractSectionComponent));
    exports.DomainTypeMatchersDetailsComponent = DomainTypeMatchersDetailsComponent;
    angular.module(require("feed-mgr/domain-types/module-name"))
        .component("domainTypeMatchersDetails", {
        bindings: {
            allowEdit: "<",
            model: "<"
        },
        controller: DomainTypeMatchersDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/matchers/matchers.component.html"
    });
});
//# sourceMappingURL=matchers.component.js.map