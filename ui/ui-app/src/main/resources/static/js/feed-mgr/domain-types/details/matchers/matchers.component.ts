import * as angular from "angular";

import {DomainTypeDetailsService} from "../../services/details.service";
import {AbstractSectionComponent} from "../abstract-section.component";

/**
 * Patterns section of the {@link DomainTypeDetailsComponent}.
 */
export class DomainTypeMatchersDetailsComponent extends AbstractSectionComponent {

    /**
     * Editable copy of the field name regex.
     */
    columnNameEditValue: RegExp;

    /**
     * Syntax error for the editable field name regex.
     */
    columnNameSyntaxError: Error;

    /**
     * Read-only copy of the field name regex.
     */
    columnNameValue: RegExp;

    /**
     * Editable copy of the sample values regex.
     */
    sampleDataEditValue: RegExp;

    /**
     * Syntax error for the editable sample values regex.
     */
    sampleDataSyntaxError: Error;

    /**
     * Read-only copy of the sample values regex.
     */
    sampleDataValue: RegExp;

    static readonly $inject: string[] = ["DomainTypeDetailsService"];

    constructor(DomainTypeDetailsService: DomainTypeDetailsService) {
        super(DomainTypeDetailsService);
    }

    /**
     * Updates the read-only copy when the model changes.
     */
    $onChanges() {
        this.columnNameValue = this.model.fieldNamePattern ? new RegExp(this.model.fieldNamePattern, this.model.fieldNameFlags) : null;
        this.sampleDataValue = this.model.regexPattern ? new RegExp(this.model.regexPattern, this.model.regexFlags) : null;
    }

    /**
     * Creates a copy of the model for editing.
     */
    onEdit(): void {
        super.onEdit();

        this.columnNameEditValue = this.columnNameValue;
        this.sampleDataEditValue = this.sampleDataValue;
    }

    /**
     * Saves changes to the model.
     */
    onSave() {
        this.onUpdate({
            fieldNameFlags: this.columnNameEditValue ? this.columnNameEditValue.flags : null,
            fieldNamePattern: this.columnNameEditValue ? this.columnNameEditValue.source : null,
            regexFlags: this.sampleDataEditValue ? this.sampleDataEditValue.flags : null,
            regexPattern: this.sampleDataEditValue ? this.sampleDataEditValue.source : null
        });
    }
}

angular.module(require("feed-mgr/domain-types/module-name"))
    .component("domainTypeMatchersDetails", {
        bindings: {
            allowEdit: "<",
            model: "<"
        },
        controller: DomainTypeMatchersDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/matchers/matchers.component.html"
    });
