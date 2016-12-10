
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require:['thinkbigDefineFeedGeneralInfo','^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/define-feed/feed-details/define-feed-general-info.html',
            controller: "DefineFeedGeneralInfoController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller =  function($scope,$log, $http,$mdToast,RestUrlService, FeedService, CategoriesService) {

        var self = this;

        /**
         * The angular form
         * @type {{}}
         */
        this.defineFeedGeneralForm = {};
        this.templates = [];
        this.model = FeedService.createFeedModel;
        this.isValid = false;
        this.stepNumber = parseInt(this.stepIndex)+1
        this.stepperController = null;

        // Contains existing system feed names for the current category
        this.existingFeedNames = {};

        this.categorySearchText = '';
        this.category;
        self.categorySelectedItemChange = selectedItemChange;
        self.categorySearchTextChanged = searchTextChange;
        self.categoriesService = CategoriesService;

        function searchTextChange(text) {
         //   $log.info('Text changed to ' + text);
        }
        function selectedItemChange(item) {
            if(item != null && item != undefined) {
                self.model.category.name = item.name;
                self.model.category.id = item.id;
                self.model.category.systemName = item.systemName;
                setSecurityGroups(item.name);
                validateUniqueFeedName();
            }
            else {
                self.model.category.name = null;
                self.model.category.id = null;
                self.model.category.systemName = null;
                self.existingFeedNames = {};
                self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
            }
        }

        function existingFeedNameKey(categoryId, feedName) {
            return categoryId + "-" + feedName;
        }

        populateExistingFeedNames();

        /**
         * updates the {@code existingFeedNames} object with the latest feed names from the server
         * @returns {promise}
         */
        function populateExistingFeedNames() {
            return $http.get(RestUrlService.GET_FEEDS_URL).then(function (response) {
                self.existingFeedNames = {};
                angular.forEach(response.data, function (feed) {
                    self.existingFeedNames[existingFeedNameKey(feed.categoryId, feed.systemFeedName)] = feed.systemFeedName;
                });
            });
        }

        function validateUniqueFeedName() {

            function _validate() {
                //validate to ensure the name is unique in this category
                if (self.model && self.model.category && self.existingFeedNames[existingFeedNameKey(self.model.category.id, self.model.systemFeedName)]) {
                    self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', false);
                }
                else {
                    self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                }
            }

            if (_.isEmpty(self.existingFeedNames)) {
                populateExistingFeedNames().then(function () {
                    _validate();
                });
            }
            else {
                _validate();
            }

        }



      //  getRegisteredTemplates();

        function validate(){
           var valid = isNotEmpty(self.model.category.name) && isNotEmpty(self.model.feedName) && isNotEmpty(self.model.templateId);
            self.isValid = valid;
        }

        function setSecurityGroups(newVal) {
            if(newVal) {
                var category = self.categoriesService.findCategoryByName(newVal)
                var securityGroups = category.securityGroups;
                self.model.securityGroups = securityGroups;
            }
        }

        function isNotEmpty(item){
            return item != null && item != undefined && item != '';
        }

        this.onTemplateChange = function() {

        }

        $scope.$watch(function(){
            return self.model.id;
        },function(newVal){
            if(newVal == null) {
                self.category = null;
            }
            else {
                self.category = self.model.category;
            }
        })

       var feedNameWatch = $scope.$watch(function(){
            return self.model.feedName;
        },function(newVal) {
           FeedService.getSystemName(newVal).then(function (response) {
               self.model.systemFeedName = response.data;
               validateUniqueFeedName();
               validate();
           });

        });

        $scope.$watch(function(){
            return self.model.category.name;
        },function(newVal) {

            validate();
        })

      var templateIdWatch =  $scope.$watch(function(){
            return self.model.templateId;
        },function(newVal) {
            validate();
        });

        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        function getRegisteredTemplates() {
            var successFn = function (response) {
                self.templates = response.data;
            }
            var errorFn = function (err) {

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };

            $scope.$on('$destroy',function(){
                feedNameWatch();
                templateIdWatch();
                self.model = null;
            });

    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedGeneralInfoController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedGeneralInfo', directive);

})();
