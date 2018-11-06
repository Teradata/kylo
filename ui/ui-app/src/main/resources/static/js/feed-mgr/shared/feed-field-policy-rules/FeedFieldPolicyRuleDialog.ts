import * as _ from "underscore";
import * as angular from "angular";
import { FeedService } from '../../services/FeedService';
import { PolicyInputFormService } from '../policy-input-form/PolicyInputFormService';
import { Injectable, Component, Inject } from '@angular/core';
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { CloneUtil } from "../../../common/utils/clone-util";
import StateService from "../../../services/StateService";
import { RestUrlService } from "../../services/RestUrlService";
import { HttpClient } from "@angular/common/http";
import {moduleName} from "../../module-name";
import { downgradeInjectable } from "@angular/upgrade/static";
import { FieldPolicyRuleOptionsFactory } from "./services/FieldPolicyRuleOptionsFactory";
import { FeedFieldPolicyRuleService } from "./services/FeedFieldPolicyRuleService";
import { FieldPolicyOptionsService } from "../field-policies-angular2/field-policy-options.service";

@Component({
    templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html'
})
export class FeedFieldPolicyRuleDialogComponent {

    feed: any;
    field: any;
    options: any = [];
    ruleMode: string = 'NEW';
    policyForm: any = {};
    loadingPolicies: boolean = true;
    policyRules: any = null;
    optionTypes: any = [{ type: 'standardization', name: 'Standardization' }, { type: 'validation', name: 'Validation' }];
    selectedOptionType: string = 'standardization';
    validators: any = [];
    standardizers: any = [];
    validatorsAndStandardizers: any = [];
    moved: boolean = false;
    onChangedOptionType: any = null;

    pendingEdits: boolean = false;
    editRule: any = null;
    ruleType: any = null;
    editIndex: any = null;
    editMode: string = 'NEW';
    modeText: string = "Add";
    title: string =this. modeText + " Field Policies";
    titleText: string = 'Add a new policy';
    addText: string = 'ADD RULE';
    cancelText: string = 'CANCEL ADD';

    constructor(private StateService: StateService,
                private FeedService: FeedService,
                private PolicyInputFormService: PolicyInputFormService, 
                private dialogRef: MatDialogRef<FeedFieldPolicyRuleDialogComponent>,
                @Inject(MAT_DIALOG_DATA) private data: any,
                private fieldPolicyRuleOptionsFactory: FieldPolicyOptionsService, 
                private feedFieldPolicyRuleService: FeedFieldPolicyRuleService) {}

    ngOnInit() {
        this.feed = this.data.feed;
        this.field = this.data.field;


        this.fieldPolicyRuleOptionsFactory.getStandardizersAndValidators().subscribe((response: any[]) => {
            var currentFeedValue = null;
            if (this.feed != null) {
                currentFeedValue = this.PolicyInputFormService.currentFeedValue(this.feed);
                currentFeedValue = currentFeedValue.toLowerCase();
            }
            var standardizationResults = [];
            var validationResults = [];
            if (response ) {
                standardizationResults = _.sortBy(response[0], (r) => {
                    return r.name;
                });

                _.each(standardizationResults, (result) => {
                    result.type = 'standardization';
                })
            }

            if (response) {
                validationResults = _.sortBy(response[1], (r) => {
                    return r.name;
                });
                _.each(validationResults, (result) => {
                    result.type = 'validation';
                });
            }
            this.standardizers = this.PolicyInputFormService.groupPolicyOptions(standardizationResults, currentFeedValue);
            this.validators = this.PolicyInputFormService.groupPolicyOptions(validationResults, currentFeedValue);
            this.validatorsAndStandardizers = _.union(this.validators, this.standardizers);
            //set the correct options in the drop down
            this.changedOptionType(this.selectedOptionType);

            this.ruleTypesAvailable();
            this.loadingPolicies = false;
        });


        this.onChangedOptionType = this.changedOptionType;
        this.setupPoliciesForFeed();

        if (this.editMode == 'EDIT') {
           this.modeText = "Edit";
        }

        /*if(this.policyRules != null && this.policyRules.length  && this.policyRules.length >0 ){
         modeText = "Edit";
         }
         */
        

    }


    changedOptionType(type: any) {
        this.options = type == 'standardization' ? this.standardizers : this.validators;
        this.selectedOptionType = type;
    }


    setupPoliciesForFeed() {
        var arr = this.feedFieldPolicyRuleService.getAllPolicyRules(this.field);
        if (arr != null && arr != undefined) {
            this.policyRules = CloneUtil.deepCopy(arr);
        }
    }
    
    findRuleType(ruleName: any, type: any) {
        return _.find(this.validatorsAndStandardizers, (opt: any) => {
            return opt.name == ruleName && opt.type == type;
        });
    }

    ruleTypesAvailable() {
        if (this.editRule != null) {
            this.ruleType = this.findRuleType(this.editRule.name, this.editRule.type);
            if (this.ruleType && this.ruleType.type != this.selectedOptionType) {
                this.changedOptionType(this.ruleType.type);
            }
        }
    }

        
        

    _cancelEdit() {
        this.editMode = 'NEW';
        this.addText = 'ADD RULE';
        this.cancelText = 'CANCEL ADD';
        this.titleText = 'Add a new policy';

        this.ruleType = null;
        this.editRule = null;
    }

    resequence() {
        _.each(this.policyRules, (rule: any, i: any) => {
            rule.sequence = i;
        });

    }

    onMovedPolicyRule = ($index: any) => {
        this.policyRules.splice($index, 1);
        this.moved = true;
        this.pendingEdits = true;
        this.resequence();

    }

    /**
     * when canceling a pending edit
     * @param $event
     */
    cancelEdit = ($event: any) => {
        this._cancelEdit();

    }

    onRuleTypeChange = () => {
        if (this.ruleType != null) {
            var rule = CloneUtil.deepCopy(this.ruleType);
            rule.groups = this.PolicyInputFormService.groupProperties(rule);
            this.PolicyInputFormService.updatePropertyIndex(rule);
            //make all rules editable
            rule.editable = true;
            this.editRule = rule;
        }
        else {
            this.editRule = null;
        }
    }

    validateForm() {
        var validForm = this.PolicyInputFormService.validateForm(this.policyForm, this.editRule.properties, false);
        return validForm;
    }

    deletePolicyByIndex = ($index: any) => {
        if (this.policyRules != null) {
            this.policyRules.splice($index, 1);
        }
        this.pendingEdits = true;
        this._cancelEdit();
    }

    deletePolicy = ($index: any) => {
        var index: any = this.editIndex;
        if (this.policyRules != null && index != null) {
            this.policyRules.splice($index, 1);
        }
        this.pendingEdits = true;
        this._cancelEdit();
        //  $mdDialog.hide('done');
    }

    editPolicy = ($event: any, index: any, rule: any) => {
        if (this.editMode == 'EDIT') {
           this._cancelEdit();
        }
        this.editMode = 'EDIT';
        this.addText = 'SAVE EDIT';
        this.titleText = 'Edit the policy';
        this.editIndex = index;
        //get a copy of the saved rule
        var editRule = CloneUtil.deepCopy(this.policyRules[index]);
        //copy the rule from options with all the select options
        var startingRule = CloneUtil.deepCopy(this.findRuleType(editRule.name, editRule.type));
        //reset the values
        _.each(startingRule.properties, (ruleProperty: any) => {
            var editRuleProperty = _.find(editRule.properties, (editProperty: any) => {
                return editProperty.name == ruleProperty.name;
            });
            if (editRuleProperty != null && editRuleProperty != undefined) {
                //assign the values
                ruleProperty.value = editRuleProperty.value;
                ruleProperty.values = editRuleProperty.values;
            }
        });
            
        //reassign the editRule object to the one that has all the select values
        editRule = startingRule;

        editRule.groups = this.PolicyInputFormService.groupProperties(editRule);
        this.PolicyInputFormService.updatePropertyIndex(editRule);
        //make all rules editable
        editRule.editable = true;
        this.editRule = editRule;
        var match = this.findRuleType(rule.name, rule.type)
        this.ruleType = CloneUtil.deepCopy(match);


        if (this.ruleType && this.ruleType.type != this.selectedOptionType) {
            this.changedOptionType(this.ruleType.type);
        }
        this.selectedOptionType = editRule.type;

    }

    done = ($event: any) => {
        var validators: any = [];
        var standardizers: any = [];
        _.each(this.policyRules, (rule: any, i: any) => {
            rule.sequence = i;
            if (rule.type == 'validation') {
                validators.push(rule);
            }
            else if (rule.type == 'standardization') {
                standardizers.push(rule);
            }
        })
        this.field['validation'] = validators;
        this.field['standardization'] = standardizers;
        this.dialogRef.close('done');
    }

    addPolicy = ($event: any) => {

        var validForm = this.validateForm();
        if (validForm == true) {
            if (this.policyRules == null) {
                this.policyRules = [];
            }
            // buildDisplayString();

            this.editRule.ruleType = this.ruleType;
            if (this.editMode == 'NEW') {
                this.policyRules.push(this.editRule);
            }
            else if (this.editMode == 'EDIT') {
                this.policyRules[this.editIndex] = this.editRule;

            }

            this.pendingEdits = true;
            this._cancelEdit();
        }
    }

    hide = ($event: any) => {
        this._cancelEdit();
        this.dialogRef.close();
    };

    cancel = ($event: any) => {
        this._cancelEdit();
        this.dialogRef.close();
    };

}
