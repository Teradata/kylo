define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    /**
     * Displays the details for a feed.
     *
     * @param $scope
     * @param $q
     * @param $transition$.params()
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
    var controller = function ($scope, $q, $transition$, $mdDialog, $mdToast, $http, $state, AccessControlService, RestUrlService, FeedService, RegisterTemplateService, StateService, SideNavService,
                               FileUpload, ConfigurationService,EntityAccessControlDialogService, EntityAccessControlService, UiComponentsService) {

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
         * Allow the Changing of this feeds permissions
         * @type {boolean}
         */
        self.allowChangePermissions = false;

        /**
         * Indicates if edit operations are allowed.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Indicates if export operations are allowed.
         * @type {boolean}
         */
        self.allowExport = false;

        /**
         * Alow user to access the sla tab
         * @type {boolean}
         */
        self.allowSlaAccess = false;

        this.feedId = null;
        this.selectedTabIndex = 0;

        this.loadingFeedData = false;
        this.model = FeedService.editFeedModel;
        this.model.loaded = false;
        this.loadMessage = ''
        this.uploadFile = null;
        this.uploading = false;
        this.uploadAllowed = false;

        /**
         * flag to indicate the feed could not be loaded
         * @type {boolean}
         */
        this.errorLoadingFeed = false;



        /** flag to indicate if we get a valid connection back from NiFi.  Initially to true. it will be rechecked on load **/
        this.isNiFiRunning = true;

        var requestedTabIndex = $transition$.params().tabIndex;



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
            self.feedId = $transition$.params().feedId;

            self.exportFeedUrl = RestUrlService.ADMIN_EXPORT_FEED_URL+"/"+self.feedId

            loadFeed(requestedTabIndex);
            nifiRunningCheck();
        };

        this.cloneFeed = function(){
            StateService.FeedManager().Feed().navigateToCloneFeed(this.model.feedName);
        }

        /**
         * Displays a confirmation dialog for deleting the feed.
         */
        this.confirmDeleteFeed = function() {
            if(self.allowAdmin) {
                // Verify there are no dependent feeds
                if (angular.isArray(self.model.usedByFeeds) && self.model.usedByFeeds.length > 0) {
                    var list = "<ul>";
                    list += _.map(self.model.usedByFeeds, function (feed) {
                        return "<li>" + _.escape(feed.feedName) + "</li>";
                    });
                    list += "</ul>";

                    var alert = $mdDialog.alert()
                        .parent($("body"))
                        .clickOutsideToClose(true)
                        .title("Feed is referenced")
                        .htmlContent("This feed is referenced by other feeds and cannot be deleted. The following feeds should be deleted first: " + list)
                        .ariaLabel("feed is referenced")
                        .ok("Got it!");
                    $mdDialog.show(alert);

                    return;
                }

                // Display delete dialog
                var $dialogScope = $scope.$new();
                $dialogScope.dialog = $mdDialog;
                $dialogScope.vm = self;

                $mdDialog.show({
                    escapeToClose: false,
                    fullscreen: true,
                    parent: angular.element(document.body),
                    scope: $dialogScope,
                    templateUrl: "js/feed-mgr/feeds/edit-feed/feed-details-delete-dialog.html"
                });
            }
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

        this.showFeedUploadDialog = function() {
            $mdDialog.show({
                controller: 'FeedUploadFileDialogController',
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                templateUrl: "js/feed-mgr/feeds/edit-feed/feed-details-upload-dialog.html",
                locals: {feedId: self.feedId}
            }).then(function(msg) {
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('File uploaded.')
                        .hideDelay(3000)
                );
            });
        }

        this.showAccessControlDialog = function(){

            function onCancel(){

            }

            function onSave(){
            }

            EntityAccessControlDialogService.showAccessControlDialog(self.model,"feed",self.model.feedName,onSave,onCancel);

        }


        this.openFeedMenu = function($mdOpenMenu, ev) {
            $mdOpenMenu(ev);
        };


        /**
         * Enables this feed.
         */
        this.enableFeed = function() {
            if(!self.enabling && self.allowEdit) {
                self.enabling = true;
                $http.post(RestUrlService.ENABLE_FEED_URL(self.feedId)).then(function (response) {
                    self.model.state = response.data.state;
                    FeedService.updateEditModelStateIcon();
                    self.enabling = false;
                }, function () {
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
            }
        };

        /**
         * Disables this feed.
         */
        this.disableFeed = function() {
            if(!self.disabling && self.allowEdit) {
                self.disabling = true;
                $http.post(RestUrlService.DISABLE_FEED_URL(self.feedId)).then(function (response) {
                    self.model.state = response.data.state;
                    FeedService.updateEditModelStateIcon();
                    self.disabling = false;
                }, function () {
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
            }
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
            AccessControlService.getUserAllowedActions()
                    .then(function(actionSet) {
                        if (AccessControlService.hasAction(AccessControlService.CATEGORIES_ACCESS, actionSet.actions)) {
                            StateService.FeedManager().Category().navigateToCategoryDetails(self.model.category.id);
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
            StateService.FeedManager().Table().navigateToTable(self.model.category.systemName, self.model.table.tableSchema.name);
        }

        this.addSla = function() {
            self.selectedTabIndex = SLA_INDEX;
            self.newSla = true;
        }

        this.updateMenuOptions = function() {
            self.uploadAllowed=false;
            var model = self.model;
            if (model && model.inputProcessor && model.inputProcessor.allProperties.length > 0) {
                angular.forEach(model.inputProcessor.allProperties, function (property) {
                   if (property.processorType == 'org.apache.nifi.processors.standard.GetFile') {
                       self.uploadAllowed = true;
                       return;
                   }
                });
            }
        }

        function loadFeed(tabIndex) {
            self.errorLoadingFeed = false;
            self.loadingFeedData = true;
            self.model.loaded = false;
            self.loadMessage = '';
            var successFn = function(response) {
                if (response.data) {
                    var promises = {
                        feedPromise: mergeTemplateProperties(response.data),
                        processorTemplatesPromise:  UiComponentsService.getProcessorTemplates()
                    };

                    $q.all(promises).then(function(result) {


                        //deal with the feed data
                        var updatedFeedResponse = result.feedPromise;
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
                                //sort them by name
                                self.model.inputProcessors = _.sortBy(self.model.inputProcessors,'name')

                                self.model.inputProcessor = _.find(self.model.inputProcessors,function(processor){
                                    return angular.isDefined(self.model.inputProcessorName) && self.model.inputProcessorName != null ? self.model.inputProcessorType == processor.type && self.model.inputProcessorName.toLowerCase() == processor.name.toLowerCase() : self.model.inputProcessorType == processor.type;
                                });

                                if(angular.isUndefined(self.model.inputProcessor)){
                                    self.model.inputProcessor = _.find(self.model.inputProcessors,function(processor){
                                        return self.model.inputProcessorType == processor.type;
                                    });
                                }
                                self.model.nonInputProcessors = RegisterTemplateService.removeNonUserEditableProperties(updatedFeedResponse.data.registeredTemplate.nonInputProcessors,false);
                                self.updateMenuOptions();
                                self.loadingFeedData = false;
                                self.model.isStream = updatedFeedResponse.data.registeredTemplate.stream;
                                FeedService.updateEditModelStateIcon();

                                var entityAccessControlled = AccessControlService.isEntityAccessControlled();
                                //Apply the entity access permissions
                                var requests = {
                                    entityEditAccess: entityAccessControlled === true
                                        ? FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, self.model)
                                        : true,
                                    entityExportAccess: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT, self.model),
                                    entityPermissionAccess: entityAccessControlled === true
                                        ? FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.CHANGE_FEED_PERMISSIONS, self.model)
                                        : true,
                                    functionalAccess: AccessControlService.getUserAllowedActions()
                                };
                                $q.all(requests).then(function (response) {
                                    var allowEditAccess =  AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions);
                                    var allowAdminAccess =  AccessControlService.hasAction(AccessControlService.FEEDS_ADMIN, response.functionalAccess.actions);
                                    var slaAccess =  AccessControlService.hasAction(AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                                    var allowExport = AccessControlService.hasAction(AccessControlService.FEEDS_EXPORT, response.functionalAccess.actions);

                                    self.allowEdit = response.entityEditAccess && allowEditAccess;
                                    self.allowChangePermissions = entityAccessControlled && response.entityPermissionAccess && allowEditAccess;
                                    self.allowAdmin = allowAdminAccess;
                                    self.allowSlaAccess = slaAccess;
                                    self.allowExport = response.entityExportAccess && allowExport;
                                });
                            }








                    },function(err){
                        //handle err
                        self.loadingFeedData = false;
                    });
                }
                else {
                    errorFn(" The feed was not found.")
                }
            }
            var errorFn = function(err) {
                self.loadingFeedData = false;
                self.errorLoadingFeed = true;
                var message = angular.isDefined(err) && angular.isString(err) ? err : '';
                $mdDialog.show(
                        $mdDialog.alert()
                                .parent(angular.element(document.querySelector('body')))
                                .clickOutsideToClose(true)
                                .title('Error loading feed')
                                .textContent('Error loading feed. '+message)
                                .ariaLabel('Error loading feed')
                                .ok('Got it!')
                        //.targetEvent(ev)
                );

            }
            var promise = $http.get(RestUrlService.GET_FEEDS_URL + "/" + self.feedId);
            promise.then(successFn, errorFn);
            return promise;
        }

         function nifiRunningCheck(){
            var promise = $http.get(RestUrlService.IS_NIFI_RUNNING_URL);
            promise.then(function(response) {
                self.isNiFiRunning =response.data;
            }, function(err) {
                self.isNiFiRunning = false;
            });
        }

        this.gotoFeedStats = function (ev) {
            ev.preventDefault();
            ev.stopPropagation();
            var feedName = self.model.systemCategoryName + "." + self.model.systemFeedName;
            StateService.OpsManager().Feed().navigateToFeedStats(feedName);
        };

        this.gotoFeedDetails = function (ev) {
            ev.preventDefault();
            ev.stopPropagation();
            var feedName = self.model.systemCategoryName + "." + self.model.systemFeedName;
            StateService.OpsManager().Feed().navigateToFeedDetails(feedName);
        };

        init();
    };

    var FeedUploadFileDialogController = function ($scope, $mdDialog, $http, RestUrlService, FileUpload, feedId){
        var self = this;
        $scope.uploading = false;
        $scope.uploadFile = null;

        /**
         * Upload file
         */
        $scope.doUpload = function() {

            $scope.uploading = true;
            $scope.errorMessage = '';

            var uploadUrl = RestUrlService.UPLOAD_FILE_FEED_URL(feedId);
            var params = {};
            var successFn = function (response) {
                $scope.uploading = false;
                $mdDialog.hide('Upload successfully submitted.');
            }
            var errorFn = function (data) {
                $scope.uploading = false;
                $scope.errorMessage = 'Failed to submit file.';
            }
            FileUpload.uploadFileToUrl($scope.uploadFile, uploadUrl, successFn, errorFn, params);
        };


        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            $mdDialog.cancel();
        };


    };

    angular.module(moduleName).controller('FeedDetailsController', ["$scope","$q","$transition$","$mdDialog","$mdToast","$http","$state","AccessControlService","RestUrlService","FeedService","RegisterTemplateService","StateService","SideNavService","FileUpload","ConfigurationService","EntityAccessControlDialogService","EntityAccessControlService","UiComponentsService",controller]);

    angular.module(moduleName).controller('FeedUploadFileDialogController',["$scope","$mdDialog","$http","RestUrlService","FileUpload","feedId",FeedUploadFileDialogController]);
});
