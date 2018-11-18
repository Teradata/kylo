import {Component, Injector, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {Sla} from '../model/sla.model';
import {AbstractControl, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {Feed} from '../..//model/feed/feed.model';
import * as _ from 'underscore';
import {PolicyInputFormService} from '../../../../lib/feed-mgr/shared/field-policies-angular2/policy-input-form.service';
import * as angular from 'angular';
import {FormMode, RuleType} from './sla-details.componment';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {SlaService} from "../../services/sla.service";
import {FormGroupUtil} from "../../../services/form-group-util";


export function nonEmptyValidator(): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
        const controls = (<FormGroup>control).controls;
        const isUndefined = controls === undefined;
        const isEmpty = _.keys(controls).length == 0;
        return isUndefined || isEmpty ? {'empty': {}} : null;
    };
}

@Component({
    selector: "sla-form",
    styleUrls: ["./sla-form.component.scss"],
    templateUrl: "./sla-form.component.html"
})
export class SlaFormComponent implements OnInit, OnChanges {

    private static conditionsLoader: string = "SlaFormComponent.conditionsLoader";

    @Input('formGroup') slaForm: FormGroup;
    @Input('sla') editSla: Sla;
    @Input('feed') feedModel: Feed;
    @Input('mode') mode: FormMode;
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
    slaConditions: FormGroup = new FormGroup({}, nonEmptyValidator());
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
    slaName: FormControl;
    slaDescription: FormControl;

    private stateService: any;
    options: any;
    private allowCreate = true;
    allowEdit = true;
    private addingSlaAction = false;
    private slaActionOptions: any;
    private showActionOptions = false;
    isDebug = false;
    newMode = FormMode.ModeNew;
    private loadingConditions: boolean;


    constructor(private $$angularInjector: Injector, private policyInputFormService: PolicyInputFormService, private loadingService: TdLoadingService, private slaService:SlaService) {

        this.stateService = $$angularInjector.get("StateService");

        this.createLoader(SlaFormComponent.conditionsLoader);

        this.slaName = new FormControl('', Validators.required);
        this.slaName.valueChanges.subscribe(value => {
            this.editSla.name = value;
            this.userSuppliedName = true;
        });
        this.slaDescription = new FormControl('', Validators.required);
        this.slaDescription.valueChanges.subscribe(value => {
            this.editSla.description = value;
            this.userSuppliedDescription = true;
        });
    }

    ngOnInit(): void {

        this.slaForm.addControl('conditions', this.slaConditions);
        this.slaForm.addControl('name', this.slaName);
        this.slaForm.addControl('description', this.slaDescription);
        this.checkSlaForm();
        /**
         * Load up the Metric Options for defining SLAs
         */
        this.loadingService.register(SlaFormComponent.conditionsLoader);
        this.loadingConditions = true;
        this.slaService.getPossibleSlaMetricOptions().then((response: any) => {
            let currentFeedValue = null;
            if (this.feedModel != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feedModel);
            }
            this.options = this.policyInputFormService.groupPolicyOptions(response, currentFeedValue);
            if (this.allowCreate || this.allowEdit) {
                this.policyInputFormService.stripNonEditableFeeds(this.options);
            }
            this.loadingService.resolve(SlaFormComponent.conditionsLoader);
            this.loadingConditions = false;
        }, (err: any) => {
            this.loadingService.resolve(SlaFormComponent.conditionsLoader);
            this.loadingConditions = false;
        });

        /**
         * Get all possible SLA Action Options
         */
        this.slaService.getPossibleSlaActionOptions().then((response: any) => {
            let currentFeedValue = null;
            if (this.feedModel != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feedModel);
            }
            this.slaActionOptions = this.policyInputFormService.groupPolicyOptions(response, currentFeedValue);
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

    checkSlaForm(){
        if (this.editSla) {
            if(!this.editSla.editable) {
                FormGroupUtil.disableFormControls(this.slaForm);
            }
        }
    }




    ngOnChanges(changes: SimpleChanges): void {
        if (this.editSla) {
            this.slaName.setValue(this.editSla.name);
            this.slaDescription.setValue(this.editSla.description);
            this.checkSlaForm();
        }
    }

    onPolicyInputControlsAdded(): void {
        this.validateForm2();
        this.checkSlaForm();
    }

    addNewCondition() {
        this.ruleType = new FormControl(this.EMPTY_RULE_TYPE);
        this.addingSlaCondition = true;
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
            newRule.mode = FormMode.ModeNew;
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
            newRule.mode = FormMode.ModeNew;
            //update property index
            this.policyInputFormService.updatePropertyIndex(newRule);

            newRule.condition = this.ruleTypeCondition;
            newRule.editable = true;
            this.editSla.rules.push(newRule);
            this.addingSlaCondition = false;
            this.ruleType.setValue(this.EMPTY_RULE_TYPE);

            if ((this.userSuppliedName == false || (this.slaName.value == '' || this.slaName.value == null))) {
                this.slaName.setValue(this.deriveSlaName());
            }
            if ((this.userSuppliedDescription == false || (this.slaDescription.value == '' || this.slaDescription.value == null))) {
                this.slaDescription.setValue(this.deriveDescription());
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


    viewSlaAssessments() {
        if (this.editSla) {
            this.stateService.OpsManager().Sla().navigateToServiceLevelAssessments('slaId==' + this.editSla.id);
        }
    }

    private createLoader(name: string) {
        this.loadingService.create({
            name: name,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }


}

