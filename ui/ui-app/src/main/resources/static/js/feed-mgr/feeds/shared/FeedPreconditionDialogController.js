
define(['angular','feed-mgr/feeds/module-name'], function (angular,moduleName) {

    var controller = function($scope, $mdDialog, $mdToast, $http, StateService, FeedService, PolicyInputFormService, feed, index) {
        $scope.feed = feed;
        $scope.options = [];

        $scope.ruleMode = 'NEW';

        FeedService.getPossibleFeedPreconditions().then(function(response) {
            var currentFeedValue = null;
            if ($scope.feed != null) {
                currentFeedValue = PolicyInputFormService.currentFeedValue($scope.feed);
                currentFeedValue = currentFeedValue.toLowerCase();
            }

            $scope.options = PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            ruleTypesAvailable();
        });

        var arr = feed.schedule.preconditions;

        if (arr != null && arr != undefined) {

            $scope.preconditions = angular.copy(arr);
        }

        function findRuleType(ruleName) {
            return _.find($scope.options, function(opt) {
                return opt.name == ruleName;
            });
        }

        function ruleTypesAvailable() {
            if ($scope.editRule != null) {
                $scope.ruleType = findRuleType($scope.editRule.name);
            }
        }

        $scope.pendingEdits = false;
        $scope.editRule = null;
        $scope.ruleType = null;
        $scope.editIndex = null;
        $scope.editMode = 'NEW';
        if (index != null) {
            $scope.editMode = 'EDIT';
            $scope.editIndex = index;
            var editRule = $scope.preconditions[index];
            editRule.groups = PolicyInputFormService.groupProperties(editRule);
            PolicyInputFormService.updatePropertyIndex(editRule);
            //make all rules editable
            editRule.editable = true;
            $scope.editRule = editRule;
        }
        var modeText = "Add";
        if ($scope.editMode == 'EDIT') {
            modeText = "Edit";
        }

        $scope.title = modeText + " Precondition";

        $scope.addText = 'ADD PRECONDITION';
        $scope.cancelText = 'CANCEL ADD';

        function _cancelEdit() {
            $scope.editMode = 'NEW';
            $scope.addText = 'ADD PRECONDITION';
            $scope.cancelText = 'CANCEL ADD';
            $scope.ruleType = null;
            $scope.editRule = null;
        }

        $scope.cancelEdit = function() {
            _cancelEdit();
        };

        $scope.onRuleTypeChange = function() {
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
        };

        function validateForm() {
            return PolicyInputFormService.validateForm($scope.preconditionForm, $scope.editRule.properties, false);
        }

        function buildDisplayString() {
            if ($scope.editRule != null) {
                var str = '';
                _.each($scope.editRule.properties, function(prop) {
                    if (prop.type != 'currentFeed') {
                        //chain it to the display string
                        if (str != '') {
                            str += ';';
                        }
                        str += ' ' + prop.displayName;
                        var val = prop.value;
                        if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
                            val = _.map(prop.values, function(labelValue) {
                                return labelValue.value;
                            }).join(",");
                        }
                        str += ": " + val;
                    }
                });
                $scope.editRule.propertyValuesDisplayString = str;
            }
        }

        $scope.deletePrecondition = function() {
            var index = $scope.editIndex;
            if ($scope.preconditions != null && index != null) {
                $scope.preconditions.splice(index, 1);
            }
            feed.schedule.preconditions = $scope.preconditions;
            $scope.pendingEdits = true;
            $mdDialog.hide('done');
        };

        $scope.addPolicy = function() {

            var validForm = validateForm();
            if (validForm == true) {
                if ($scope.preconditions == null) {
                    $scope.preconditions = [];
                }
                buildDisplayString();

                $scope.editRule.ruleType = $scope.ruleType;
                if ($scope.editMode == 'NEW') {
                    $scope.preconditions.push($scope.editRule);
                }
                else if ($scope.editMode == 'EDIT') {
                    $scope.preconditions[$scope.editIndex] = $scope.editRule;

                }

                $scope.pendingEdits = true;
                feed.schedule.preconditions = $scope.preconditions;
                $mdDialog.hide('done');
            }
        };

        $scope.hide = function() {
            _cancelEdit();
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            _cancelEdit();
            $mdDialog.hide();
        };
    };

    angular.module(moduleName).controller("FeedPreconditionsDialogController", ["$scope","$mdDialog","$mdToast","$http","StateService","FeedService","PolicyInputFormService","feed","index",controller]);

});