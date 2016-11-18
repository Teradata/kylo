(function() {

    /**
     * Displays the details for a feed.
     *
     * @param $scope
     * @param $q
     * @param $stateParams
     * @param $mdDialog
     * @param $mdToast
     * @param $http
     * @param $state
     * @param {AccessControlService} AccessControlService the access control service
     * @param RestUrlService
     * @param FeedService
     * @param RegisterTemplateService
     * @param StateService
     */
    var controller = function ($scope, $q, $stateParams, $mdDialog, $mdToast, $http, $state, AccessControlService, RestUrlService, FeedService, RegisterTemplateService, StateService, SideNavService) {

        var SLA_INDEX = 3;
        var self = this;

        /**
         * Flag to indicate style of page
         * if true it will fit the card to the 980px width
         * if false it will make it go 100% width
         * @type {boolean}
         */
        self.cardWidth = true;

        /**
         * Indicates if admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        /**
         * Indicates if edit operations are allowed.
         * @type {boolean}
         */
        self.allowEdit = false;

        this.feedId = null;
        this.selectedTabIndex = 0;

        this.loadingFeedData = false;
        this.model = FeedService.editFeedModel;
        this.model.loaded = false;
        this.loadMessage = ''

        var requestedTabIndex = $stateParams.tabIndex;



        $scope.$watch(function() {
            return self.selectedTabIndex;
        }, function(newVal) {
            //Make the Lineage tab fit without side nav
            //open side nav if we are not navigating between lineage links
            if (newVal == 2 || (requestedTabIndex != undefined && requestedTabIndex == 2)) {
                SideNavService.hideSideNav();
                self.cardWidth = false;
                requestedTabIndex = 0;
            }
            else {
                SideNavService.showSideNav();
                self.cardWidth = true;
            }

        })

        /**
         * flag to indicate if the SLA page should be set to empty new form rather than the list
         * Used for when the "Add SLA" button is clicked
         * @type {boolean}
         */
        this.newSla = false;

        var init = function() {
            self.feedId = $stateParams.feedId;

            loadFeed(requestedTabIndex);

            AccessControlService.getAllowedActions()
                    .then(function(actionSet) {
                        self.allowAdmin = AccessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
                        self.allowEdit = AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions);
                    });
        };

        /**
         * Displays a confirmation dialog for deleting the feed.
         */
        this.confirmDeleteFeed = function() {
            var $dialogScope = $scope.$new();
            $dialogScope.dialog = $mdDialog;
            $dialogScope.vm = self;

            $mdDialog.show({
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                scope: $dialogScope,
                templateUrl: "js/feed-details/feed-details-delete-dialog.html"
            });
        };

        /**
         * Permanently deletes this feed.
         */
        this.deleteFeed = function() {
            // Update model state
            self.model.state = "DELETED";

            // Delete the feed
            var successFn = function() {
                $state.go("feeds");
            };
            var errorFn = function(response) {
                // Update model state
                self.model.state = "DISABLED";

                // Display error message
                var msg = "<p>The feed cannot be deleted at this time.</p><p>";
                msg += angular.isString(response.data.message) ? _.escape(response.data.message) : "Please try again later.";
                msg += "</p>";

                $mdDialog.hide();
                $mdDialog.show(
                        $mdDialog.alert()
                                .ariaLabel("Error deleting feed")
                                .clickOutsideToClose(true)
                                .htmlContent(msg)
                                .ok("Got it!")
                                .parent(document.body)
                                .title("Error deleting feed")
                );
            };

            $http.delete(RestUrlService.GET_FEEDS_URL + "/" + self.feedId).then(successFn, errorFn);
        };

        /**
         * Enables this feed.
         */
        this.enableFeed = function() {
            self.enabling = true;
            $http.post(RestUrlService.ENABLE_FEED_URL(self.feedId)).then(function(response) {
                self.model.state = response.data.state;
                FeedService.updateEditModelStateIcon();
                self.enabling = false;
            }, function() {
                $mdDialog.show(
                        $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("NiFi Error")
                                .textContent("The feed could not be enabled.")
                                .ariaLabel("Cannot enable feed.")
                                .ok("OK")
                );
                self.enabling = false;
            });
        };

        /**
         * Disables this feed.
         */
        this.disableFeed = function() {
            self.disabling = true;
            $http.post(RestUrlService.DISABLE_FEED_URL(self.feedId)).then(function(response) {
                self.model.state = response.data.state;
                FeedService.updateEditModelStateIcon();
                self.disabling = false;
            }, function() {
                $mdDialog.show(
                        $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("NiFi Error")
                                .textContent("The feed could not be disabled.")
                                .ariaLabel("Cannot disable feed.")
                                .ok("OK")
                );
                self.disabling = false;
            });
        };

        function mergeTemplateProperties(feed) {
            var successFn = function(response) {
                return response;
            }
            var errorFn = function(err) {

            }

            var promise = $http({
                url: RestUrlService.MERGE_FEED_WITH_TEMPLATE(feed.id),
                method: "POST",
                data: angular.toJson(feed),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);

            return promise;
        }

        /**
         * Navigates to the category details page for this feed's category.
         *
         * An error is displayed if the user does not have permissions to access categories.
         */
        this.onCategoryClick = function() {
            AccessControlService.getAllowedActions()
                    .then(function(actionSet) {
                        if (AccessControlService.hasAction(AccessControlService.CATEGORIES_ACCESS, actionSet.actions)) {
                            StateService.navigateToCategoryDetails(self.model.category.id);
                        } else {
                            $mdDialog.show(
                                    $mdDialog.alert()
                                            .clickOutsideToClose(true)
                                            .title("Access Denied")
                                            .textContent("You do not have permissions to access categories.")
                                            .ariaLabel("Access denied for categories")
                                            .ok("OK")
                            );
                        }
                    });
        };

        this.onTableClick = function() {
            StateService.navigateToTable(self.model.category.systemName, self.model.table.tableSchema.name);
        }

        this.addSla = function() {
            self.selectedTabIndex = SLA_INDEX;
            self.newSla = true;
        }

        function loadFeed(tabIndex) {
            self.loadingFeedData = true;
            self.model.loaded = false;
            self.loadMessage = '';
            var successFn = function(response) {
                if (response.data) {
                    mergeTemplateProperties(response.data).then(function(updatedFeedResponse) {
                        //merge in the template properties
                        //this will update teh self.model as they point to the same object
                        if (updatedFeedResponse == undefined || updatedFeedResponse.data == undefined) {
                            self.loadingFeedData = false;
                            var loadMessage = 'Unable to load Feed Details.  Please ensure that Apache Nifi is up and running and then refresh this page.';
                            self.loadMessage = loadMessage;
                            $mdDialog.show(
                                    $mdDialog.alert()
                                    //   .parent(angular.element(document.querySelector('#popupContainer')))
                                            .clickOutsideToClose(true)
                                            .title('Unable to load Feed Details')
                                            .textContent(loadMessage)
                                            .ariaLabel('Unable to load Feed Details')
                                            .ok('Got it!')
                            );
                        } else {
                            self.model.loaded = true;
                            FeedService.updateFeed(updatedFeedResponse.data);
                            if (tabIndex != null && tabIndex != undefined && tabIndex != self.selectedTabIndex) {
                                self.selectedTabIndex = tabIndex;
                            }


                            RegisterTemplateService.initializeProperties(updatedFeedResponse.data.registeredTemplate,'edit');
                            self.model.inputProcessors = RegisterTemplateService.removeNonUserEditableProperties(updatedFeedResponse.data.registeredTemplate.inputProcessors,true);
                            self.model.inputProcessor = _.find(self.model.inputProcessors,function(processor){
                                return self.model.inputProcessorType == processor.type;
                            });
                            self.model.nonInputProcessors = RegisterTemplateService.removeNonUserEditableProperties(updatedFeedResponse.data.registeredTemplate.nonInputProcessors,false);
                            self.loadingFeedData = false;
                            FeedService.updateEditModelStateIcon();

                            /*

                            //get those properties that are Input properties
                            var processors = {};
                            var inputProcessors = [];

                            var nonInputProcessors = [];
                            angular.forEach(self.model.properties, function(property) {
                                if (property.userEditable) {

                                    if (processors[property.processorId] === undefined) {
                                        processors[property.processorId] = {
                                            name: property.processorName,
                                            properties: [],
                                            processorId: property.processorId,
                                            inputProcessor: property.inputProperty,
                                            type: property.processorType
                                        }
                                        if (property.inputProperty) {
                                            inputProcessors.push(processors[property.processorId]);
                                            if (self.model.inputProcessorType == property.processorType) {
                                                self.model.inputProcessor = processors[property.processorId];
                                            }
                                        }
                                        else {
                                            nonInputProcessors.push(processors[property.processorId]);
                                        }
                                    }

                                    var processor = processors[property.processorId];
                                    processor.properties.push(property);
                                }
                                property.value = RegisterTemplateService.deriveExpression(property.value);
                                property.renderWithCodeMirror = RegisterTemplateService.isRenderPropertyWithCodeMirror(property);

                            });

                            self.model.inputProcessors = inputProcessors;
                            self.model.nonInputProcessors = nonInputProcessors;
                            self.loadingFeedData = false;
                            FeedService.updateEditModelStateIcon();
                            */
                        }
                    }, function(err) {
                        //handle err
                        self.loadingFeedData = false;
                    })

                }
            }
            var errorFn = function(err) {
                self.loadingFeedData = false;
                $mdDialog.show(
                        $mdDialog.alert()
                                .parent(angular.element(document.querySelector('body')))
                                .clickOutsideToClose(true)
                                .title('Error loading feed')
                                .textContent('Feed error ')
                                .ariaLabel('Error loading feed')
                                .ok('Got it!')
                        //.targetEvent(ev)
                );

            }
            var promise = $http.get(RestUrlService.GET_FEEDS_URL + "/" + self.feedId);
            promise.then(successFn, errorFn);
            return promise;
        }

        init();
    };

    angular.module(MODULE_FEED_MGR).controller('FeedDetailsController', controller);

}());

