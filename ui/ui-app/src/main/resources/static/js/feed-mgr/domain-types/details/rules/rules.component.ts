import * as angular from "angular";

import {DomainType} from "../../../services/DomainTypesService";
import {DomainTypeDetailsService} from "../../services/details.service";
import {AbstractSectionComponent} from "../abstract-section.component";

/**
 * Properties section of the {@link DomainTypeDetailsComponent}.
 */
export class DomainTypeRulesDetailsComponent extends AbstractSectionComponent {

    /**
     * Standard data types for column definitions
     */
    availableDefinitionDataTypes: string[] = [];

    /**
     * Metadata for the tags.
     */
    tagChips: any = {searchText: null, selectedItem: null};

    static readonly $inject: string[] = ["$mdDialog", "DomainTypeDetailsService", "FeedFieldPolicyRuleService", "FeedService"];

    constructor(private $mdDialog: angular.material.IDialogService, DomainTypeDetailsService: DomainTypeDetailsService, private FeedFieldPolicyRuleService: any, FeedService: any) {
        super(DomainTypeDetailsService);
        this.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
    }

    /**
     * Gets a list of all field policies for the specified domain type.
     */
    getAllFieldPolicies(domainType: DomainType) {
        return this.FeedFieldPolicyRuleService.getAllPolicyRules(domainType.fieldPolicy);
    }

    /**
     * Indicates if the specified domain type has any field policies.
     */
    hasFieldPolicies(domainType: DomainType) {
        return (domainType.fieldPolicy.standardization.length > 0 || domainType.fieldPolicy.validation.length > 0);
    }

    /**
     * Updates the field when the data type is changed.
     */
    onDataTypeChange() {
        if (this.editModel.field.derivedDataType !== "decimal") {
            this.editModel.field.precisionScale = null;
        }
    }

    /**
     * Creates a copy of the domain type model for editing.
     */
    onEdit() {
        super.onEdit();

        // Ensure tags is array
        if (!angular.isObject(this.editModel.field)) {
            this.editModel.field = {};
        }
        if (!angular.isArray(this.editModel.field.tags)) {
            this.editModel.field.tags = [];
        }
    }

    /**
     * Saves changes to the model.
     */
    onSave() {
        this.onUpdate({
            field: angular.copy(this.editModel.field),
            fieldPolicy: angular.copy(this.editModel.fieldPolicy)
        });
    }

    /**
     * Shows the dialog for updating field policy rules.
     */
    showFieldRuleDialog(domainType: DomainType) {
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
    }

    /**
     * Transforms the specified chip into a tag.
     * @param chip - the chip
     * @returns the tag
     */
    transformChip = function (chip: string) {
        return angular.isObject(chip) ? chip : {name: chip};
    }
}

angular.module(require("feed-mgr/domain-types/module-name"))
    .component("domainTypeRulesDetails", {
        bindings: {
            allowEdit: "<",
            model: "<"
        },
        controller: DomainTypeRulesDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/rules/rules.component.html"
    });
