(function() {

    var controller = function($scope, $stateParams, $http, AccessControlService, FeedService, RestUrlService, StateService) {

        var self = this;

        /**
         * Indicates if feeds may be imported from an archive.
         * @type {boolean}
         */
        self.allowImport = false;

        //     StateService.navigateToFeeds();

        this.layout = 'first'
        this.stepperUrl = null;
        this.totalSteps = null;
        this.template = null;
        self.model = FeedService.createFeedModel;

        var self = this;
        self.allTemplates = [];
        self.firstTemplates = [];
        self.displayMoreLink = false;
        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        function getRegisteredTemplates() {
            var successFn = function(response) {

                if (response.data) {

                    var data = _.chain(response.data).filter(function (template) {
                        return template.state == 'ENABLED'
                    }).sortBy('templateName').sortBy(function (template) {
                        if (template.templateName == 'Data Ingest') {
                            return 0;
                        }
                        else if (template.templateName == 'Archive Data') {
                            return 1;
                        }
                        else if (template.templateName == 'Data Transformation') {
                            return 2;
                        }
                        else {
                            return 3;
                        }
                    })
                            .value();

                    if (data.length > 1) {
                        self.displayMoreLink = true;
                    }
                    self.allTemplates = data;
                    self.firstTemplates = _.first(data, 3);

                }

            }
            var errorFn = function(err) {

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };

        this.more = function() {
            this.layout = 'all';
        }

        this.gotoImportFeed = function() {
            StateService.navigatetoImportFeed();
        }
        getRegisteredTemplates();

        this.selectTemplate = function(template) {
            self.model.templateId = template.id;
            self.model.templateName = template.templateName;
            //setup some initial data points for the template
            self.model.defineTable = template.defineTable;
            self.model.allowPreconditions = template.allowPreconditions;
            self.model.dataTransformationFeed = template.dataTransformation;
            if (template.defineTable) {
                self.totalSteps = 6;
                self.stepperUrl = 'js/define-feed/define-feed-stepper.html'
            }
            else if (template.dataTransformation) {
                self.totalSteps = 8;
                self.stepperUrl = 'js/define-feed/define-feed-data-transform-stepper.html'
            }
            else {
                self.totalSteps = 4;
                self.stepperUrl = 'js/define-feed/define-feed-no-table-stepper.html'
            }
        }

        self.cancelStepper = function() {
            //or just reset the url
            FeedService.resetFeed();
            self.stepperUrl = null;
            //StateService.navigateToFeeds();
        }

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowImport= AccessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions);
                });
    };

    angular.module(MODULE_FEED_MGR).controller('DefineFeedController', controller);

}());
