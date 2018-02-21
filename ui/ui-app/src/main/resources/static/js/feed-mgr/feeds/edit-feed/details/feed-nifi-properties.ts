import * as angular from 'angular';
import 'pascalprecht.translate';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');

var directive = function () {
    return {
        restrict: "EA",
        bindToController: {},
        controllerAs: 'vm',
            scope: {
                versions: '=?'
            },
        templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-nifi-properties.html',
        controller: "FeedNifiPropertiesController",
        link: function ($scope:any, element:any, attrs:any, controller:any) {
                if ($scope.versions == undefined) {
                    $scope.versions = false;
                }
        }

    };
}



export class FeedNIFIController implements ng.IComponentController{
// define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {

           /**
             * The ng-form object
             * @type {{}}
             */
            feedDetailsForm:any = {}
            
            
            versions:any = this.$scope.versions;
            /**
             * Indicates if the feed NiFi properties may be edited.
             * @type {boolean}
             */
            allowEdit:boolean = !this.versions;
    
            model:any = this.FeedService.editFeedModel;
            versionFeedModel:any = this.FeedService.versionFeedModel;
            editModel:any = {};
            editableSection:boolean = false;
            INCREMENTAL_DATE_PROPERTY_KEY:string = 'Date Field';
    
            onEdit = function () {
                //copy the model
                var inputProcessors = angular.copy(this.FeedService.editFeedModel.inputProcessors);
                var nonInputProcessors = angular.copy(this.FeedService.editFeedModel.nonInputProcessors);
                this.editModel = {};
    
                var allInputProperties = _.filter(this.model.properties, (property:any) => {
                    return property.inputProperty == true;
                });
    
                var allInputProcessorProperties = _.groupBy(allInputProperties, (property) => {
                    return property.processorId;
                })
    
                var allInputProcessorProperties = angular.copy(allInputProcessorProperties);
                this.editModel.allInputProcessorProperties = allInputProcessorProperties;
                this.editModel.inputProcessors = inputProcessors;
                this.editModel.nonInputProcessors = nonInputProcessors;
    
                // Find controller services
                _.chain(this.editModel.inputProcessors.concat(this.editModel.nonInputProcessors ))
                    .pluck("properties")
                    .flatten(true)
                    .filter((property:any) => {
                        return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                    })
                    .each(this.FeedService.findControllerServicesForProperty);
    
    
    
                //NEED TO COPY IN TABLE PROPS HERE
                this.editModel.table = angular.copy(this.FeedService.editFeedModel.table);
                this.EditFeedNifiPropertiesService.editFeedModel = this.editModel;
                if (angular.isDefined(this.model.inputProcessor)) {
                    this.updateInputProcessor(this.model.inputProcessor.processorId);
                    this.editModel.inputProcessorId = this.model.inputProcessor.processorId;
                }
            };
            onCancel = function () {
                
            };

            onSave = function (ev:any) {
                this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-nifi-properties.Saving'), this.model.feedName);
    
                var copy = angular.copy(this.FeedService.editFeedModel);
    
                copy.inputProcessors = this.editModel.inputProcessors;
                copy.nonInputProcessors = this.editModel.nonInputProcessors;
                copy.inputProcessorId = this.editModel.inputProcessorId;
                copy.inputProcessor = this.editModel.inputProcessor;
                copy.inputProcessorType = this.editModel.inputProcessorType;
                copy.userProperties = null;
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;
    
                //table type is edited here so need to update that prop as well
                copy.table.tableType = this.editModel.table.tableType
    
                if (copy.table.incrementalDateField) {
                    var dateProperty:any = this.findIncrementalDateFieldProperty();
                    if(dateProperty) {
                        dateProperty.value = this.editModel.table.incrementalDateField;
                    }
                    copy.table.incrementalDateField = this.editModel.table.incrementalDateField;
                }
    
                //update the db properties
    
                this.FeedService.saveFeedModel(copy).then( (response:any) => {
                    this.FeedService.hideFeedSavingDialog();
    
                    this.editableSection = false;
    
    
                    this.model.inputProcessors = this.editModel.inputProcessors;
                    this.model.nonInputProcessors = this.editModel.nonInputProcessors;
                    this.model.inputProcessorId = this.editModel.inputProcessorId;
                    this.model.inputProcessor = this.editModel.inputProcessor;
                    this.model.table.tableType = this.editModel.table.tableType;
                    this.model.table.incrementalDateField = this.editModel.table.incrementalDateField;
                    this.model.inputProcessorType = this.editModel.inputProcessorType;
    
                    this.FeedPropertyService.updateDisplayValueForProcessors(this.model.inputProcessors);
                    this.FeedPropertyService.updateDisplayValueForProcessors(this.model.nonInputProcessors);
    
                    this.updateControllerServiceProperties();
                    //update the displayValue
                //Get the updated value from the server.
                this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
                }, (response:any) => {
                    this.FeedService.hideFeedSavingDialog();
                    console.log('ERRORS were found ', response)
                    this.FeedService.buildErrorData(this.model.feedName, response);
                    this.FeedService.showFeedErrorsDialog();
                    //make it editable
                    this.editableSection = true;
                });
            };


    constructor(private $scope :any, private $http:any,private $q:any,private RestUrlService:any,private AccessControlService:any
        ,private EntityAccessControlService:any, private FeedService:any, private EditFeedNifiPropertiesService:any
        ,private FeedInputProcessorOptionsFactory:any, private FeedDetailsProcessorRenderingHelper:any, 
        private BroadcastService:any, private FeedPropertyService:any, private $filter:any) {

            var self = this;
            
        if (self.versions) {
            $scope.$watch(function(){
                return self.FeedService.versionFeedModel;
            },function(newVal:any) {
                self.versionFeedModel = self.FeedService.versionFeedModel;
            });
        }
    
            $scope.$watch(()=> {
                return FeedService.editFeedModel;
            }, (newVal:any) =>{
                //only update the model if it is not set yet
                if (self.model == null) {
                    self.model = angular.copy(FeedService.editFeedModel);
    
                }
                //tell the ui what properties to show/hide
                var renderGetTableData = FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(self.model.inputProcessor, self.model.nonInputProcessors);
                //   var renderSqoop = FeedDetailsProcessorRenderingHelper.updateSqoopProcessorRendering(self.model.inputProcessor, self.model.nonInputProcessors);
                updateControllerServiceProperties();
            })
    
            var inputProcessorIdWatch = $scope.$watch( ()=> {
                return self.editModel.inputProcessorId;
            }, (newVal:any) => {
                updateInputProcessor(newVal);
                //validate();
            });
    
            function updateInputProcessor(newVal:any) {
                angular.forEach(self.editModel.inputProcessors,  (processor) => {
                    if (processor.processorId == newVal) {
                        //check the type and return the custom form if there is one via a factory
                        var renderGetTableData = FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(processor, self.editModel.nonInputProcessors);
                        //   var renderSqoop = FeedDetailsProcessorRenderingHelper.updateSqoopProcessorRendering(processor, self.editModel.nonInputProcessors);
                        if (renderGetTableData) {
                            self.model.table.method = 'EXISTING_TABLE';
                        }
                        self.editModel.inputProcessor = processor;
                        self.editModel.inputProcessorType = processor.type;
                        return false;
                    }
                })
            }
    
            function findIncrementalDateFieldProperty() {
                return findProperty(self.INCREMENTAL_DATE_PROPERTY_KEY);
            }
    
            /**
             * add the select options to controller services
             */
            function updateControllerServiceProperties(){
    
                    _.filter(self.model.nonInputProcessors, (processor:any) =>{
                    if(processor && processor.properties){
                        var props = _.filter(processor.properties,(property:any) =>{
                            if(isControllerServiceProperty(property)){
                                setControllerServicePropertyDisplayName(property);
                                return true
                            }
                        });
                        return true;
                    }

                });
    
                _.filter(self.model.inputProcessor, (processor:any) => {
                    if(processor && processor.properties){
                        var props = _.filter(processor.properties,(property:any) =>{
                            if(isControllerServiceProperty(property)){
                                setControllerServicePropertyDisplayName(property);
                                return true;
                            }
                        });
                        return true;
                    }
                });
    
    
            }
    
            /**
             * determine if a property is a controller service
             * @param property
             * @returns {boolean}
             */
            function isControllerServiceProperty(property:any){
                var controllerService = property.propertyDescriptor.identifiesControllerService;
                if(controllerService != null && controllerService != undefined && controllerService != ''){
                    return true;
                }
                return false;
            }
    
            /**
             * add the proper select values to controller services
             * @param property
             */
            function setControllerServicePropertyDisplayName(property:any){
                var controllerService = property.propertyDescriptor.identifiesControllerService;
                if(controllerService != null && controllerService != undefined && controllerService != ''){
                    //fetch the name
                    var promise = $http.get(RestUrlService.GET_CONTROLLER_SERVICE_URL(property.value));
                    promise.then((response:any) => {
                        if(response && response.data ){
                            property.displayValue = response.data.name;
                            //set the allowable values on the property
                            if(property.propertyDescriptor.allowableValues == null){
                                property.propertyDescriptor.allowableValues = [];
                                property.propertyDescriptor.allowableValues.push({value:property.value,displayName:property.displayValue});
                            }
                        }
                    }, (err:any)=>{
                        //unable to fetch controller service... the id will display
                    });
    
    
                }
            }
    
            function findProperty(key:any) {
                return _.find(self.model.allProperties, (property:any) => {
                    //return property.key = 'Source Database Connection';
                    return property.key == key;
                });
            }
    
            //Apply the entity access permissions
            $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access:any) => {
                self.allowEdit = !self.versions && access && !self.model.view.feedDetails.disabled
            });
    

    }

        diff(path:any) {
            return this.FeedService.diffOperation(path);
        }

}
angular.module(moduleName).controller('FeedNifiPropertiesController', ["$scope","$http","$q","RestUrlService","AccessControlService","EntityAccessControlService","FeedService","EditFeedNifiPropertiesService","FeedInputProcessorOptionsFactory","FeedDetailsProcessorRenderingHelper","BroadcastService","FeedPropertyService", "$filter",FeedNIFIController]);

angular.module(moduleName)
    .directive('thinkbigFeedNifiProperties', directive);
