import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";;

    export class PolicyInputFormController {

    editChips:any={};
    theForm:any;
    queryChipSearch:any;
    transformChip:any;
    onPropertyChange:any;
    rule:any;
    feed:any;
    mode:any;

    ngOnInit(): void {
        this.editChips.selectedItem = null;
        this.editChips.searchText = null;
        
        this.queryChipSearch = this.PolicyInputFormService.queryChipSearch;
        this.transformChip = this.PolicyInputFormService.transformChip;

        //call the onChange if the form initially sets the value
        if(this.onPropertyChange != undefined && angular.isFunction(this.onPropertyChange)) {
            _.each(this.rule.properties, (property:any) => {
                if ((property.type == 'select' || property.type =='feedSelect' || property.type == 'currentFeed') && property.value != null) {
                    this.onPropertyChange()(property);
                }
            });
        }
    }

    $onInit(): void {
        this.ngOnInit();
    }

    static readonly $inject = ["$scope","$q","PolicyInputFormService"];
    constructor(private $scope:IScope, private $q:angular.IQService, private PolicyInputFormService:any) {
    };

    onPropertyChanged = (property:any) => {
        if(this.onPropertyChange != undefined && angular.isFunction(this.onPropertyChange)){
            this.onPropertyChange()(property);
        }
    }
    validateRequiredChips = (property:any) => {
        return this.PolicyInputFormService.validateRequiredChips(this.theForm, property);
    }


    
}

angular.module(moduleName)
    .component('thinkbigPolicyInputForm', {
        controller : PolicyInputFormController,
        bindings: {
            rule: '=',
            theForm: '=',
            feed: '=?',
            mode: '=', //NEW or EDIT
            onPropertyChange:"&?"
        },
        controllerAs: 'vm',
        templateUrl: './policy-input-form.html',
    });
