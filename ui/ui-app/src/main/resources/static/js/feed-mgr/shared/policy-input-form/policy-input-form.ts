import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');



    /**
     * the rule and options need to have been initialized by the PolicyInputFormService grouping
     *
     */
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                rule: '=',
                theForm: '=',
                feed: '=?',
                mode: '=', //NEW or EDIT
                onPropertyChange:"&?"
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/shared/policy-input-form/policy-input-form.html',
            controller: "PolicyInputFormController",
            link: function ($scope:any, element:any, attrs:any, controller:any) {

            }

        };
    }

    export class PolicyInputFormController {

    editChips:any;
    validateRequiredChips:any;
    theForm:any;
    queryChipSearch:any;
    transformChip:any;
    onPropertyChanged:any;
    onPropertyChange:any;
    rule:any;

    constructor(private $scope:any, private $q:any, private PolicyInputFormService:any) {

        var self = this;
        self.editChips = {};
        self.editChips.selectedItem = null;
        self.editChips.searchText = null;
        self.validateRequiredChips = function (property:any) {
            return PolicyInputFormService.validateRequiredChips(self.theForm, property);
        }
        self.queryChipSearch = PolicyInputFormService.queryChipSearch;
        self.transformChip = PolicyInputFormService.transformChip;

        self.onPropertyChanged = function(property:any){
            if(self.onPropertyChange != undefined && angular.isFunction(self.onPropertyChange)){
                self.onPropertyChange()(property);
            }
        }
        //call the onChange if the form initially sets the value
        if(self.onPropertyChange != undefined && angular.isFunction(self.onPropertyChange)) {
            _.each(self.rule.properties, function (property:any) {
                if ((property.type == 'select' || property.type =='feedSelect' || property.type == 'currentFeed') && property.value != null) {
                    self.onPropertyChange()(property);
                }
            });
        }

    };


    
}


angular.module(moduleName).controller('PolicyInputFormController', ["$scope","$q","PolicyInputFormService",PolicyInputFormController]);

angular.module(moduleName)
    .directive('thinkbigPolicyInputForm', directive);
