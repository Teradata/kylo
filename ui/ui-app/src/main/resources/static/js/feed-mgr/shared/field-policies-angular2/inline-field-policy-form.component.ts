import * as angular from 'angular';
import * as _ from "underscore";
import {Input, Output,Component, OnInit, OnChanges, EventEmitter} from '@angular/core';
import {FieldPolicyOptionsService} from "./field-policy-options.service";
import {PolicyInputFormService} from "../../../../lib/feed-mgr/shared/field-policies-angular2/policy-input-form.service"
import {SimpleChanges} from "@angular/core/src/metadata/lifecycle_hooks";
import {FormGroup} from "@angular/forms";

@Component({
    selector: "inline-field-policy-form",
    templateUrl: "./inline-field-policy-form.component.html"
})
export class InlinePolicyInputFormComponent implements OnInit {

    @Input()
    selectedPolicyRule:any;
    @Input()
    policyParameter: string;
    @Input()
    selectLabel: string;
    @Input()
    defaultValue: string;

    @Input()
    feed?:any

    ruleTypes : any[] = [];


    ruleMode : string = "NEW";
    @Input()
    showAdvancedOptions?:boolean = false;
    @Input()
    expandAdvancedOptions ?:boolean = false;

    @Input()
    policyForm ?:FormGroup;

    @Output()
    selectedPolicyRuleChange:EventEmitter<any> = new EventEmitter<any>();

    loadingPolicies :boolean = true;

    /**
     * the selected value
     **/
    ruleType :any = null;

    skipChangeHandler :boolean = false;

    constructor(private fieldPolicyOptionsService : FieldPolicyOptionsService, private policyInputFormService :PolicyInputFormService){

        this.onRuleTypeChange.bind(this)
    }
    ngOnInit() {
        if(this.policyForm == undefined){
            this.policyForm = new FormGroup({});
        }

        this.fieldPolicyOptionsService.getOptionsForType(this.policyParameter).subscribe( (response:any) => {
            var currentFeedValue = null;
            var results = [];
            if (response) {
                results = _.sortBy(response, function (r) {
                    return r.name;
                });
            }
            this.ruleTypes = this.policyInputFormService.groupPolicyOptions(results, currentFeedValue);
            this.ruleTypesAvailable();


            if ((this.defaultValue && (angular.isUndefined(this.selectedPolicyRule) )|| this.selectedPolicyRule== null)) {
                var defaultOption = this.ruleTypes.filter((v:any) =>{ return (v.name == this.defaultValue); })
                if (defaultOption.length > 0) {
                    this.ruleType = this.selectedPolicyRule = defaultOption[0];
                    this.onRuleTypeChange();
                }
            }
            else if(angular.isDefined(this.selectedPolicyRule)){
              //  this.skipChangeHandler = true;
                //this.ruleType = this.selectedPolicyRule
                this.ruleType = this.findRuleType(this.selectedPolicyRule.name)
                this.showAdvancedOptions = (this.selectedPolicyRule.properties && this.selectedPolicyRule.properties.length > 0);

            }

            this.loadingPolicies = false;
        });
    }



    findRuleType(ruleName:any) {
        return _.find(this.ruleTypes, function (opt:any) {
            return opt.name == ruleName;
        });
    }

    ruleTypesAvailable() {
        if (this.selectedPolicyRule != null) {
            this.ruleType = this.findRuleType(this.selectedPolicyRule.name);
        }
    }

    toggleAdvancedOptions() {
        this.expandAdvancedOptions = !this.expandAdvancedOptions;
    }


    onRuleTypeChange() {
        this.expandAdvancedOptions = false;
        this.showAdvancedOptions = false;
        if (this.ruleType != null) {
            if( !this.skipChangeHandler) {
                var rule = angular.copy(this.ruleType);
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
                //make all rules editable
                rule.editable = true;
                //reset the model
                this.selectedPolicyRule = rule;
                this.selectedPolicyRuleChange.emit(this.selectedPolicyRule)
            }
            this.showAdvancedOptions = (this.ruleType.properties && this.ruleType.properties.length > 0);
            this.skipChangeHandler = false;
        }
        else {
            this.selectedPolicyRule = null;
            this.selectedPolicyRuleChange.emit(this.selectedPolicyRule)
        }
    }

    validateForm():boolean {
        var validForm = this.policyInputFormService.validateForm(this.policyForm, this.selectedPolicyRule.properties, false);
        return validForm;
    }

    ngOnChanges(changes :SimpleChanges) {
        if(changes.selectedPolicyRule.currentValue){
         //   this.skipChangeHandler = true;
            var rule = this.selectedPolicyRule;
            rule.groups = this.policyInputFormService.groupProperties(this.selectedPolicyRule)
            this.policyInputFormService.updatePropertyIndex(rule);
            //make all rules editable
            rule.editable = true;
            this.ruleType = this.findRuleType(rule.name)
            this.showAdvancedOptions = (this.selectedPolicyRule.properties && this.selectedPolicyRule.properties.length > 0);

        }
    }
}
