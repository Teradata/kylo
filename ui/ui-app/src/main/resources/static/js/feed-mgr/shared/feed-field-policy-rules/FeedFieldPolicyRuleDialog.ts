import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

export class FeedFieldPolicyRuleDialogController {

    constructor (private $scope:any, private $mdDialog:any, private $mdToast:any, private $http:any, private StateService:any, private FeedService:any
        , private PolicyInputFormService:any, private FieldPolicyRuleOptionsFactory:any, private FeedFieldPolicyRuleService:any, private feed:any, private field:any) {
        $scope.feed = feed;
        $scope.options = [];
        $scope.field = field;
        $scope.ruleMode = 'NEW'

        /**
         * The form for validation
         * @type {{}}
         */
        $scope.policyForm = {};

        /**
         * Flag if we are loading the available policies
         * @type {boolean}
         */
        $scope.loadingPolicies = true;

        /**
         * The field policies associated with the field
         * @type {null}
         */
        $scope.policyRules = null;

        /**
         *  Renders Radio selection for different types
         * @type {[*]}
         */
        $scope.optionTypes = [{type:'standardization',name:'Standardization'},{type:'validation',name:'Validation'}]

        /**
         * the selected Typed.. used with the optionTypes
         * @type {null}
         */
        $scope.selectedOptionType = 'standardization';

        /**
         * The list of either Standardizers or validators depending upon the selectedOptionType (radio button)
         * @type {Array}
         */
        $scope.options = [];

        /**
         * The list of available validators
         * @type {Array}
         */
        var validators:any = [];

        /**
         * The list of available standardizers
         * @type {Array}
         */
        var standardizers:any = [];

        /**
         * Array of all standardizers and validators
         * @type {Array}
         */
        var validatorsAndStandardizers:any = [];

        /**
         * flag to indicate the items have been re ordered/moved
         * @type {boolean}
         */
        $scope.moved = false;

        FieldPolicyRuleOptionsFactory.getStandardizersAndValidators().then(function (response:any) {
            var currentFeedValue = null;
            if ($scope.feed != null) {
                currentFeedValue = PolicyInputFormService.currentFeedValue($scope.feed);
                currentFeedValue = currentFeedValue.toLowerCase();
            }
            var standardizationResults = [];
            var validationResults = [];
            if (response.standardization && response.standardization.data) {
                standardizationResults = _.sortBy(response.standardization.data, function (r) {
                    return r.name;
                });

                _.each(standardizationResults,function(result){
                    result.type = 'standardization';
                })
            }

            if (response.validation && response.validation.data) {
                validationResults = _.sortBy(response.validation.data, function (r) {
                    return r.name;
                });

                _.each(validationResults,function(result){
                    result.type = 'validation';
                })
            }
            standardizers = PolicyInputFormService.groupPolicyOptions(standardizationResults, currentFeedValue);
            validators = PolicyInputFormService.groupPolicyOptions(validationResults, currentFeedValue);
            validatorsAndStandardizers = _.union(validators,standardizers);
            //set the correct options in the drop down
            changedOptionType($scope.selectedOptionType);

            ruleTypesAvailable();
            $scope.loadingPolicies = false;
        });


        $scope.onChangedOptionType = changedOptionType;


        function changedOptionType(type:any) {
            $scope.options = type == 'standardization' ? standardizers : validators;
            $scope.selectedOptionType = type;
        }


         function setupPoliciesForFeed(){
             var arr = FeedFieldPolicyRuleService.getAllPolicyRules(field);
             if (arr != null && arr != undefined) {
                 $scope.policyRules = angular.copy(arr);
             }
         }
        setupPoliciesForFeed();





        function findRuleType(ruleName:any, type:any) {
            return _.find(validatorsAndStandardizers, function (opt:any) {
                return opt.name == ruleName && opt.type == type;
            });
        }

        function ruleTypesAvailable() {
            if ($scope.editRule != null) {
                $scope.ruleType = findRuleType($scope.editRule.name, $scope.editRule.type);
                if($scope.ruleType && $scope.ruleType.type != $scope.selectedOptionType) {
                    changedOptionType($scope.ruleType.type);
                }
            }
        }

        $scope.pendingEdits = false;
        $scope.editRule;
        $scope.ruleType = null;
        $scope.editIndex = null;
        $scope.editMode = 'NEW';
        var modeText = "Add";
        if ($scope.editMode == 'EDIT') {
            modeText = "Edit";
        }

        /*if($scope.policyRules != null && $scope.policyRules.length  && $scope.policyRules.length >0 ){
         modeText = "Edit";
         }
         */
        $scope.title = modeText + " Field Policies";
        $scope.titleText = 'Add a new policy';
        $scope.addText = 'ADD RULE';
        $scope.cancelText = 'CANCEL ADD';

        function _cancelEdit() {
            $scope.editMode = 'NEW';
            $scope.addText = 'ADD RULE';
            $scope.cancelText = 'CANCEL ADD';
            $scope.titleText = 'Add a new policy';

            $scope.ruleType = null;
            $scope.editRule = null;
        }

        function resequence(){
            _.each($scope.policyRules,function(rule:any, i:any) {
                rule.sequence = i;
            });

        }

        $scope.onMovedPolicyRule = function ($index:any) {
            $scope.policyRules.splice($index, 1);
            $scope.moved = true;
            $scope.pendingEdits = true;
            resequence();

        }

        /**
         * when canceling a pending edit
         * @param $event
         */
        $scope.cancelEdit = function ($event:any) {
            _cancelEdit();

        }

        $scope.onRuleTypeChange = function () {
            if ($scope.ruleType != null) {
                var rule = angular.copy($scope.ruleType);
                rule.groups = PolicyInputFormService.groupProperties(rule);
                PolicyInputFormService.updatePropertyIndex(rule);
                //make all rules editable
                rule.editable = true;
                $scope.editRule = rule;
            }
            else {
                $scope.editRule = null;
            }
        }

        function validateForm() {
            var validForm = PolicyInputFormService.validateForm($scope.policyForm, $scope.editRule.properties, false);
            return validForm;
        }

        /*

         function buildDisplayString() {
         if ($scope.editRule != null) {
         var str = '';
         _.each($scope.editRule.properties, function (prop, idx) {
         if (prop.type != 'currentFeed') {
         //chain it to the display string
         if (str != '') {
         str += ';';
         }
         str += ' ' + prop.displayName;
         var val = prop.value;
         if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
         val = _.map(prop.values, function (labelValue) {
         return labelValue.value;
         }).join(",");
         }
         str += ": " + val;
         }
         });
         $scope.editRule.propertyValuesDisplayString = str;
         }
         }
         */

        $scope.deletePolicyByIndex = function ($index:any) {
            if ($scope.policyRules != null) {
                $scope.policyRules.splice($index, 1);
            }
            $scope.pendingEdits = true;
            _cancelEdit();
        }

        $scope.deletePolicy = function ($index:any) {
            var index:any = $scope.editIndex;
            if ($scope.policyRules != null && index != null) {
                $scope.policyRules.splice($index, 1);
            }
            $scope.pendingEdits = true;
            _cancelEdit();
            //  $mdDialog.hide('done');
        }

        $scope.editPolicy = function ($event:any, index:any, rule:any) {
            if ($scope.editMode == 'EDIT') {
                _cancelEdit();
            }
            $scope.editMode = 'EDIT';
            $scope.addText = 'SAVE EDIT';
            $scope.titleText = 'Edit the policy';
            $scope.editIndex = index;
            //get a copy of the saved rule
            var editRule = angular.copy($scope.policyRules[index]);
            //copy the rule from options with all the select options
            var startingRule = angular.copy(findRuleType(editRule.name, editRule.type));
            //reset the values
            _.each(startingRule.properties,function(ruleProperty:any){
                var editRuleProperty =_.find(editRule.properties,function(editProperty:any){
                    return editProperty.name == ruleProperty.name;
                });
                if(editRuleProperty != null && editRuleProperty != undefined){
                 //assign the values
                    ruleProperty.value = editRuleProperty.value;
                    ruleProperty.values = editRuleProperty.values;
                }
            });
            //reassign the editRule object to the one that has all the select values
            editRule = startingRule;







            editRule.groups = PolicyInputFormService.groupProperties(editRule);
            PolicyInputFormService.updatePropertyIndex(editRule);
            //make all rules editable
            editRule.editable = true;
            $scope.editRule = editRule;
            var match = findRuleType(rule.name, rule.type)
            $scope.ruleType = angular.copy(match);


            if($scope.ruleType && $scope.ruleType.type != $scope.selectedOptionType) {
                changedOptionType($scope.ruleType.type);
            }
            $scope.selectedOptionType = editRule.type;

        }

        $scope.done = function ($event:any) {
            var validators:any = [];
            var standardizers:any = [];
            _.each($scope.policyRules,function(rule:any, i:any) {
                rule.sequence = i;
                if(rule.type == 'validation'){
                    validators.push(rule);
                }
                else  if(rule.type == 'standardization'){
                    standardizers.push(rule);
                }
            })
            field['validation'] =validators;
            field['standardization'] =standardizers;
            $mdDialog.hide('done');
        }

        $scope.addPolicy = function ($event:any) {

            var validForm = validateForm();
            if (validForm == true) {
                if ($scope.policyRules == null) {
                    $scope.policyRules = [];
                }
                // buildDisplayString();

                $scope.editRule.ruleType = $scope.ruleType;
                if ($scope.editMode == 'NEW') {
                    $scope.policyRules.push($scope.editRule);
                }
                else if ($scope.editMode == 'EDIT') {
                    $scope.policyRules[$scope.editIndex] = $scope.editRule;

                }

                $scope.pendingEdits = true;
                _cancelEdit();
            }
        }

        $scope.hide = function ($event:any) {
            _cancelEdit();
            $mdDialog.hide();
        };

        $scope.cancel = function ($event:any) {
            _cancelEdit();
            $mdDialog.hide();
        };

    };


    
}



angular.module(moduleName).controller('FeedFieldPolicyRuleDialogController', ["$scope","$mdDialog","$mdToast","$http","StateService","FeedService","PolicyInputFormService","FieldPolicyRuleOptionsFactory", "FeedFieldPolicyRuleService","feed","field",FeedFieldPolicyRuleDialogController]);


angular.module(moduleName).factory('FieldPolicyRuleOptionsFactory', ["$http","$q","RestUrlService",function ($http:any, $q:any, RestUrlService:any) {

    function getStandardizationOptions() {
        return $http.get(RestUrlService.AVAILABLE_STANDARDIZATION_POLICIES, {cache: true});
    }

    function getValidationOptions() {
        return $http.get(RestUrlService.AVAILABLE_VALIDATION_POLICIES, {cache: true});
    }

    function getParserOptions() {
        return $http.get(RestUrlService.LIST_FILE_PARSERS, {cache: true});
    }

    function getSparkParserOptions() {
        return $http.get(RestUrlService.LIST_SPARK_FILE_PARSERS, {cache: true});
    }

    var data:any = {
        standardizationOptions: [],
        validationOptions: [],
        getTitleForType: function (type:any) {
            if (type == 'standardization') {
                return "Standardization Policies";
            }
            else if (type == 'validation') {
                return 'Validation Policies';
            } else if (type == 'schemaParser') {
                return 'Supported Parsers'
            } else if (type == 'schemaParser') {
                return 'Supported Parsers'
            }

        },
        getStandardizersAndValidators: function () {
            return this.getOptionsForType('standardization-validation');
        },
        getOptionsForType: function (type:any) {
            if (type == 'standardization-validation') {
                var defer = $q.defer();
                var requests = {validation:getValidationOptions(), standardization:getStandardizationOptions()};
                $q.all(requests).then(function(response:any){
                    defer.resolve(response);
                });
                return defer.promise;
            }
            if (type == 'standardization') {
                return getStandardizationOptions();
            }
            else if (type == 'validation') {
                return getValidationOptions();
            }
            else if (type == 'schemaParser') {
                return getParserOptions();
            }
            else if (type == 'sparkSchemaParser') {
                return getSparkParserOptions();
            }
        }
    };
    return data;
    
}]);


    angular.module(moduleName).factory('FeedFieldPolicyRuleService', [function () {


        var data = {
            getAllPolicyRules :function(field:any) {
                if (field === undefined) {
                    return [];
                }
                var arr = [];

                var standardizers =field['standardization'];
                var validators =field['validation'];

               //add in the type so we know what we are dealing with
                if(standardizers) {
                    _.each(standardizers, function (item:any) {
                        item.type = 'standardization';
                    });
                }

                if(validators) {
                    _.each(validators, function (item:any) {
                        item.type = 'validation';
                    });
                }

                var tmpArr = _.union(standardizers,validators);

                var hasSequence = _.find(tmpArr,function(item:any){
                        return item.sequence != null && item.sequence != undefined;
                    }) !== undefined;

                //if we dont have a sequence, add it in
                if(!hasSequence){
                    _.each(tmpArr,function(item:any,idx:any){
                        item.sequence = idx;
                    });
                }

                arr = _.sortBy(tmpArr,'sequence');
                return arr;
            }
        };
        return data;

    }]);
