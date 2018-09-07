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
    slaForm: FormGroup = new FormGroup({});
    slaName = new FormControl({});
    slaDescription = new FormControl({});

    private slaService: any;
    options: any;
    private allowCreate = true;
    allowEdit = true;
    private feed: null;


    constructor(private $$angularInjector: Injector, private policyInputFormService: PolicyInputFormService) {
        console.log('constructor');
        this.slaService = $$angularInjector.get("SlaService");

    }

    ngOnInit(): void {
        console.log('ngOnInit, editSla', this.editSla);

        /**
         * Load up the Metric Options for defining SLAs
         */
        this.slaService.getPossibleSlaMetricOptions().then((response: any) => {
            let currentFeedValue = null;
            if (this.feed != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feed);
            }
            this.options = this.policyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.allowCreate || this.allowEdit) {
                this.policyInputFormService.stripNonEditableFeeds(this.options);
            }

        });
    }


    ngOnChanges(changes: SimpleChanges): void {
        console.log('ngOnChanges, editSla', this.editSla);

        if (this.editSla) {
            this.slaName = new FormControl(this.editSla.name, Validators.required);
            this.slaDescription = new FormControl(this.editSla.description, Validators.required);
            this.slaForm.addControl('slaName', this.slaName);
            this.slaForm.addControl('slaDescription', this.slaDescription);
        }
    }

    addNewCondition() {
        this.ruleType = new FormControl(this.EMPTY_RULE_TYPE);
        //if editing one already validate, complete it and then add the new one
        let valid = true;
        if (this.editSla != null) {
            valid = this.validateForm();
        }
        if (valid) {
            //this will display the drop down to select the correct new rule/metric to assign to this SLA
            this.addingSlaCondition = true;
        }
    }

    /**
     * Validate the form before adding/editing a Rule for an SLA
     * @returns {boolean}
     */
    private validateForm() {
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        const ruleProperties: any[] = [];
        _.each(this.editSla.rules, function (rule: any) {
            _.each(rule.properties, function (property: any) {
                ruleProperties.push(property);
            });
        });

        return this.policyInputFormService.validateForm(this.slaForm, ruleProperties, true);
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
        this.validateForm();
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


}

