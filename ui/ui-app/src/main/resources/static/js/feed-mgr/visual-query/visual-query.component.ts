import {Input, OnDestroy, OnInit} from "@angular/core";
import {IAngularStatic} from "angular";

import {FeedDataTransformation} from "../model/feed-data-transformation";
import {QueryEngine} from "./wrangler/query-engine";

declare const angular: IAngularStatic;

const moduleName = require("feed-mgr/visual-query/module-name");

/**
 * Displays the Visual Query page.
 */
class VisualQueryComponent implements OnInit, OnDestroy {

    /**
     * Query engine and data transformation model
     */
    dataModel: { engine: QueryEngine<any>, model: FeedDataTransformation };

    /**
     * Query engine for the data model
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Constructs a {@code VisualQueryComponent}.
     */
    constructor($scope: angular.IScope, private SideNavService: any, private StateService: any) {
        // Manage the sidebar navigation
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
    }

    $onInit(): void {
        this.ngOnInit();
    }
}

angular.module(moduleName).component('visualQuery', {
    bindings: {
        engine: "<"
    },
    controller: ["$scope", "SideNavService", "StateService", VisualQueryComponent],
    controllerAs: "vm",
    templateUrl: "js/feed-mgr/visual-query/visual-query.component.html"
});
