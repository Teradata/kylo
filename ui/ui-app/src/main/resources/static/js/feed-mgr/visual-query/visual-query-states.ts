import {Injector} from "@angular/core";
import {Ng2StateDeclaration, StateService} from "@uirouter/angular";

import AccessConstants from "../../constants/AccessConstants";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component"

export const visualQueryStates: Ng2StateDeclaration[] = [
    {
        name: "visual-query",
        url: "/visual-query/:engineName",
        params: {
            engineName: null
        },
        resolve: [{
            token: "engineName",
            deps: [Injector, StateService],
            resolveFn: ($injector: Injector, stateService: StateService) => {
                let engineName = stateService.transition.params().engine;
                if (engineName === null) {
                    engineName = "spark";
                }
                return engineName;
            }
        }],
        views: {
            "content": {
                component: VisualQueryStepperComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Wrangler",
            permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
        }
    }
];
