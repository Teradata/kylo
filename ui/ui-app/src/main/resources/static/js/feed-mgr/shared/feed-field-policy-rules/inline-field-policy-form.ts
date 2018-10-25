import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";;



class InlineFieldPolicyFormController {

    ngModel: any;
    policyParameter: any;
    selectLabel: any;
    defaultValue: any;
    options: any[] = [];
    ruleMode: string = 'NEW';
    showAdvancedOptions: boolean = false;
    expandAdvancedOptions: boolean = false;
    policyForm: any;
    loadingPolicies : boolean = true;
    field: any;
    ruleType:any = null;
    skipChangeHandler = false;

    ngOnInit(): void {
        this.field = this.ngModel;
        this.FieldPolicyRuleOptionsFactory.getOptionsForType(this.policyParameter).then((response: any) => {
            var currentFeedValue = null;
            var results = [];
            if (response.data) {
                results = _.sortBy(response.data, (r) => {
                    return r.name;
                });
            }
            this.options = this.PolicyInputFormService.groupPolicyOptions(results, currentFeedValue);
            this.ruleTypesAvailable();

            if (this.defaultValue && (angular.isUndefined(this.field) || this.field == null)) {
                var defaultOption = this.options.filter((v: any) => { return (v.name == this.defaultValue); })
                if (defaultOption.length > 0) {
                    this.ruleType = this.field = defaultOption[0];
                    this.onRuleTypeChange();
                }
            }
            else if(angular.isDefined(this.field)){
                this.skipChangeHandler = true;
                this.ruleType = this.field
                //    PolicyInputFormService.updatePropertyIndex(rule);
                this.showAdvancedOptions = (this.field.properties && this.field.properties.length > 0);

            }
            this.loadingPolicies = false;
        });
    }

    $onInit(): void {
        this.ngOnInit();
    }

    static readonly $inject = ["FieldPolicyRuleOptionsFactory", "PolicyInputFormService"];
    constructor(private FieldPolicyRuleOptionsFactory: any, private PolicyInputFormService: any) {

    }
    validateForm = () => {
        var validForm = this.PolicyInputFormService.validateForm(this.policyForm, this.field.properties, false);
        return validForm;
    }
    onRuleTypeChange =  () => {
        this.expandAdvancedOptions = false;
        this.showAdvancedOptions = false;
        if (this.ruleType != null) {
            if( !this.skipChangeHandler) {
                var rule = angular.copy(this.ruleType);
                rule.groups = this.PolicyInputFormService.groupProperties(rule);
                this.PolicyInputFormService.updatePropertyIndex(rule);
                //make all rules editable
                rule.editable = true;
                this.ngModel = this.field = rule;

            }
            this.showAdvancedOptions = (this.ruleType.properties && this.ruleType.properties.length > 0);
            this.skipChangeHandler = false;
        }
        else {
            this.field = null;
        }
    }
    toggleAdvancedOptions = () => {
        this.expandAdvancedOptions = !this.expandAdvancedOptions;
    }
    findRuleType(ruleName: any) {
        return _.find(this.options, (opt: any) => {
            return opt.name == ruleName;
        });
    }
    ruleTypesAvailable() {
        if (this.field != null) {
            this.ruleType = this.findRuleType(this.field.name);
        }
    }
}

angular.module(moduleName)
    .component('inlineFieldPolicyForm', {
        controller: InlineFieldPolicyFormController,
        controllerAs : 'vm',
        templateUrl: './inline-field-policy-form.html',
        bindings: {
            ngModel: '=',
            policyParameter: '@',
            selectLabel: '@',
            defaultValue: '@'
        }
    });


