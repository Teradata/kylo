import {Component, Injector, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {Sla} from '../sla.componment';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Feed} from '../../../../../model/feed/feed.model';
import * as _ from 'underscore';
import {PolicyInputFormService} from '../../../../../shared/field-policies-angular2/policy-input-form.service';
import * as angular from 'angular';


export class RuleType {
    name: string;
    mode: 'NEW';

    constructor() {
        this.name = '';
    }
}


@Component({
    selector: "sla-form",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/sla/details/sla-form.component.html"
})
export class SlaFormComponent implements OnInit, OnChanges {

    @Input('formGroup') slaForm: FormGroup;
    @Input('sla') editSla: Sla;
    @Input('feed') feedModel: Feed;

    /**
     * The Default Condition to be applied to the new Rule
     * REQUIRED = "AND"
     * SUFFICIENT = "OR"
     */
    ruleTypeCondition: any = 'REQUIRED';
    /**
     * flag to indicate if we should show the SLA Rule Type/Condition selection
     * @type {boolean}
     */
    addingSlaCondition: boolean = false;
    EMPTY_RULE_TYPE: RuleType = new RuleType();
    /**
     * The current Rule type that is being edited (i.e. the @ServiceLevelAgreementPolicy#name of the current edit
     * @type {null}
     */
    ruleType: FormControl;
    slaAction: FormControl = new FormControl(this.EMPTY_RULE_TYPE);

    /**
     * did the user modify the name of the sla
     * @type {boolean}
     */
    userSuppliedName: boolean = false;

    /**
     * did the user modify the desc of the sla
     * @type {boolean}
     */
    userSuppliedDescription: boolean = false;

    /**
     * The Form for validation
     * @type {{}}
     */
    slaName = new FormControl({});
    slaDescription = new FormControl({});

    private slaService: any;
    options: any;
    private allowCreate = true;
    allowEdit = true;
    private addingSlaAction = false;
    private slaActionOptions: any;
    private showActionOptions: boolean;
    isDebug = false;


    constructor(private $$angularInjector: Injector, private policyInputFormService: PolicyInputFormService) {
        this.slaService = $$angularInjector.get("SlaService");
    }

    ngOnInit(): void {
        /**
         * Load up the Metric Options for defining SLAs
         */
        this.slaService.getPossibleSlaMetricOptions().then((response: any) => {
            let currentFeedValue = null;
            if (this.feedModel != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feedModel);
            }
            this.options = this.policyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.allowCreate || this.allowEdit) {
                this.policyInputFormService.stripNonEditableFeeds(this.options);
            }
        });

        /**
         * Get all possible SLA Action Options
         */
        this.slaService.getPossibleSlaActionOptions().then((response: any) => {
            let currentFeedValue = null;
            if (this.feedModel != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feedModel);
            }
            this.slaActionOptions = this.policyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.slaActionOptions.length > 0) {
                this.showActionOptions = true;

                _.each(this.slaActionOptions, (action: any) => {
                    //validate the rules
                    this.slaService.validateSlaActionRule(action);
                });

                if (this.allowCreate || this.allowEdit) {
                    this.policyInputFormService.stripNonEditableFeeds(this.slaActionOptions);
                }

            }
            else {
                this.showActionOptions = false;
            }
        });

    }


    ngOnChanges(changes: SimpleChanges): void {
        if (this.editSla) {
            this.slaName = new FormControl(this.editSla.name, Validators.required);
            this.slaDescription = new FormControl(this.editSla.description, Validators.required);
            this.slaForm.addControl('slaName', this.slaName);
            this.slaForm.addControl('slaDescription', this.slaDescription);
        }
    }

    onPolicyInputControlsAdded(): void {
        this.validateForm2();
    }

    addNewCondition() {
        this.ruleType = new FormControl(this.EMPTY_RULE_TYPE);
        //if editing one already validate, complete it and then add the new one
        if (!this.slaForm.invalid) {
            //this will display the drop down to select the correct new rule/metric to assign to this SLA
            this.addingSlaCondition = true;
        }
    }

    private validateForm2() {
        this.validateAllFormFields(this.slaForm);
    }

    validateAllFormFields(formGroup: FormGroup) {
        Object.keys(formGroup.controls).forEach(field => {
            const control = formGroup.get(field);
            if (control instanceof FormControl) {
                (<FormControl>control).markAsTouched({ onlySelf: false });
            } else if (control instanceof FormGroup) {
                this.validateAllFormFields(control);
            }
        });
    }

    onAddSlaActionChange() {
        if (this.slaAction.value != this.EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            const newRule = angular.copy(this.slaAction.value);
            newRule.mode = 'NEW';
            //update property index
            this.policyInputFormService.updatePropertyIndex(newRule);

            newRule.editable = true;
            this.editSla.actionConfigurations.push(newRule);
            this.addingSlaAction = false;
            this.slaAction.setValue(this.EMPTY_RULE_TYPE);
        }
    }

    onAddConditionRuleTypeChange() {
        if (this.ruleType.value != this.EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            const newRule = angular.copy(this.ruleType.value);
            newRule.mode = 'NEW';
            //update property index
            this.policyInputFormService.updatePropertyIndex(newRule);

            newRule.condition = this.ruleTypeCondition;
            newRule.editable = true;
            this.editSla.rules.push(newRule);
            this.addingSlaCondition = false;
            this.ruleType.setValue(this.EMPTY_RULE_TYPE);

            if ((this.userSuppliedName == false || (this.editSla.name == '' || this.editSla.name == null))) {
                this.editSla.name = this.deriveSlaName();
            }
            if ((this.editSla.description == '' || this.editSla.description == null)) {
                this.editSla.description = this.deriveDescription();
            }
        }
    }

    private deriveSlaName() {

        let feedNamesString = null;
        const feedNames = this.policyInputFormService.getFeedNames(this.editSla.rules);
        if (feedNames.length > 0) {
            feedNamesString = feedNames.join(",");
        }
        const ruleNames = this.policyInputFormService.getRuleNames(this.editSla.rules);
        let slaName = ruleNames.join(",");
        if (feedNamesString != null) {
            slaName = feedNamesString + " - " + slaName;
        }
        return slaName;
    }

    private deriveDescription() {
        let feedNamesString = null;
        const feedNames = this.policyInputFormService.getFeedNames(this.editSla.rules);
        if (feedNames.length > 0) {
            feedNamesString = feedNames.join(",");
        }
        const ruleNames = this.policyInputFormService.getRuleNames(this.editSla.rules);
        let desc = ruleNames.join(",");
        if (feedNamesString != null) {
            desc += " for " + feedNamesString;
        }
        return desc;
    }

    onNameChange(): void {
        this.userSuppliedName = true;
    }

    onDescriptionChange(): void {
        this.userSuppliedDescription = true;
    }

    onDeleteSlaMetric(index: number) {
        //warn before delete
        this.editSla.rules.splice(index, 1);
        if (this.editSla.rules.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onDeleteSlaAction(index: number) {
        //warn before delete
        this.editSla.actionConfigurations.splice(index, 1);
        if (this.editSla.actionConfigurations.length == 0) {
            this.addingSlaAction = true;
        }
    }


}

