import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";

import {TdLoadingService} from "@covalent/core/loading";
import {Observable} from "rxjs/Observable";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component"
import AccessConstants from "../../constants/AccessConstants";
import {moduleName} from "./module-name";
import {Injector} from "@angular/core";
import {CatalogComponent} from "../catalog/catalog.component";
import {VisualQueryNg2Component} from "./visual-query-ng2.component";
import {VisualQueryComponent} from "./angular2";






export const visualQueryStates: Ng2StateDeclaration[] = [
    {
        name: "visual-query2",
        url: "/visual-query2/:engineName",
        redirectTo: "visual-query2.stepper",
        params: {
            engineName: null
        },
        resolve: [{
            token:"engineName",
            deps:[Injector,StateService, TdLoadingService],
            resolveFn:($injector:Injector,stateService:StateService, loading: TdLoadingService) => {
                // loading.register(VisualQueryComponent.LOADER);
                let engineName = stateService.transition.params().engine;
                if (engineName === null) {
                    engineName = "spark";
                }
                //$injector.get("VisualQueryEngineFactory").getEngine(engineName);
                return engineName;
            }
        }],
        views: {
            "content": {
                component: VisualQueryNg2Component
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Visual Query",
            module: moduleName,
            permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
        }
    },
    {
        name: "visual-query2.stepper",
        url: "/stepper",
        component: VisualQueryStepperComponent
    }



];

/**
 *  data: {
            breadcrumbRoot: true,
            displayName: "Visual Query",
            module: moduleName,
            permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
        }
 */

