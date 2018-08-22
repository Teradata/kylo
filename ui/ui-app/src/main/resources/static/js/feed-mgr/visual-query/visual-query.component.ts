import * as angular from "angular";

import {FeedDataTransformation} from "../model/feed-data-transformation";
import {QueryEngine} from "./wrangler/query-engine";
import {PreviewDatasetCollectionService} from "../catalog/api/services/preview-dataset-collection.service";

import {moduleName} from "../module-name";

/**
 * Displays the Visual Query page for AngularJS.
 */
angular.module(moduleName).component('visualQuery', {
    bindings: {
        engine: "<"
    },
    controller: class {
        /**
         * Query engine and data transformation model
         */
        dataModel: { engine: QueryEngine<any>, model: FeedDataTransformation };

        /**
         * Query engine for the data model
         */
        engine: QueryEngine<any>;

        static readonly $inject = ["$scope", "SideNavService", "StateService","PreviewDatasetCollectionService"];

        /**
         * Constructs a {@code VisualQueryComponent}.
         */
        constructor($scope: angular.IScope, private SideNavService: any, private StateService: any, private previewDataSetCollectionService : PreviewDatasetCollectionService) {
            // Manage the sidebar navigation
            console.log("PreviewDatasetCollectionService",this.previewDataSetCollectionService.datasets)
            SideNavService.hideSideNav();
            $scope.$on("$destroy", this.ngOnDestroy.bind(this));
        }

        /**
         * Navigates to the Feeds page when the stepper is cancelled.
         */
        cancelStepper() {
            this.StateService.navigateToHome();
        }

        /**
         * Resets the side state.
         */
        ngOnDestroy(): void {
            this.SideNavService.showSideNav();
        }

        ngOnInit(): void {
            this.dataModel = {engine: this.engine, model: {} as FeedDataTransformation};
            let collection = this.previewDataSetCollectionService.getSparkDataSets();
            this.dataModel.model.datasets = collection;
            console.log('collection',collection)
        }

        $onInit(): void {
            this.ngOnInit();
        }
    },
    controllerAs: "vm",
    template: `
        <thinkbig-stepper total-steps="3" stepper-name="VisualQueryStepper" core-data-model="vm.dataModel" on-cancel-stepper="vm.cancelStepper()"
                          template-url="js/feed-mgr/visual-query/visual-query.stepper.html"></thinkbig-stepper>
    `
});
