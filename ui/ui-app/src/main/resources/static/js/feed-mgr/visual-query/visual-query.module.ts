import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";

import {VisualQueryComponent, VisualQueryDirective} from "./angular2";

const AccessConstants: any = require("../../constants/AccessConstants");
const moduleName: string = require("./module-name");

@NgModule({
    declarations: [
        VisualQueryComponent,
        VisualQueryDirective
    ],
    imports: [
        CommonModule,
        UIRouterModule.forChild({
            states: [{
                name: "visual-query",
                url: "/visual-query/{engine}",
                params: {
                    engine: null
                },
                views: {
                    "content": {
                        component: VisualQueryComponent
                    }
                },
                resolve: {
                    engine: function ($injector: any, $ocLazyLoad: any, $transition$: any) {
                        let engineName = $transition$.params().engine;
                        if (engineName === null) {
                            engineName = "spark";
                        }

                        return $ocLazyLoad.load("feed-mgr/visual-query/module-require")
                            .then(function () {
                                return $injector.get("VisualQueryEngineFactory").getEngine(engineName);
                            });
                    }
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: "Visual Query",
                    module: moduleName,
                    permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
                }
            }]
        })
    ]
})
export class VisualQueryModule {

    constructor(injector: Injector) {
        // Lazy load AngularJS module and entry component
        require("./module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");
        require("./visual-query.component");
    }
}
