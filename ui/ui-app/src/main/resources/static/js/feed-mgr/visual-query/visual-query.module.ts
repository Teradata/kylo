import {CommonModule} from "@angular/common";
import {Inject, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDividerModule} from "@angular/material/divider";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatStepperModule} from "@angular/material/stepper";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentFileModule} from "@covalent/core/file";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {UIRouterModule} from "@uirouter/angular";
import "kylo-ui-codemirror"
import {CodemirrorModule} from "ng2-codemirror";
import {ILazyLoad} from "oclazyload";

import {KyloCodeMirrorModule} from "../../codemirror-require/codemirror.module";
import {KyloCommonModule} from "../../common/common.module";
import {KyloFeedManagerModule} from "../feed-mgr.module";
import {DynamicFormModule} from "../shared/dynamic-form/dynamic-form.module";
import {FieldPoliciesModule} from "../shared/field-policies-angular2/field-policies.module";
import {PropertyListModule} from "../shared/property-list/property-list.module";
import {BuildQueryComponent} from "./build-query/build-query-ng2.component";
import {ConnectionDialog} from "./build-query/connection-dialog/connection-dialog.component";
import {FlowChartModule} from "./build-query/flow-chart/flow-chart.module";
import {UploadSampleFileComponent} from "./build-query/upload-sample-file.component";
import {VisualQuerySaveService} from "./services/save.service";
import {SparkQueryEngine} from "./services/spark/spark-query-engine";
import {SaveOptionsComponent} from "./store/save-options.component";
import {VisualQueryStoreComponent} from "./store/store.component";
import {WranglerDataService} from "./transform-data/services/wrangler-data.service";
import {WranglerTableService} from "./transform-data/services/wrangler-table.service";
import {TransformDataComponent} from "./transform-data/transform-data.component";
import {VisualQueryPainterService} from "./transform-data/visual-query-table/visual-query-painter.service";
import {VisualQueryTable} from "./transform-data/visual-query-table/visual-query-table.component";
import {visualQueryStates} from "./visual-query-states";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component";
import {INJECTOR} from "./wrangler/api/index";
import {WranglerModule} from "./wrangler/core/wrangler.module";
import {QueryEngineFactory, registerQueryEngine} from "./wrangler/query-engine-factory.service";

registerQueryEngine('spark', SparkQueryEngine);

@NgModule({
    declarations: [
        BuildQueryComponent,
        ConnectionDialog,
        SaveOptionsComponent,
        TransformDataComponent,
        UploadSampleFileComponent,
        VisualQueryStepperComponent,
        VisualQueryStoreComponent,
        VisualQueryTable
    ],
    exports: [
        BuildQueryComponent,
        ConnectionDialog,
        UploadSampleFileComponent,
        VisualQueryStepperComponent
    ],
    imports: [
        CodemirrorModule,
        CommonModule,
        CovalentDialogsModule,
        CovalentFileModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        DynamicFormModule,
        FieldPoliciesModule,
        FlexLayoutModule,
        FlowChartModule,
        FormsModule,
        KyloCodeMirrorModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatDividerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatMenuModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatRadioModule,
        MatSelectModule,
        MatSidenavModule,
        MatStepperModule,
        MatToolbarModule,
        MatTooltipModule,
        PropertyListModule,
        ReactiveFormsModule,
        WranglerModule,
        UIRouterModule.forChild({states: visualQueryStates})
    ],
    providers: [
        {provide: INJECTOR, useFactory: () => QueryEngineFactory.$$wranglerInjector},
        {provide: "VisualQueryEngineFactory", useClass: QueryEngineFactory},
        VisualQueryPainterService,
        VisualQuerySaveService,
        WranglerDataService,
        WranglerTableService
    ]
})
export class VisualQueryModule {
    constructor(@Inject("$ocLazyLoad") $ocLazyLoad: ILazyLoad) {
        $ocLazyLoad.load({
            files: [
                "bower_components/fattable/fattable.css",
                "js/feed-mgr/visual-query/visual-query.component.css",
                "js/feed-mgr/visual-query/build-query/flowchart/flowchart.css",
                "js/feed-mgr/visual-query/transform-data/transform-data.component.css"
            ]
        });
    }
}
