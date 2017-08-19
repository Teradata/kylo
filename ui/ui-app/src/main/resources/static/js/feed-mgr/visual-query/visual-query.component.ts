import {OnDestroy} from "@angular/core";
import {QueryEngineFactory} from "./services/query-engine-factory.service";
import {QueryEngine} from "./services/query-engine";
import "./services/query-engine-factory.service";
import {FeedDataTransformation} from "../model/feed-data-transformation";

declare const angular: angular.IAngularStatic;

const moduleName = require("feed-mgr/visual-query/module-name");

/**
 * Displays the Visual Query page.
 */
class VisualQueryComponent implements OnDestroy {

    /**
     * Query engine and data transformation model
     */
    dataModel: {engine: QueryEngine<any>, model: FeedDataTransformation};

    /**
     * Constructs a {@code VisualQueryComponent}.
     */
    constructor($scope: angular.IScope, $transition$: any, queryEngineFactory: QueryEngineFactory, private SideNavService: any, private StateService: any) {
        // Create the query engine and data model
        let engine = this.createEngine($transition$.params().engine, queryEngineFactory);
        this.dataModel = {engine: engine, model: {} as FeedDataTransformation};

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

    /**
     * Creates a new query engine from the specified factory.
     *
     * @param name - the path parameter or name of the engine
     * @param factory - the query engine factory
     * @returns the query engine
     */
    private createEngine(name: string, factory: QueryEngineFactory): QueryEngine<any> {
        const engineName = (function (name) {
            if (name === null) {
                return "spark";
            } else if (name.startsWith("/")) {
                return name.substr(1);
            } else {
                return name;
            }
        })(name);
        return factory.getEngine(engineName);
    }
}

angular.module(moduleName).controller("VisualQueryComponent", ["$scope", "$transition$", "VisualQueryEngineFactory", "SideNavService", "StateService", VisualQueryComponent]);
