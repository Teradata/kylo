import * as _ from "underscore";
import {FeedFieldPolicyDialogData} from "./feed-field-policy-dialog-data";
import {FieldPolicyOptionsService} from "../field-policies-angular2/field-policy-options.service";
import {Component, Inject, OnDestroy, OnInit, ChangeDetectionStrategy} from "@angular/core";
import {PolicyInputFormService} from "../field-policies-angular2/policy-input-form.service";
import {FormGroup} from "@angular/forms";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatRadioChange} from "@angular/material/radio";
import {MatSelectChange} from "@angular/material/select";


interface ViewText {
    modeText:string;
    title:string;
    titleText:string;
    addText:string;
    cancelText:string;
}

@Component({
    selector:"feed-field-policy-rules-dialog",
    templateUrl: "js/feed-mgr/shared/feed-field-policy-rules/feed-field-policy-rules-dialog.component.html"
})
export class FeedFieldPolicyRulesDialogComponent implements OnInit,OnDestroy{

    private loading = false;

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


    pendingEdits :boolean = false;


    editRule:any = this.emptyRule();
    /**
     * is this needed?  dup of selectedOptionType?
     * @type {null}
     */
    ruleType :any = null;

    editIndex :number = null;

    editMode:string = 'NEW';

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



    viewText :ViewText = {modeText:"Add",title:"Add Field Policies",titleText:"Add a new Policy", addText:"ADD RULE", cancelText:"CANCEL ADD"}

    constructor(
        public fieldPolicyOptionsService: FieldPolicyOptionsService,
        public dialogRef: MatDialogRef<FeedFieldPolicyRulesDialogComponent>,
        private policyInputFormService :PolicyInputFormService,
        @Inject(MAT_DIALOG_DATA) public data: FeedFieldPolicyDialogData) {
        this.policyForm = new FormGroup({});

        this.policyForm.statusChanges.subscribe(status => {
            this.policyFormIsValid = status == "VALID";
        })
    }
    
    ngOnInit(){

        this.buildStandardizersAndValidators();
        this.setupPoliciesForFeed();
    }

    ngOnDestroy() {

    }

    emptyRule():any {
        return {name:"",groups:[],editable:false};
    }

    onChangedOptionType(event:MatRadioChange){
        let type:string = event.value;
        this._changedOptionType(type);
    }


    private _changedOptionType(type: string) {
        this.options = type == FieldPolicyOptionsService.STANDARDIZATION ? this.standardizers : this.validators;
        this.selectedOptionType = type;
    }

    compareRuleTypes(o1: any, o2: any): boolean {
        if (o1 != null && o2 != null) {
            return o1.name === o2.name;
        }
        else if(o1 == null && o2 == null){
            return true;
        }
        else {
            return false;
        }
    }

      onRuleTypeChange(change:MatSelectChange) {
        if(this.selectedRuleTypeName && this.selectedOptionType) {
            let ruleType :any = this.findRuleType(this.selectedRuleTypeName, this.selectedOptionType);

            if(ruleType) {
                var rule :any = Object.assign({},ruleType);
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

    onRuleTypeChange2(change:MatSelectChange) {
        this.ruleType = change.value;
            if (this.ruleType != null) {
                var rule :any = Object.assign({},this.ruleType);
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

    /**
     * when canceling a pending edit
     */
     cancelEdit() {
        this.editMode = 'NEW';
        this.viewText.addText = 'ADD RULE';
        this.viewText.cancelText = 'CANCEL ADD';
        this.viewText.titleText = 'Add a new policy';

        this.ruleType = null;
        this.editRule = this.emptyRule();
        this.editRuleSelected = false;
    }


    /**
     * When a policy is reordered
     * @param $index
     */
    onMovedPolicyRule($index: any) {
        this.policyRules.splice($index, 1);
        this.moved = true;
        this.pendingEdits = true;
     //resequence
        this.policyRules.forEach((rule:any,index:number) =>rule.sequence = index);

    }


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


    editPolicy(index: number, rule: any) {
        if (this.editMode == 'EDIT') {
            this.cancelEdit();
        }
        this.editMode = 'EDIT';
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

    }

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

    private validateForm(){
        return true;
    }

    addPolicy(){

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
            this.cancelEdit();
        }
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
            this.policyRules = this.deepCopy(arr);
        }
    }

    private deepCopy(obj:any):any {
        return JSON.parse(JSON.stringify( obj ));
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

    onCancelClick(): void {
        this.cancelEdit();
        this.dialogRef.close();
    }


    private    getAllPolicyRules(field: TableFieldPolicy) {
        if (field === undefined) {
            return [];
        }
        var arr = [];

        var standardizers = field['standardization'];
        var validators = field['validation'];

        //add in the type so we know what we are dealing with
        if (standardizers) {
            _.each(standardizers, (item: any) => {
                item.type = 'standardization';
            });
        }

        if (validators) {
            _.each(validators, (item: any) => {
                item.type = 'validation';
            });
        }

        var tmpArr = _.union(standardizers, validators);

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