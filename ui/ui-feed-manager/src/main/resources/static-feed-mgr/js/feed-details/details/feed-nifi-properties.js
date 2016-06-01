(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-nifi-properties.html',
            controller: "FeedNifiPropertiesController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, FeedService, EditFeedNifiPropertiesService, FeedInputProcessorOptionsFactory) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.editModel = {};
        this.INCREMENTAL_DATE_PROPERTY_KEY = 'Date Field';

        $scope.$watch(function () {
            return FeedService.editFeedModel;
        }, function (newVal) {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = angular.copy(FeedService.editFeedModel);
            }
        })

        var inputProcessorIdWatch = $scope.$watch(function () {
            return self.editModel.inputProcessorId;
        }, function (newVal) {
            updateInputProcessor(newVal);
            //validate();
        });

        function setInputProcessorFeedPropertiesUrl(processor) {

            if (processor.feedPropertiesUrl == undefined) {
                processor.feedPropertiesUrl = null;
            }
            if (processor.feedPropertiesUrl == null) {
                processor.feedPropertiesUrl = FeedInputProcessorOptionsFactory.templateForProcessor(processor, 'edit');
            }
        }

        function updateInputProcessor(newVal) {
            angular.forEach(self.editModel.inputProcessors, function (processor) {
                if (processor.processorId == newVal) {
                    //check the type and return the custom form if there is one via a factory
                    self.editModel.inputProcessor = processor;
                    self.editModel.inputProcessorType = processor.type;
                    setInputProcessorFeedPropertiesUrl(processor);
                    return false;
                }
            })
        }

        function findIncrementalDateFieldProperty() {
            return findProperty(self.INCREMENTAL_DATE_PROPERTY_KEY);
        }

        function findProperty(key) {
            return _.find(self.model.inputProcessor.properties, function (property) {
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
        }

        this.onEdit = function () {
            //copy the model
            var inputProcessors = angular.copy(FeedService.editFeedModel.inputProcessors);
            var nonInputProcessors = angular.copy(FeedService.editFeedModel.nonInputProcessors);
            self.editModel = {};

            var allInputProperties = _.filter(self.model.properties, function (property) {
                return property.inputProperty == true;
            });

            var allInputProcessorProperties = _.groupBy(allInputProperties, function (property) {
                return property.processorId;
            })

            var allInputProcessorProperties = angular.copy(allInputProcessorProperties);
            self.editModel.allInputProcessorProperties = allInputProcessorProperties;
            self.editModel.inputProcessors = inputProcessors;
            self.editModel.nonInputProcessors = nonInputProcessors;
            //NEED TO COPY IN TABLE PROPS HERE
            self.editModel.table = angular.copy(FeedService.editFeedModel.table);
            EditFeedNifiPropertiesService.editFeedModel = self.editModel;
            updateInputProcessor(self.model.inputProcessor.processorId);

            self.editModel.inputProcessorId = self.model.inputProcessor.processorId

        }

        this.onCancel = function () {

        }
        this.onSave = function () {
            //save changes to the model
            self.model.inputProcessors = self.editModel.inputProcessors;
            self.model.nonInputProcessors = self.editModel.nonInputProcessors;
            self.model.inputProcessorId = self.editModel.inputProcessorId;
            self.model.inputProcessor = self.editModel.inputProcessor;

            //table type is edited here so need tup update that prop as well
            self.model.table.tableType = self.editModel.table.tableType

            if (self.editModel.table.incrementalDateField) {
                findIncrementalDateFieldProperty().value = self.editModel.table.incrementalDateField;
                self.model.table.incrementalDateField = self.editModel.table.incrementalDateField;
            }

            //update the db properties

            FeedService.saveFeedModel(self.model);
        }

    };

    angular.module(MODULE_FEED_MGR).controller('FeedNifiPropertiesController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedNifiProperties', directive);

})();
