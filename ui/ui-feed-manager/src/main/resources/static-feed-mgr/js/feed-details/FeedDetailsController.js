(function () {

    var controller = function ($scope, $q, $stateParams, $mdDialog, $mdToast, $http, $state, RestUrlService, FeedService, RegisterTemplateService, StateService) {

        var self = this;
        this.feedId = null;
        this.selectedTabIndex = 0;
        var init = function () {
            self.feedId = $stateParams.feedId;
            loadFeed()
        }
        this.loadingFeedData = false;
        this.model = FeedService.editFeedModel;
        this.model.loaded = false;
        this.loadMessage = ''

        $scope.$watch(function () {
            return self.selectedTabIndex;
        }, function (newVal) {

        })

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

        this.enableFeed = function () {
            $http.post(RestUrlService.ENABLE_FEED_URL(self.feedId)).then(function (response) {
                self.model.state = response.data.state;
                FeedService.updateEditModelStateIcon();
            });
        }
        this.disableFeed = function () {
            $http.post(RestUrlService.DISABLE_FEED_URL(self.feedId)).then(function (response) {
                self.model.state = response.data.state;
                FeedService.updateEditModelStateIcon();
            });
        }

        function mergeTemplateProperties(feed) {
            var successFn = function (response) {
                return response;
            }
            var errorFn = function (err) {

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

        this.onCategoryClick = function () {
            StateService.navigateToCategoryDetails(self.model.category.id);
        }

        this.onTableClick = function () {
            StateService.navigateToTable(self.model.category.systemName, self.model.table.tableSchema.name);
        }

        function loadFeed() {
            self.loadingFeedData = true;
            self.model.loaded = false;
            self.loadMessage = '';
            var successFn = function (response) {
                if (response.data) {
                    mergeTemplateProperties(response.data).then(function (updatedFeedResponse) {
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

                            //get those properties that are Input properties
                            var processors = {};
                            var inputProcessors = [];

                            var nonInputProcessors = [];
                            angular.forEach(self.model.properties, function (property) {
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
                        }
                    }, function (err) {
                        //handle err
                        self.loadingFeedData = false;
                    })

                }
            }
            var errorFn = function (err) {
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

