(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                feed: '=?'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/sla/service-level-agreement.html',
            controller: "ServiceLevelAgreementController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $mdDialog, $mdToast, $http, StateService, FeedService, SlaService, PolicyInputFormService) {

        var self = this;

        //   this.feed = FeedService.editFeedModel;

        /**
         * SLA Options (aka. Metric Classes annotated with @ServiceLevelPolicy exposing the properties
         * @type {Array}
         */
        this.options = [];

        /**
         * The Form for validation
         * @type {{}}
         */
        this.slaForm = {};

        /**
         * Flag to indicated there are pending changes to be saved
         * @type {boolean}
         */
        self.pendingEdits = false;

        var EMPTY_RULE_TYPE = {name: ''};

        /**
         * The current Rule type that is being edited (i.e. the @ServiceLevelAgreementPolicy#name of the current edit
         * @type {null}
         */
        self.ruleType = EMPTY_RULE_TYPE;

        /**
         * The Default Condition to be applied to the new Rule
         * REQUIRED = "AND"
         * SUFFICIENT = "OR"
         */
        self.ruleTypeCondition = 'REQUIRED';

        /**
         * flag to indicate if we should show the SLA Rule Type/Condition selection
         * @type {boolean}
         */
        self.addingSlaCondition = false;

        /**
         * The Current array of SLA objects found for this feed
         * This will be a copy of the agreements so they can be freely edited
         * [{name:'', rules:[{name:'',properties:[], condition:""}]}]
         * @type {Array}
         */
        this.serviceLevelAgreements = [];

        /**
         * The index of the ServiceLevelAgreement that is being edited  in reference o the self.serviceLevelAgreements array
         * @type {null}
         */
        self.editSlaIndex = null;

        /**
         * The SLA Object that is being created/edited
         * @type {null}
         */
        self.editSla = null;

        /**
         * Array of all the Action Configuration options available
         * @type {Array}
         */
        self.slaActionOptions = []

        /**
         * flag to indicated if we should show the Add Another Action button or not along with the Action dropdown
         * @type {boolean}
         */
        self.addingSlaAction = false;

        /**
         * The current SLA Action selected to edit/add
         * @type {{name: string}}
         */
        self.slaAction = EMPTY_RULE_TYPE;

        /**
         * flag to indicate we are creating a new sla to clear the message "no slas exist"
         * @type {boolean}
         */
        self.creatingNewSla = false;

        /**
         * Load and copy the serviceLevelAgreements from the feed if available
         * @type {Array|*}
         */
        if (self.feed) {

            var arr = self.feed.serviceLevelAgreements;

            if (arr != null && arr != undefined) {
                self.serviceLevelAgreements = angular.copy(arr);
            }
        }

        if (this.feed != null) {
            SlaService.getFeedSlas(self.feed.feedId).then(function (response) {
                if (response.data && response.data.serviceLevelAgreements != undefined && response.data.serviceLevelAgreements.length > 0) {
                    _.each(response.data.serviceLevelAgreements, function (sla) {
                        _.each(sla.rules, function (rule) {
                            rule.groups = PolicyInputFormService.groupProperties(rule);
                            PolicyInputFormService.updatePropertyIndex(rule);
                        });

                        _.each(sla.actionConfigurations, function (rule) {
                            rule.groups = PolicyInputFormService.groupProperties(rule);
                            PolicyInputFormService.updatePropertyIndex(rule);
                        });
                    });
                    self.serviceLevelAgreements = response.data.serviceLevelAgreements;
                }
            });
        }
        else {
            //get All Slas
            SlaService.getAllSlas().then(function (response) {
                self.serviceLevelAgreements = response.data;
            });
        }

        /**
         * Load up the Metric Options for defining SLAs
         */
        SlaService.getPossibleSlaMetricOptions().then(function (response) {
            self.options = PolicyInputFormService.groupPolicyOptions(response.data);
        });

        SlaService.getPossibleSlaActionOptions().then(function (response) {
            self.slaActionOptions = PolicyInputFormService.groupPolicyOptions(response.data);
        })

        self.cancelEditSla = function () {
            self.addingSlaCondition = false;
            self.editSla = null;
            self.editSlaIndex = null;
        }

        self.addNewCondition = function () {
            self.ruleType = EMPTY_RULE_TYPE;
            //if editing one already validate, complete it and then add the new one
            var valid = true;
            if (self.editSla != null) {
                valid = self.validateForm();

            }
            if (valid) {
                //this will display the drop down to select the correct new rule/metric to assign to this SLA
                self.addingSlaCondition = true;
            }

        }

        self.addNewActionCondition = function () {
            self.addingSlaAction = true;
        }

        self.saveSla = function () {
            var valid = self.validateForm();
            if (valid) {

                function success(response) {
                    if (response) {
                        self.editSla.id = response.id;
                    }
                    if (self.editSlaIndex != null) {
                        self.serviceLevelAgreements[self.editSlaIndex] = self.editSla;
                    }
                    else {
                        self.serviceLevelAgreements.push(self.editSla);
                    }
                    self.addingSlaCondition = false;
                    self.editSla = null;
                    self.editSlaIndex = null;

                    $mdDialog.show(
                        $mdDialog.alert()
                            .parent(angular.element(document.body))
                            .clickOutsideToClose(true)
                            .title('Saved SLA')
                            .textContent('Saved the Sla')
                            .ariaLabel('Alert Saved Sla')
                            .ok('Got it!')
                    );
                }

                saveSla(success)

            }

        }
        function saveSla(successFn, failureFn) {
            if (self.feed != null) {
                SlaService.saveFeedSla(self.feed.feedId, self.editSla).then(function (response) {
                    if (successFn) {
                        successFn(response);
                    }
                }, function () {
                    if (failureFn) {
                        failureFn();
                    }
                });
            }
            else {
                SlaService.saveSla(self.editSla).then(function () {
                    if (successFn) {
                        successFn();
                    }
                }, function () {
                    if (failureFn) {
                        failureFn();
                    }
                });
            }
        }

        self.onBackToList = function (ev) {
            self.editSla = null;
            self.creatingNewSla = null;
        }

        self.onNewSla = function () {
            self.creatingNewSla = true;
            self.editSlaIndex = null;
            self.editSla = {name: '', description: '', rules: [], actionConfigurations: []};
            self.addingSlaCondition = true;
        }

        self.onEditSla = function (index) {
            self.creatingNewSla = false;
            self.editSlaIndex = index;
            self.ruleType = EMPTY_RULE_TYPE;
            self.addingSlaCondition = false;

            var slaObj = self.serviceLevelAgreements[index]
            //fetch the SLA
            SlaService.getSlaForEditForm(slaObj.id).then(function (response) {
                var sla = response.data;

                _.each(sla.rules, function (rule) {
                    rule.editable = true;
                    rule.groups = PolicyInputFormService.groupProperties(rule);
                    PolicyInputFormService.updatePropertyIndex(rule);
                });

                _.each(sla.actionConfigurations, function (rule) {
                    rule.editable = true;
                    rule.groups = PolicyInputFormService.groupProperties(rule);
                    PolicyInputFormService.updatePropertyIndex(rule);
                });
                sla.editable = true;
                self.editSla = sla;

            });
        }

        self.onDeleteSla = function (ev) {
            //warn are you sure you want to delete?
            if (self.editSlaIndex != null) {
                var confirm = $mdDialog.confirm()
                    .title('Delete SLA')
                    .textContent('Are you sure you want to Delete this SLA?')
                    .ariaLabel('Delete SLA')
                    .targetEvent(ev)
                    .ok('Please do it!')
                    .cancel('Nope');
                $mdDialog.show(confirm).then(function () {
                    SlaService.deleteSla(self.editSla.id).then(function () {
                        self.editSla = null;
                        self.serviceLevelAgreements.splice(self.editSlaIndex, 1);
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('SLA Deleted.')
                                .position('bottom left')
                                .hideDelay(3000)
                        );
                    }, function () {
                        //alert delete error
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('Error deleting SLA.')
                                .position('bottom left')
                                .hideDelay(3000)
                        );
                    });

                }, function () {
                    //cancelled confirm box
                });

            }
        }

        self.onDeleteSlaMetric = function (index) {
            //warn before delete
            self.editSla.rules.splice(index, 1);
            if (self.editSla.rules.length == 0) {
                self.addingSlaCondition = true;
            }
        }

        self.onDeleteSlaAction = function (index) {
            //warn before delete
            self.editSla.actionConfigurations.splice(index, 1);
            if (self.editSla.actionConfigurations.length == 0) {
                self.addingSlaCondition = true;
            }
        }

        self.onAddConditionRuleTypeChange = function () {
            if (self.ruleType != EMPTY_RULE_TYPE) {
                //replace current sla rule if already editing
                var newRule = angular.copy(self.ruleType);
                //update property index
                PolicyInputFormService.updatePropertyIndex(newRule);

                newRule.condition = self.ruleTypeCondition;
                newRule.editable = true;
                self.editSla.rules.push(newRule);
                self.addingSlaCondition = false;
                self.ruleType = EMPTY_RULE_TYPE;

            }
        }

        self.onAddSlaActionChange = function () {
            if (self.slaAction != EMPTY_RULE_TYPE) {
                //replace current sla rule if already editing
                var newRule = angular.copy(self.slaAction);
                //update property index
                PolicyInputFormService.updatePropertyIndex(newRule);

                newRule.editable = true;
                self.editSla.actionConfigurations.push(newRule);
                self.addingSlaAction = false;
                self.slaAction = EMPTY_RULE_TYPE;
            }
        }

        /**
         * Validate the form before adding/editing a Rule for an SLA
         * @returns {boolean}
         */
        self.validateForm = function () {
            //loop through properties and determine if they are valid
            //the following _.some routine returns true if the items are invalid
            var ruleProperties = [];
            _.each(self.editSla.rules, function (rule) {
                _.each(rule.properties, function (property) {
                    ruleProperties.push(property);
                });
            });

            var validForm = PolicyInputFormService.validateForm(self.slaForm, ruleProperties);
            if (validForm) {
                //validate there is at least 1 action configuration
                var actions = self.editSla.actionConfigurations.length;
                if (actions == 0) {
                    validForm = false;
                    $mdDialog.show(
                        $mdDialog.alert()
                            .parent(angular.element(document.body))
                            .clickOutsideToClose(true)
                            .title('SLA Action Required')
                            .textContent('At least 1 SLA Action is Required')
                            .ariaLabel('Alert Input Sla errors')
                            .ok('Got it!')
                    );
                }
            }
            return validForm;
        }

        function buildDisplayString() {
            if (self.editRule != null) {
                var str = '';
                _.each(self.editRule.properties, function (prop, idx) {
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
                self.editRule.propertyValuesDisplayString = str;
            }
        }

    };

    angular.module(MODULE_FEED_MGR).controller('ServiceLevelAgreementController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigServiceLevelAgreement', directive);

}());