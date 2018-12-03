import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";

import {VisualQueryComponent, VisualQueryDirective} from "./angular2";

import AccessConstants from "../../constants/AccessConstants";
import "../../codemirror-require/module"
import { VisualQueryTableHeader } from "./transform-data/visual-query-table/visual-query-table-header.component";
import { VisualQueryPainterService } from "./transform-data/visual-query-table/visual-query-painter.service";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatMenuModule } from "@angular/material/menu";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { MatIconModule } from "@angular/material/icon";
import { TranslateModule } from "@ngx-translate/core";
import { MatTooltipModule } from "@angular/material/tooltip";

const moduleName: string = require("./module-name");

@NgModule({
    declarations: [
        VisualQueryComponent,
        VisualQueryDirective,
        VisualQueryTableHeader
    ],
    entryComponents : [
        VisualQueryTableHeader
    ],
    providers:[VisualQueryPainterService],
    imports: [
        CommonModule,
        MatFormFieldModule,
        MatMenuModule,
        MatButtonModule,
        MatDividerModule,
        MatIconModule,
        TranslateModule,
        MatTooltipModule,
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
