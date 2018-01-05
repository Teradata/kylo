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
     * Properties section of the {@link DomainTypeDetailsComponent}.
     */
    var DomainTypeRulesDetailsComponent = /** @class */ (function (_super) {
        __extends(DomainTypeRulesDetailsComponent, _super);
        function DomainTypeRulesDetailsComponent($mdDialog, DomainTypeDetailsService, FeedFieldPolicyRuleService, FeedService) {
            var _this = _super.call(this, DomainTypeDetailsService) || this;
            _this.$mdDialog = $mdDialog;
            _this.FeedFieldPolicyRuleService = FeedFieldPolicyRuleService;
            /**
             * Standard data types for column definitions
             */
            _this.availableDefinitionDataTypes = [];
            /**
             * Metadata for the tags.
             */
            _this.tagChips = { searchText: null, selectedItem: null };
            /**
             * Transforms the specified chip into a tag.
             * @param chip - the chip
             * @returns the tag
             */
            _this.transformChip = function (chip) {
                return angular.isObject(chip) ? chip : { name: chip };
            };
            _this.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
            return _this;
        }
        /**
         * Gets a list of all field policies for the specified domain type.
         */
        DomainTypeRulesDetailsComponent.prototype.getAllFieldPolicies = function (domainType) {
            return this.FeedFieldPolicyRuleService.getAllPolicyRules(domainType.fieldPolicy);
        };
        /**
         * Indicates if the specified domain type has any field policies.
         */
        DomainTypeRulesDetailsComponent.prototype.hasFieldPolicies = function (domainType) {
            return (domainType.fieldPolicy.standardization.length > 0 || domainType.fieldPolicy.validation.length > 0);
        };
        /**
         * Updates the field when the data type is changed.
         */
        DomainTypeRulesDetailsComponent.prototype.onDataTypeChange = function () {
            if (this.editModel.field.derivedDataType !== "decimal") {
                this.editModel.field.precisionScale = null;
            }
        };
        /**
         * Creates a copy of the domain type model for editing.
         */
        DomainTypeRulesDetailsComponent.prototype.onEdit = function () {
            _super.prototype.onEdit.call(this);
            // Ensure tags is array
            if (!angular.isObject(this.editModel.field)) {
                this.editModel.field = {};
            }
            if (!angular.isArray(this.editModel.field.tags)) {
                this.editModel.field.tags = [];
            }
        };
        /**
         * Saves changes to the model.
         */
        DomainTypeRulesDetailsComponent.prototype.onSave = function () {
            this.onUpdate({
                field: angular.copy(this.editModel.field),
                fieldPolicy: angular.copy(this.editModel.fieldPolicy)
            });
        };
        /**
         * Shows the dialog for updating field policy rules.
         */
        DomainTypeRulesDetailsComponent.prototype.showFieldRuleDialog = function (domainType) {
            this.$mdDialog.show({
                controller: "FeedFieldPolicyRuleDialogController",
                templateUrl: "js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html",
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: null,
                    field: domainType.fieldPolicy
                }
            });
        };
        DomainTypeRulesDetailsComponent.$inject = ["$mdDialog", "DomainTypeDetailsService", "FeedFieldPolicyRuleService", "FeedService"];
        return DomainTypeRulesDetailsComponent;
    }(abstract_section_component_1.AbstractSectionComponent));
    exports.DomainTypeRulesDetailsComponent = DomainTypeRulesDetailsComponent;
    angular.module(require("feed-mgr/domain-types/module-name"))
        .component("domainTypeRulesDetails", {
        bindings: {
            allowEdit: "<",
            model: "<"
        },
        controller: DomainTypeRulesDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/rules/rules.component.html"
    });
});
//# sourceMappingURL=rules.component.js.map