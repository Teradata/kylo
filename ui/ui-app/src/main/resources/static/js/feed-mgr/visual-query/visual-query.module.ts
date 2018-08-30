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
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatStepperModule} from "@angular/material/stepper";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentFileModule} from "@covalent/core/file";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {TranslateModule} from "@ngx-translate/core";
import {UIRouterModule} from "@uirouter/angular";
import {CodemirrorModule} from "ng2-codemirror";
import {NvD3Module} from "ng2-nvd3";
import {ILazyLoad} from "oclazyload";

import {KyloCommonModule} from "../../common/common.module";
import {KyloFeedManagerModule} from "../feed-mgr.module";
import {DynamicFormModule} from "../shared/dynamic-form/dynamic-form.module";
import {FieldPoliciesModule} from "../shared/field-policies-angular2/field-policies.module";
import {PropertyListModule} from "../shared/property-list/property-list.module";
import {SqlEditorModule} from "../shared/sql-editor/sql-editor.module";
import {BuildQueryComponent} from "./build-query/build-query-ng2.component";
import {ConnectionDialog} from "./build-query/connection-dialog/connection-dialog.component";
import {FlowChartModule} from "./build-query/flow-chart/flow-chart.module";
import {UploadSampleFileComponent} from "./build-query/upload-sample-file.component";
import {VisualQuerySaveService} from "./services/save.service";
import {SparkQueryEngine} from "./services/spark/spark-query-engine";
import {SaveOptionsComponent} from "./store/save-options.component";
import {VisualQueryStoreComponent} from "./store/store.component";
import {AnalyzeColumnDialog} from "./transform-data/profile-stats/analyze-column-dialog";
import {ColumnAnalysisController, HistogramController} from "./transform-data/profile-stats/column-analysis";
import {WranglerDataService} from "./transform-data/services/wrangler-data.service";
import {WranglerTableService} from "./transform-data/services/wrangler-table.service";
import {TransformDataComponent} from "./transform-data/transform-data.component";
import {VisualQueryPainterService} from "./transform-data/visual-query-table/visual-query-painter.service";
import {VisualQueryTable} from "./transform-data/visual-query-table/visual-query-table.component";
import {VisualQueryControlDirective} from "./visual-query-control.directive";
import {visualQueryStates} from "./visual-query-states";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component";
import {INJECTOR} from "./wrangler/api/index";
import {WranglerModule} from "./wrangler/core/wrangler.module";
import {QueryEngineFactory, registerQueryEngine} from "./wrangler/query-engine-factory.service";
import VisualQueryProfileStatsController from "./transform-data/profile-stats/VisualQueryProfileStats";
import {ConnectionErrorValidatorDirective} from "./store/connection-error-validator.directive";

registerQueryEngine('spark', SparkQueryEngine);

@NgModule({
    declarations: [
        AnalyzeColumnDialog,
        BuildQueryComponent,
        ColumnAnalysisController,
        ConnectionDialog,
        ConnectionErrorValidatorDirective,
        HistogramController,
        SaveOptionsComponent,
        TransformDataComponent,
        UploadSampleFileComponent,
        VisualQueryControlDirective,
        VisualQueryProfileStatsController,
        VisualQueryStepperComponent,
        VisualQueryStoreComponent,
        VisualQueryTable
    ],
    entryComponents: [
        AnalyzeColumnDialog,
        ConnectionDialog,
        SaveOptionsComponent,
        VisualQueryProfileStatsController
    ],
    exports: [
        BuildQueryComponent,
        ConnectionDialog,
        UploadSampleFileComponent,
        VisualQueryControlDirective,
        VisualQueryStepperComponent
    ],
    imports: [
        CodemirrorModule,
        CommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentFileModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        DynamicFormModule,
        FieldPoliciesModule,
        FlexLayoutModule,
        FlowChartModule,
        FormsModule,
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
        MatTabsModule,
        MatToolbarModule,
        MatTooltipModule,
        NvD3Module,
        PropertyListModule,
        ReactiveFormsModule,
        SqlEditorModule,
        TranslateModule.forChild(),
        WranglerModule
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
                "js/feed-mgr/visual-query/visual-query.component.css"
            ]
        });
    }
}

@NgModule({
    imports: [
        VisualQueryModule,
        UIRouterModule.forChild({states: visualQueryStates})
    ]
})
export class VisualQueryRouterModule {
}
