define(["angular",  "/example-plugin-1.0/module-name"], function (angular, moduleName) {

    var directive = function () {
        return {
            controller: "ExampleFeedDetailsPreStepController",
            controllerAs: "vm",
            templateUrl: "/example-plugin-1.0/example-feed-details-pre-step/example-feed-details-pre-step.html"
        }
    };

    function controller($scope, $q, AccessControlService, FeedService) {
        var self = this;

        // Indicates if the model may be edited
        self.allowEdit = false;

        self.editableSection = false;
        self.editModel = {};

        self.isValid = false;

        self.model = FeedService.editFeedModel;
        $scope.$watch(function() {
            return FeedService.editFeedModel;
        }, function(newVal) {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        });

        // Copy model for editing
        this.onEdit = function() {
            self.editModel = {
                tableOption: angular.copy(self.model.tableOption)
            };
            setDefaults(self.editModel);

        };

        var setDefaults = function(model){
            var defaults = {"preStepName":"","preStepAge":0,"preStepMagicWord":""}
            _.each(defaults,function(value,key){
             if(angular.isUndefined(model.tableOption[key])){
                 model.tableOption[key] = value;
             }
            });
        }

        // Save changes to the model
        this.onSave = function(ev) {
            FeedService.showFeedSavingDialog(ev, "Saving...", self.model.feedName);

            var copy = angular.copy(FeedService.editFeedModel);
            copy.tableOption = self.editModel.tableOption;

            FeedService.saveFeedModel(copy).then(function() {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.tableOption = self.editModel.tableOption;
            }, function(response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        };

        /**
         * Enforce min age limit
         */
        this.ageChanged = function(){
            var age = self.editModel.tableOption.preStepAge;
            if(angular.isDefined(age) && parseInt(age) >=21) {
                self.form['preStepAge'].$setValidity('ageError', true);
            }
            else {
                self.form['preStepAge'].$setValidity('ageError', false);

            }

        }

        this.magicWordChanged = function(){
            var magicWord = self.editModel.tableOption.preStepMagicWord;
            if(angular.isDefined(magicWord) && magicWord != null) {
                if (magicWord.toLowerCase() == 'kylo') {
                    self.form['preStepMagicWord'].$setValidity('magicWordError', true);
                }
                else {
                    self.form['preStepMagicWord'].$setValidity('magicWordError', false);
                }
            }
        }



        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function(access) {
            self.allowEdit = access;
        });
    }
    angular.module(moduleName)
        .controller("ExampleFeedDetailsPreStepController", ["$scope", "$q", "AccessControlService", "FeedService", controller])
        .directive("exampleFeedDetailsPreStep", directive);

});
