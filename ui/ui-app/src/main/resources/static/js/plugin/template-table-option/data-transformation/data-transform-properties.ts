import * as angular from 'angular';

export class DataTransformPropertiesController implements ng.IComponentController{

    constructor(private $scope: any,
                private $q: any, 
                private AccessControlService: any, 
                private FeedService: any,
                private StateService: any,
                private VisualQueryService: any){
        // Watch for model changes
        this.$scope.$watch( ()=> { return FeedService.editFeedModel;},
                            ()=>{
                                //only update the model if it is not set yet
                                if (this.model == null) {
                                    this.model = angular.copy(FeedService.editFeedModel);
                                }
        });

        /**
         * Navigates to the Edit Feed page for the current feed.
         */
        navigateToEditFeedInStepper = => () {
            this.VisualQueryService.resetModel();
            this.StateService.FeedManager().Feed().navigateToEditFeedInStepper(this.model.feedId);
        };

        //Apply the entity access permissions
        this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, 
                                                             this.model, 
                                                             this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS))
                                              .then((access)=>{
                                                         this.allowEdit = access;
                                                        });
                 }

        /**
         * Indicates if the feed NiFi properties may be edited.
         * @type {boolean}
         */
        allowEdit: boolean = false;

        /**
         * Indicates that the editable section is visible.
         * @type {boolean}
         */
        editableSection: boolean = false;

        /**
         * Feed model.
         * @type {Object}
         */
        model: any = this.FeedService.editFeedModel;

}
export class kyloDataTransformProperties extends ng.IDirective
{
    constructor(){
          return {
            controller: "DataTransformPropertiesController",
            controllerAs: "vm",
            restrict: "E",
            templateUrl: "js/plugin/template-table-option/data-transformation/data-transform-properties.html"
        };
        }
}

  angular.module("kylo.plugin.template-table-option.data-transformation", [])
        .controller('DataTransformPropertiesController', 
                    ["$scope", 
                    "$q", 
                    "AccessControlService", 
                    "FeedService", 
                    "StateService", 
                    "VisualQueryService", 
                    DataTransformPropertiesController])
        .directive('kyloDataTransformProperties', kyloDataTransformProperties);