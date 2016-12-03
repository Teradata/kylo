(function () {

    var controller = function ($scope, $mdDialog, $mdToast, $http, StateService, FeedService, PolicyInputFormService, FieldPolicyRuleOptionsFactory, feed, field, policyParameter) {
        $scope.feed = feed;
        $scope.options = [];
        $scope.field = field;
        $scope.ruleMode = 'NEW'

        $scope.policyForm = {};

        $scope.loadingPolicies = true;
        $scope.options = [];
        FieldPolicyRuleOptionsFactory.getOptionsForType(policyParameter).then(function (response) {
            var currentFeedValue = null;
            if ($scope.feed != null) {
                currentFeedValue = PolicyInputFormService.currentFeedValue($scope.feed);
                currentFeedValue = currentFeedValue.toLowerCase();
            }
            var results = [];
            if (response.data) {
                results = _.sortBy(response.data, function (r) {
                    return r.name;
                });
            }
            $scope.options = PolicyInputFormService.groupPolicyOptions(results, currentFeedValue);
            ruleTypesAvailable();
            $scope.loadingPolicies = false;
        });

        var arr = field[policyParameter];

        if (arr != null && arr != undefined) {
            $scope.policyRules = angular.copy(arr);
        }

        function findRuleType(ruleName) {
            return _.find($scope.options, function (opt) {
                return opt.name == ruleName;
            });
        }

        function ruleTypesAvailable() {
            if ($scope.editRule != null) {
                $scope.ruleType = findRuleType($scope.editRule.name);
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
        $scope.title = modeText + " " + FieldPolicyRuleOptionsFactory.getTitleForType(policyParameter);

        $scope.addText = 'ADD RULE';
        $scope.cancelText = 'CANCEL EDIT';

        function _cancelEdit() {
            $scope.editMode = 'NEW';
            $scope.addText = 'ADD RULE';
            $scope.cancelText = 'CANCEL ADD';
            $scope.ruleType = null;
            $scope.editRule = null;
        }

        /**
         * when canceling a pending edit
         * @param $event
         */
        $scope.cancelEdit = function ($event) {
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

        $scope.deletePolicyByIndex = function ($index) {
            if ($scope.policyRules != null) {
                $scope.policyRules.splice($index, 1);
            }
            $scope.pendingEdits = true;
            _cancelEdit();
        }

        $scope.deletePolicy = function () {
            var index = $scope.editIndex;
            if ($scope.policyRules != null && index != null) {
                $scope.policyRules.splice($index, 1);
            }
            $scope.pendingEdits = true;
            _cancelEdit();
            //  $mdDialog.hide('done');
        }

        $scope.editPolicy = function ($event, index, rule) {
            if ($scope.editMode == 'EDIT') {
                _cancelEdit();
            }
            $scope.editMode = 'EDIT';
            $scope.addText = 'SAVE EDIT';
            $scope.editIndex = index;
            //get a copy of the saved rule
            var editRule = angular.copy($scope.policyRules[index]);
            //copy the rule from options with all the select options
            var startingRule = angular.copy(_.find($scope.options,function(optRule) { return optRule.name == editRule.name}));
            //reset the values
            _.each(startingRule.properties,function(ruleProperty){
                var editRuleProperty =_.find(editRule.properties,function(editProperty){
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
            var match = _.find($scope.options, function (option) {
                return option.name == rule.name;
            })
            $scope.ruleType = angular.copy(match);

        }

        $scope.done = function ($event) {
            field[policyParameter] = $scope.policyRules;
            $mdDialog.hide('done');
        }

        $scope.addPolicy = function ($event) {

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

        $scope.hide = function ($event) {
            _cancelEdit();
            $mdDialog.hide();
        };

        $scope.cancel = function ($event) {
            _cancelEdit();
            $mdDialog.hide();
        };

    };

    angular.module(MODULE_FEED_MGR).controller('FeedFieldPolicyRuleDialogController', controller);

}());

angular.module(MODULE_FEED_MGR).factory('FieldPolicyRuleOptionsFactory', function ($http, $q, RestUrlService) {

    function getStandardizationOptions() {
        return $http.get(RestUrlService.AVAILABLE_STANDARDIZATION_POLICIES, {cache: true});
    }

    function getValidationOptions() {
        return $http.get(RestUrlService.AVAILABLE_VALIDATION_POLICIES, {cache: true});
    }

    function getParserOptions() {
        return $http.get(RestUrlService.LIST_FILE_PARSERS, {cache: true});
    }

    var data = {
        standardizationOptions: [],
        validationOptions: [],
        getTitleForType: function (type) {
            if (type == 'standardization') {
                return "Standardization Policies";
            }
            else if (type == 'validation') {
                return 'Validation Policies';
            } else if (type == 'schemaParser') {
                return 'Supported Parsers'
            }

        },
        getOptionsForType: function (type) {
            if (type == 'standardization') {
                return getStandardizationOptions();
            }
            else if (type == 'validation') {
                return getValidationOptions();
            }
            else if (type == 'schemaParser') {
                return getParserOptions();
            }
        }
    };
    return data;

});