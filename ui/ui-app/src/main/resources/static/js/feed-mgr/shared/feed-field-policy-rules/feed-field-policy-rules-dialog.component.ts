import * as _ from "underscore";
import {FeedFieldPolicyDialogData} from "./feed-field-policy-dialog-data";
import {FieldPolicyOptionsService} from "../field-policies-angular2/field-policy-options.service";
import {Component, Inject, OnDestroy, OnInit, ChangeDetectionStrategy, Output, EventEmitter, ViewChild} from "@angular/core";
import {PolicyInputFormService} from "../../../../lib/feed-mgr/shared/field-policies-angular2/policy-input-form.service";
import {FormGroup} from "@angular/forms";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatRadioChange} from "@angular/material/radio";
import {MatSelectChange} from "@angular/material/select";
import {CloneUtil} from "../../../common/utils/clone-util";
import {PolicyInputFormComponent} from "../field-policies-angular2/policy-input-form.component";


interface ViewText {
    modeText:string;
    title:string;
    titleText:string;
    addText:string;
    cancelText:string;
}

enum EditMode {
    NEW=1, EDIT=2
}

@Component({
    selector:"feed-field-policy-rules-dialog",
    templateUrl: "./feed-field-policy-rules-dialog.component.html"
})
export class FeedFieldPolicyRulesDialogComponent implements OnInit,OnDestroy{

    loading = false;

    @ViewChild(PolicyInputFormComponent)
    policyInputForm:PolicyInputFormComponent

    /**
     * Rule Type options for the std or vaidators
     * @type {any[]}
     */
    public options:any[] = [];

    /**
     * The select option/ruleType to use
     * default to standardization
     * @type {string}
     */
    public selectedOptionType =  FieldPolicyOptionsService.STANDARDIZATION;


    optionTypes = [{ type: FieldPolicyOptionsService.STANDARDIZATION, name: 'Standardization' }, { type: FieldPolicyOptionsService.VALIDATION, name: 'Validation' }]


    /**
     * The list of available validators
     * @type {Array}
     */
    validators: any = [];

    /**
     * The list of available standardizers
     * @type {Array}
     */
    standardizers: any = [];

    /**
     * Array of all standardizers and validators
     * @type {Array}
     */
    validatorsAndStandardizers: any = [];

    /**
     * The field policies associated with the field
     * @type {null}
     */
    policyRules: any[]= null;


    public policyForm:FormGroup;

    /**
     * Any pending changes
     * @type {boolean}
     */
    pendingEdits :boolean = false;

    /**
     * The rule currently editing
     * @type {any}
     */
    editRule:any = this.emptyRule();
    /**
     * is this needed?  dup of selectedOptionType?
     * @type {null}
     */
    ruleType :any = null;

    editIndex :number = null;

    editMode:EditMode = EditMode.NEW;

    /**
     * Flag to indicate the options have been moved/reordered
     * @type {boolean}
     */
    moved = false;

    editRuleSelected:boolean;

    selectedRuleTypeName:string;

    /**
     * flag to determine if the form is valid or not
     * @type {boolean}
     */
    policyFormIsValid = false;



    viewText :ViewText = {modeText:"Add",title:"Field Policies",titleText:"Add a new policy", addText:"Add Rule", cancelText:"Cancel Add"}

    constructor(
        public fieldPolicyOptionsService: FieldPolicyOptionsService,
        public dialogRef: MatDialogRef<FeedFieldPolicyRulesDialogComponent>,
        private policyInputFormService :PolicyInputFormService,
        @Inject(MAT_DIALOG_DATA) public data: FeedFieldPolicyDialogData) {
        this.policyForm = new FormGroup({});

        this.policyForm.statusChanges.debounceTime(10).subscribe(status => {
            this.policyFormIsValid = status == "VALID";
        })
    }
    
    ngOnInit(){

        this.buildStandardizersAndValidators();
        this.setupPoliciesForFeed();
    }

    ngOnDestroy() {

    }

    /**
     * When the rule select drop down changes
     * Clone the rule and show the dynamic form
     * @param {MatSelectChange} change
     */
    onRuleTypeChange(change:MatSelectChange) {
        if(this.selectedRuleTypeName && this.selectedOptionType) {

            let ruleType :any = this.findRuleType(this.selectedRuleTypeName, this.selectedOptionType);

            if(ruleType) {
                var rule :any = CloneUtil.deepCopy(ruleType);
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
                //make all rules editable
                rule.editable = true;

                this.editRule = rule;
                this.editRuleSelected = true;

            }
            else {
                this.editRule = this.emptyRule();
                this.editRuleSelected = false;

            }
        }
    }

    /**
     * react to when the form controls are painted on the screen
     * @param controls
     */
    onFormControlsAdded(controls:any){
        this.policyForm.updateValueAndValidity();
    }

    /**
     * When the std/validator changes
     * @param {MatRadioChange} event
     */
    onChangedOptionType(event:MatRadioChange){
        let type:string = event.value;
        this._changedOptionType(type);
    }






    /**
     * When a policy is reordered
     * @param $index
     */
    onMovedPolicyRule(r: any, list: any[]) {
        list.splice(list.indexOf(r), 1);
        this.moved = true;
        this.pendingEdits = true;
        //resequence
        list.forEach((rule:any,index:number) =>rule.sequence = index);
        this.policyRules = list;
    }



    /**
     * When a user deletes a given policy
     * @param {number} $index
     */
    deletePolicy($index?: number) {
        if($index == undefined){
            $index = this.editIndex;
        }

        if (this.policyRules != null && $index != null) {
            this.policyRules.splice($index, 1);
        }
        this.pendingEdits = true;
        this.cancelEdit();
    }

    /**
     * When a user adds a new policy
     */
    addPolicy(){

        var validForm = this.validateForm();
        if (validForm == true) {
            if (this.policyRules == null) {
                this.policyRules = [];
            }
            // buildDisplayString();

            this.editRule.ruleType = this.ruleType;
            if (this.editMode == EditMode.NEW) {
                this.policyRules.push(this.editRule);
            }
            else if (this.editMode == EditMode.EDIT) {
                this.policyRules[this.editIndex] = this.editRule;
            }

            this.pendingEdits = true;
            this.cancelEdit();
        }
    }


    /**
     * When a user edits a policy
     * @param {number} index
     * @param rule
     */
    editPolicy(index: number, rule: any) {
        if (this.editMode == EditMode.EDIT) {
            this.cancelEdit();
        }
        this.editMode = EditMode.EDIT;
        this.viewText.addText ='SAVE EDIT';
        this.viewText.titleText='Edit the policy';
        this.editIndex = index;
        //get a copy of the saved rule
        var editRule = Object.assign({},this.policyRules[index]);
        //copy the rule from options with all the select options
        var startingRule = Object.assign({},this.findRuleType(editRule.name, editRule.type));
        //reset the values
        startingRule.properties.forEach((ruleProperty: any) => {
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
        editRule.groups = this.policyInputFormService.groupProperties(editRule);
        this.policyInputFormService.updatePropertyIndex(editRule);
        //make all rules editable
        editRule.editable = true;
        this.editRule = editRule;
        var match = this.findRuleType(rule.name, rule.type)
        this.ruleType = Object.assign({},match);


        if (this.ruleType && this.ruleType.type != this.selectedOptionType) {
            this._changedOptionType(this.ruleType.type);
        }
        this.selectedOptionType = editRule.type;
        this.selectedRuleTypeName = this.editRule.name;

    }

    /**
     * When the user is done adding/editing save the changes back to the model
     */
    done(){
        var validators: any = [];
        var standardizers: any = [];
        this.policyRules.forEach((rule: any, i: any) => {
            rule.sequence = i;
            if (rule.type == 'validation') {
                validators.push(rule);
            }
            else if (rule.type == 'standardization') {
                standardizers.push(rule);
            }
        })
        this.data.field['validation'] = validators;
        this.data.field['standardization'] = standardizers;
        this.dialogRef.close('done');
    }

    /**
     * when a user cancels the entire edit of the form
     */
    onCancelClick(): void {
        this.cancelEdit();
        this.dialogRef.close();
    }



    private emptyRule():any {
        return {name:"",groups:[],editable:false};
    }

    private _changedOptionType(type: string) {
        this.options = type == FieldPolicyOptionsService.STANDARDIZATION ? this.standardizers : this.validators;
        this.selectedOptionType = type;
        this.policyInputForm.resetForm();
    }




    /**
     * when canceling a pending edit
     */
    cancelEdit() {
        this.editMode = EditMode.NEW;
        this.viewText.addText = 'Add Rule';
        this.viewText.cancelText = 'Cancel Add';
        this.viewText.titleText = 'Add a new policy';

        this.ruleType = null;
        this.editRule = this.emptyRule();
        this.editRuleSelected = false;
        this.selectedRuleTypeName  = null;
    }




    private validateForm(){
        return true;
    }




    private buildStandardizersAndValidators(){
    this.loading = true;

        this.fieldPolicyOptionsService.getStandardizersAndValidators().subscribe((response: any[]) => {
            var currentFeedValue = null;
            if (this.data.feed != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.data.feed);
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
                })
            }
            this.standardizers = this.policyInputFormService.groupPolicyOptions(standardizationResults, currentFeedValue);
            this.validators = this.policyInputFormService.groupPolicyOptions(validationResults, currentFeedValue);
            this.validatorsAndStandardizers = _.union(this.validators, this.standardizers);
            //set the correct options in the drop down
            this._changedOptionType(this.selectedOptionType);

            this.ruleTypesAvailable();
            this.loading = false;
        })
        
        
    }

    private setupPoliciesForFeed() {
        var arr = this.getAllPolicyRules(this.data.field);
        if (arr != null && arr != undefined) {
            this.policyRules = CloneUtil.deepCopy(arr);
        }
    }

    private findRuleType(ruleName: any, type: any) {
        return _.find(this.validatorsAndStandardizers, (opt: any) => {
            return opt.name == ruleName && opt.type == type;
        });
    }



    private ruleTypesAvailable() {
        if (this.editRule != null) {
            this.ruleType = this.findRuleType(this.editRule.name, this.editRule.type);
            if (this.ruleType && this.ruleType.type != this.selectedOptionType) {
                this._changedOptionType(this.ruleType.type);
            }
        }
    }



    private  getAllPolicyRules(field: TableFieldPolicy) {
        if (field === undefined) {
            return [];
        }
        var arr = [];

        var standardizers = field['standardization'];
        var validators = field['validation'];

        //add in the type so we know what we are dealing with
        let tmpArr :any[] = [];
        if (standardizers) {
            standardizers.forEach((item:any,i:number) => {
                item.type = 'standardization';
                tmpArr[i] = item;
            })
        }
        let idx = tmpArr.length>0 ? tmpArr.length-1 : 0;
        if (validators) {
            validators.forEach((item: any, i: number) => {
                item.type = 'validation';
                tmpArr[idx+i] = item;
            });
        }


        var hasSequence = _.find(tmpArr, (item: any) => {
            return item.sequence != null && item.sequence != undefined;
        }) !== undefined;

        //if we dont have a sequence, add it in
        if (!hasSequence) {
            _.each(tmpArr, (item: any, idx: any) => {
                item.sequence = idx;
            });
        }

        arr = _.sortBy(tmpArr, 'sequence');
        return arr;
    }

}
