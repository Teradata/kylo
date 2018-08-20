/**
 * This is the ng1 module descriptor
 * This will eventually go away and be replaced by the visual-query-ng2.module.ts
 *
 */
import "kylo-ui-codemirror"
import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";


import {VisualQueryComponent, VisualQueryDirective} from "./angular2";

import AccessConstants from "../../constants/AccessConstants";
import {registerQueryEngine} from "./wrangler/query-engine-factory.service";
import {SparkQueryEngine} from "./services/spark/spark-query-engine";

registerQueryEngine('spark',SparkQueryEngine)
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
                    module: "kylo.feedmgr.vq",
                    permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
                }
            }]
        })
    ],
    exports:[
        VisualQueryComponent,
        VisualQueryDirective
    ]
})
export class VisualQueryModule {

    constructor(injector: Injector) {
        console.log("loading vq ng1 module")
        // Lazy load AngularJS module and entry component
        require("./module");
        injector.get("$ocLazyLoad").inject("kylo.feedmgr.vq");
        //require ng1 module
        require("./module-require");
        require("./visual-query.component");



    }
}
