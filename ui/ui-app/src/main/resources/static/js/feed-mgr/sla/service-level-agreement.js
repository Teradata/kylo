define(['angular',"feed-mgr/sla/module-name"], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                feed: '=?',
                newSla: '=?',
                slaId:'=?'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: function(tElement, tAttrs) {
                if (tAttrs) {
                    if (tAttrs.view === 'all') {
                        return 'js/feed-mgr/sla/service-level-agreements.html'
                    }
                    if (tAttrs.view === 'feed') {
                        return 'js/feed-mgr/sla/feed-service-level-agreements.html'
                    }
                }
            },

            controller: "ServiceLevelAgreementController",
            link: function($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function($scope, $mdDialog, $mdToast, $http, $rootScope, $q,StateService, FeedService, SlaService, PolicyInputFormService, PaginationDataService, TableOptionsService,
                              AddButtonService, AccessControlService,EntityAccessControlService) {

        var self = this;


        /**
         * Indicates if editing SLAs is allowed.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * shows the (+) Add button when trying to assign an SLA that is not directly tied to a feed initially
         * @type {boolean}
         */
        self.allowCreate = false;

        self.cardTitle = "Service Level Agreements";

        self.cardLinksTitle = "Links";

        /**
         * show a progress bar indicating loading SLA list or individual SLA for edit
         * @type {boolean}
         */
        self.loading = true;

        var loadingListMessage = "Loading Service Level Agreements";
        var loadingSingleMessage = "Loading Service Level Agreement";

        /**
         * Loading message
         */
        self.loadingMessage = loadingListMessage;

        //if the newSLA flag is tripped then show the new SLA form and then reset it

        if (self.newSla == null && self.newSla == undefined) {
            self.newSla = false;
        }

        $scope.$watch(function() {
            return self.newSla;
        }, function(newVal) {
            if (newVal == true) {
                self.onNewSla();
                self.newSla = false;
                self.loading = false;
            }
        });

        // Register Add button
        AccessControlService.getUserAllowedActions()
            .then(function(actionSet) {
                if (AccessControlService.hasAction(AccessControlService.SLA_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton("service-level-agreements", function() {
                        self.onNewSla();
                    });
                    self.allowCreate = true;
                }
            });

        function showList(requery) {
            self.cardTitle = "Service Level Agreements"
            self.renderFilter = true;
            self.editSla = null;
            self.creatingNewSla = null;
            self.editSlaId = null;
            self.addingSlaCondition = false;
            self.editSlaIndex = null;
            if(requery && requery == true){
                loadSlas();
            }
            //Requery?
            if (self.feed == null && self.allowCreate) {
                //display the add button if we are not in a given feed
                AddButtonService.showAddButton();
            }

        }

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
         * The ID of the SLA that is being modified/edited
         * @type {null}
         */
        self.editSlaId = null;

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

        self.showActionOptions = false;

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
         * Either NEW or EDIT
         * @type {string}
         */
        self.mode = 'NEW';

        self.userSuppliedName = false;

        self.userSuppliedDescription = false;

        //Pagination DAta
        this.pageName = "service-level-agreements";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'service-level-agreements';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        if(this.feed != null){
            this.paginationData.rowsPerPage=100;
        }
        else {
            this.paginationData.rowsPerPage=5;
        }
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);


        $scope.$watch(function() {
            return self.viewType;
        }, function(newVal) {
            self.onViewTypeChange(newVal);
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
        };

        this.onPaginationChange = function(page, limit) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Name': 'name', 'Description': 'description'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'name', 'asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        /**
         * Fetch the SLAs and populate the list.
         * If there is a Feed using this page it will only get the SLAs related for this feed
         * otherwise it will get all the SLas
         */
        function loadSlas() {
            self.loadingMessage = loadingListMessage;
            self.loading = true;
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

            if (self.feed != null) {
                SlaService.getFeedSlas(self.feed.feedId).then(function(response) {
                    if (response.data && response.data != undefined && response.data.length > 0) {
                        self.serviceLevelAgreements = response.data;
                    }
                    self.loading = false;
                });
            }
            else {
                //get All Slas
                SlaService.getAllSlas().then(function(response) {
                    self.serviceLevelAgreements = response.data;
                    self.loading = false;
                });
            }
        }


        if(self.slaId){
            //fetch the sla


            SlaService.getSlaForEditForm(self.slaId).then(function(response){
                applyEditPermissionsToSLA(response.data);
                if (self.allowEdit) {
                    self.editSla = response.data;
                    applyAccessPermissions()
                    self.editSlaId = self.slaId;
                    self.onEditSla(self.editSla);
                }
                else {
                    //
                }
            })

        }
        else {
            /**
             * Initiall load the SLA list
             */
            loadSlas();
        }

        /**
         * Load up the Metric Options for defining SLAs
         */
        SlaService.getPossibleSlaMetricOptions().then(function(response) {

            var currentFeedValue = null;
            if (self.feed != null) {
                currentFeedValue = PolicyInputFormService.currentFeedValue(self.feed);
            }
            self.options = PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if(self.allowCreate || self.allowEdit){
                PolicyInputFormService.stripNonEditableFeeds(self.options);
            }

        });

        /**
         * Get all possible SLA Action Options
         */
        SlaService.getPossibleSlaActionOptions().then(function(response) {
            var currentFeedValue = null;
            if (self.feed != null) {
                currentFeedValue = PolicyInputFormService.currentFeedValue(self.feed);
            }
            self.slaActionOptions = PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (self.slaActionOptions.length > 0) {
                self.showActionOptions = true;

                _.each(self.slaActionOptions, function(action) {
                    //validate the rules
                    SlaService.validateSlaActionRule(action);
                });

                if(self.allowCreate || self.allowEdit){
                    PolicyInputFormService.stripNonEditableFeeds(self.slaActionOptions);
                }

            }
            else {
                self.showActionOptions = false;
            }
        })

        /**
         * Called when the user cancels a specific SLA
         */
        self.cancelEditSla = function() {
            showList();
            applyAccessPermissions();
            self.userSuppliedName = false;
            self.userSuppliedDescription = false;

        }

        self.addNewCondition = function() {
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

        self.addNewActionCondition = function() {
            self.addingSlaAction = true;
        }



        function deriveSlaName(){

            var feedNamesString = null;
            var feedNames = PolicyInputFormService.getFeedNames(self.editSla.rules);
            if(feedNames.length >0) {
                feedNamesString = feedNames.join(",");
            }
            var ruleNames = PolicyInputFormService.getRuleNames(self.editSla.rules);
            var slaName = ruleNames.join(",");
            if(feedNamesString != null) {
                slaName = feedNamesString + " - " + slaName;
            }
            return slaName;
        }

        function deriveDescription(){

            var feedNamesString = null;
            var feedNames = PolicyInputFormService.getFeedNames(self.editSla.rules);
            if(feedNames.length >0) {
                feedNamesString = feedNames.join(",");
            }
            var ruleNames = PolicyInputFormService.getRuleNames(self.editSla.rules);
            var desc = ruleNames.join(",");
            if(feedNamesString != null) {
                desc += " for "+feedNamesString;
            }
            return desc;
        }

        self.onPropertyChange = function(property) {
            if(PolicyInputFormService.isFeedProperty(property)) {
                if (self.editSla != null && (self.userSuppliedName == false || (self.editSla.name == '' || self.editSla.name == null))) {
                    self.editSla.name = deriveSlaName();
                }
                if (self.editSla != null && (self.userSuppliedDescription == false || (self.editSla.description == '' || self.editSla.description == null))) {
                    self.editSla.description = deriveDescription();
                }
            }

        }

        self.onNameChange = function(){
            self.userSuppliedName = true;
        }

    self.onDescriptionChange = function(){
        self.userSuppliedDescription = true;
    }

    self.saveSla = function() {
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
                showList(true);
                $mdToast.show(
                    $mdToast.simple()
                        .textContent("Saved the SLA")
                        .hideDelay(1000)
                );
            }

            saveSla(success)

        }

    }
    function saveSla(successFn, failureFn) {


        $mdDialog.show(
            $mdDialog.alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title('Saving SLA')
                .textContent('Saving the Sla')
                .ariaLabel('Saving Sla')
        );
        if (self.feed != null) {
            SlaService.saveFeedSla(self.feed.feedId, self.editSla).then(function(response) {
                $mdDialog.hide();
                if (successFn) {
                    successFn(response);
                }
            }, function() {
                if (failureFn) {
                    failureFn();
                }
            });
        }
        else {
            SlaService.saveSla(self.editSla).then(function() {
                $mdDialog.hide();
                if (successFn) {
                    successFn();
                }
            }, function() {
                if (failureFn) {
                    failureFn();
                }
            });
        }
    }

    self.onBackToList = function(ev) {
        var requery = false;
        if(self.serviceLevelAgreements == null || self.serviceLevelAgreements.length == 0) {
            requery = true;
        }
        showList(requery);
        applyAccessPermissions();

    }

    self.onNewSla = function() {
        AddButtonService.hideAddButton();
        self.mode = 'NEW';
        self.creatingNewSla = true;
        self.editSlaIndex = null;
        self.editSlaId = null;
        self.editSla = {name: '', description: '', rules: [], actionConfigurations: []};
        self.addingSlaCondition = true;
        self.renderFilter = false;
        self.userSuppliedName = false;
        self.userSuppliedDescription = false;
        self.cardTitle = "New Service Level Agreement"

    }

    self.onEditSla = function(sla) {
        if (self.allowEdit) {
            AddButtonService.hideAddButton();
            self.editSlaIndex = _.findIndex(self.serviceLevelAgreements, sla);
            self.loadAndEditSla(sla.id);
            self.renderFilter = false;
        } else {
            $mdDialog.show(
                $mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Access Denied")
                    .textContent("You do not have access to edit SLAs.")
                    .ariaLabel("Access denied to edit SLAs.")
                    .ok("OK")
            );
        }
    };

    self.viewSlaAssessments = function(){
        if(self.editSla){
            StateService.OpsManager().Sla().navigateToServiceLevelAssessments('slaId=='+self.editSla.id);
        }
    }

    self.loadAndEditSla = function(slaId) {
        self.cardTitle = "Edit Service Level Agreement"
        self.mode = 'EDIT';
        self.creatingNewSla = false;
        self.editSlaId = slaId;
        self.ruleType = EMPTY_RULE_TYPE;
        self.addingSlaCondition = false;
        self.loadingMessage = loadingSingleMessage;
        self.loading = true;
        self.userSuppliedName = true;
        self.userSuppliedDescription = true;

        //fetch the SLA
        SlaService.getSlaForEditForm(slaId).then(function(response) {
            var sla = response.data;
            applyEditPermissionsToSLA(sla);
            _.each(sla.rules, function(rule) {
                rule.editable = sla.canEdit;
                rule.mode = 'EDIT'
                rule.groups = PolicyInputFormService.groupProperties(rule);
                PolicyInputFormService.updatePropertyIndex(rule);
            });

            _.each(sla.actionConfigurations, function(rule) {
                rule.editable = sla.canEdit;
                rule.mode = 'EDIT'
                rule.groups = PolicyInputFormService.groupProperties(rule);
                PolicyInputFormService.updatePropertyIndex(rule);
                //validate the rules
                SlaService.validateSlaActionRule(rule)

            });
            //sla.editable = sla.canEdit;
            self.editSla = sla;
            self.loading = false;
            //  self.allowEdit = self.editSla.canEdit;

        }, function(err){
            var msg = err.data.message || 'Error loading the SLA';
            self.loading = false;
            $mdDialog.show(
                $mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Error loading the SLA")
                    .textContent(msg)
                    .ariaLabel("Access denied to edit the SLA")
                    .ok("OK")
            );

        });
    }

    function applyEditPermissionsToSLA(sla){

        var entityAccessControlled = AccessControlService.isEntityAccessControlled();

        var functionalAccess = AccessControlService.getUserAllowedActions()
        $q.when(functionalAccess).then(function (response) {
            if(entityAccessControlled) {
                sla.editable = sla.canEdit;
                self.allowEdit = sla.canEdit;
            }
            else {
                var allowFeedEdit = self.feed != null ? AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.actions) : true;
                self.allowEdit =  allowFeedEdit && AccessControlService.hasAction(AccessControlService.SLA_EDIT, response.actions);
                sla.editable = self.allowEdit;
            }
        });



    }

    self.onDeleteSla = function(ev) {
        //warn are you sure you want to delete?
        if (self.editSlaIndex != null || self.editSlaId != null) {
            var confirm = $mdDialog.confirm()
                .title('Delete SLA')
                .textContent('Are you sure you want to Delete this SLA?')
                .ariaLabel('Delete SLA')
                .targetEvent(ev)
                .ok('Please do it!')
                .cancel('Nope');
            $mdDialog.show(confirm).then(function() {
                SlaService.deleteSla(self.editSla.id).then(function() {
                    self.editSla = null;
                    if (self.editSlaIndex != null) {
                        self.serviceLevelAgreements.splice(self.editSlaIndex, 1);
                    }
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('SLA Deleted.')
                            .position('bottom left')
                            .hideDelay(3000)
                    );
                    showList();
                }, function() {
                    //alert delete error
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Error deleting SLA.')
                            .position('bottom left')
                            .hideDelay(3000)
                    );
                });

            }, function() {
                //cancelled confirm box
            });

        }
    }

    self.onDeleteSlaMetric = function(index) {
        //warn before delete
        self.editSla.rules.splice(index, 1);
        if (self.editSla.rules.length == 0) {
            self.addingSlaCondition = true;
        }
    }

    self.onDeleteSlaAction = function(index) {
        //warn before delete
        self.editSla.actionConfigurations.splice(index, 1);
        if (self.editSla.actionConfigurations.length == 0) {
            self.addingSlaCondition = true;
        }
    }

    self.onAddConditionRuleTypeChange = function() {
        if (self.ruleType != EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            var newRule = angular.copy(self.ruleType);
            newRule.mode = 'NEW'
            //update property index
            PolicyInputFormService.updatePropertyIndex(newRule);

            newRule.condition = self.ruleTypeCondition;
            newRule.editable = true;
            self.editSla.rules.push(newRule);
            self.addingSlaCondition = false;
            self.ruleType = EMPTY_RULE_TYPE;

            if((self.userSuppliedName == false  || (self.editSla.name == '' || self.editSla.name == null))) {
                self.editSla.name = deriveSlaName();
            }
            if((self.editSla.description == '' || self.editSla.description == null)) {
                self.editSla.description = deriveDescription();
            }

        }
    }

    self.onAddSlaActionChange = function() {
        if (self.slaAction != EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            var newRule = angular.copy(self.slaAction);
            newRule.mode = 'NEW'
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
    self.validateForm = function() {
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        var ruleProperties = [];
        _.each(self.editSla.rules, function(rule) {
            _.each(rule.properties, function(property) {
                ruleProperties.push(property);
            });
        });

        var validForm = PolicyInputFormService.validateForm(self.slaForm, ruleProperties);
        return validForm;
    }

    function buildDisplayString() {
        if (self.editRule != null) {
            var str = '';
            _.each(self.editRule.properties, function(prop, idx) {
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
            self.editRule.propertyValuesDisplayString = str;
        }
    }

    function applyAccessPermissions() {
        //Apply the entity access permissions
        //if its an existing SLA, check to see if we can edit it
        if (self.editSla && !self.newSla) {
            self.allowEdit = self.editSla.canEdit;
        }
        else {
            //ensure the user has the EDIT_SLA and if editing for a feed ensure the user has access to edit that feed

            var entityAccessControlled = self.feed != null && AccessControlService.isEntityAccessControlled();

            //Apply the entity access permissions
            var requests = {
                entityEditAccess: entityAccessControlled == true ? FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, self.feed) : true,
                functionalAccess: AccessControlService.getUserAllowedActions()
            }
            $q.all(requests).then(function (response) {
                var allowEditAccess = AccessControlService.hasAction(AccessControlService.SLA_EDIT, response.functionalAccess.actions);
                var slaAccess = AccessControlService.hasAction(AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                var allowFeedEdit = self.feed != null ? AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions) : true;
                self.allowEdit = response.entityEditAccess && allowEditAccess && slaAccess && allowFeedEdit;
            });

        }
    }

    applyAccessPermissions();


};

angular.module(moduleName).controller('ServiceLevelAgreementController', ["$scope","$mdDialog","$mdToast","$http","$rootScope","$q","StateService","FeedService","SlaService","PolicyInputFormService","PaginationDataService","TableOptionsService","AddButtonService","AccessControlService","EntityAccessControlService",controller]);
angular.module(moduleName)
    .directive('thinkbigServiceLevelAgreement', directive);

});
