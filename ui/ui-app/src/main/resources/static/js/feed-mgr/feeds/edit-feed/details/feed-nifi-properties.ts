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
        link: function ($scope: any, element: any, attrs: any, controller: any) {
            if ($scope.versions == undefined) {
                $scope.versions = false;
            }
        }

    };
}


export class FeedNIFIController implements ng.IComponentController {
    /**
     * The ng-form object
     * @type {{}}
     */
    feedDetailsForm: any = {}


    versions: boolean;
    /**
     * Indicates if the feed NiFi properties may be edited.
     * @type {boolean}
     */
    allowEdit: boolean;

    /**
     * The Read only model
     */
    model: any;
    /**
     * Then model when comparing Versions
     */
    versionFeedModel: any;
    /**
     * The model when editing
     * @type {{}}
     */
    editModel: any = {};
    /**
     * Flag to determine if we are editing or not
     * @type {boolean}
     */
    editableSection: boolean = false;

    static $inject = ["$scope", "$http", "$q", "RestUrlService", "AccessControlService", "EntityAccessControlService", "FeedService", "EditFeedNifiPropertiesService", "FeedInputProcessorOptionsFactory", "FeedDetailsProcessorRenderingHelper", "BroadcastService", "FeedPropertyService", "$filter"];

    constructor(private $scope: any, private $http: angular.IHttpService, private $q: angular.IQService, private RestUrlService: any, private AccessControlService: any
        , private EntityAccessControlService: any, private FeedService: any, private EditFeedNifiPropertiesService: any
        , private FeedInputProcessorOptionsFactory: any, private FeedDetailsProcessorRenderingHelper: any,
                private BroadcastService: any, private FeedPropertyService: any, private $filter: angular.IFilterService) {

        this.versions = $scope.versions;
        //dont allow editing if we are looking at versions
        this.allowEdit = !this.versions;
        this.model = this.FeedService.editFeedModel;
        this.versionFeedModel = this.FeedService.versionFeedModel;

    }


    $onInit() {
        this.onInit();
    }

    onInit() {
        if (this.versions) {
            this.$scope.$watch( () =>{
                return this.FeedService.versionFeedModel;
            },  (newVal: any) => {
                this.versionFeedModel = this.FeedService.versionFeedModel;
            });
        }

        this.$scope.$watch(() => {
            return this.FeedService.editFeedModel;
        }, (newVal: any) => {
            //only update the model if it is not set yet
            if (this.model == null) {
                this.model = angular.copy(this.FeedService.editFeedModel);

            }
            //tell the ui what properties to show/hide
            var renderGetTableData = this.FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(this.model.inputProcessor, this.model.nonInputProcessors);
            //update the names for the controller services
            this.updateControllerServiceDisplayName();

        })

        var inputProcessorIdWatch = this.$scope.$watch(() => {
            return this.editModel.inputProcessorId;
        }, (newVal: any) => {
            this.updateInputProcessor(newVal);
        });


        //Apply the entity access permissions
        this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, this.model, this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access: any) => {
            this.allowEdit = !this.versions && access && !this.model.view.feedDetails.disabled
        });
    }




    /**
     * Edit the data
     */
    onEdit() {
        //copy the model
        var inputProcessors = angular.copy(this.FeedService.editFeedModel.inputProcessors);
        var nonInputProcessors = angular.copy(this.FeedService.editFeedModel.nonInputProcessors);
        this.editModel = {};

        var allInputProperties = _.filter(this.model.properties, (property: any) => {
            return property.inputProperty == true;
        });

        var allInputProcessorProperties = _.groupBy(allInputProperties, (property) => {
            return property.processorId;
        })

        var allInputProcessorProperties = angular.copy(allInputProcessorProperties);
        this.editModel.allInputProcessorProperties = allInputProcessorProperties;
        this.editModel.inputProcessors = inputProcessors;
        this.editModel.nonInputProcessors = nonInputProcessors;

        // Find controller services and add in the select options
        _.chain(this.editModel.inputProcessors.concat(this.editModel.nonInputProcessors))
            .pluck("properties")
            .flatten(true)
            .filter((property: any) => {
                return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
            })
            .each(this.FeedService.findControllerServicesForProperty);


        this.editModel.table = angular.copy(this.FeedService.editFeedModel.table);
        this.EditFeedNifiPropertiesService.editFeedModel = this.editModel;
        if (angular.isDefined(this.model.inputProcessor)) {
            this.updateInputProcessor(this.model.inputProcessor.processorId);
            this.editModel.inputProcessorId = this.model.inputProcessor.processorId;
        }
    };

    /**
     * Cancel an Edit
     */
    onCancel() {

    };

    /**
     * Save the editModel
     * @param ev
     */
    onSave(ev: angular.IAngularEvent) {
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

        copy.table.sourceTableIncrementalDateField = this.editModel.table.sourceTableIncrementalDateField;

        //update the db properties

        this.FeedService.saveFeedModel(copy).then((response: any) => {
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

            //Get the updated value from the server.
            this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
        }, (response: any) => {
            this.FeedService.hideFeedSavingDialog();
            console.log('ERRORS were found ', response)
            this.FeedService.buildErrorData(this.model.feedName, response);
            this.FeedService.showFeedErrorsDialog();
            //make it editable
            this.editableSection = true;
        });
    };


    private updateControllerServiceDisplayName() :void {
        if(this.model != null) {
            _.chain(this.model.inputProcessors.concat(this.model.nonInputProcessors))
                .pluck("properties")
                .flatten(true)
                .filter((property: any) => {
                    return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                })
                .each(this.FeedService.setControllerServicePropertyDisplayName);
        }

    }

    private updateInputProcessor(newVal: any) {
        angular.forEach(this.editModel.inputProcessors, (processor) => {
            if (processor.processorId == newVal) {
                //check the type and return the custom form if there is one via a factory
                var renderGetTableData = this.FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(processor, this.editModel.nonInputProcessors);
                if (renderGetTableData) {
                    this.model.table.method = 'EXISTING_TABLE';
                }
                this.editModel.inputProcessor = processor;
                this.editModel.inputProcessorType = processor.type;
                return false;
            }
        })
    }

    diff(path: any) {
        return this.FeedService.diffOperation(path);
    }
}


angular.module(moduleName).controller('FeedNifiPropertiesController', FeedNIFIController);

angular.module(moduleName)
    .directive('thinkbigFeedNifiProperties', directive);
