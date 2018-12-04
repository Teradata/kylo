import {Injector} from "@angular/core";
import {Ng2StateDeclaration, StateService} from "@uirouter/angular";

import AccessConstants from "../../constants/AccessConstants";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component"
import {CloneUtil} from "../../common/utils/clone-util";

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
            resolveFn: resolveEngineName
        }],
        views: {
            "content": {
                component: VisualQueryStepperComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Wrangler",
            permissionsKey:"VISUAL_QUERY"
        }
    }
];


export function resolveEngineName($injector: Injector, stateService: StateService) {
    let engineName = stateService.transition.params().engine;
    if (engineName === null) {
        engineName = "spark";
    }
    return engineName;
}
