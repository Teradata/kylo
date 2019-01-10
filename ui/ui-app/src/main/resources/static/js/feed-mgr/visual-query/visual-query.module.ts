import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
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
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatStepperModule} from "@angular/material/stepper";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentFileModule} from "@covalent/core/file";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMessageModule} from "@covalent/core/message";
import {CovalentVirtualScrollModule} from "@covalent/core/virtual-scroll";
import {TranslateModule} from "@ngx-translate/core";
import {UIRouterModule} from "@uirouter/angular";
import {CodemirrorModule} from "ng2-codemirror";
import {NvD3Module} from "ng2-nvd3";
import {DndListModule} from "ngx-drag-and-drop-lists";
import {KyloCodeMirrorModule} from '../../codemirror-require/codemirror.module';

import {KyloCommonModule} from "../../common/common.module";
import {CatalogDatasetPreviewModule} from "../catalog-dataset-preview/catalog-dataset-preview.module";
import {KyloFeedManagerModule} from "../feed-mgr.module";
import {DynamicFormModule} from "../../../lib/dynamic-form/dynamic-form.module";
import {FieldPoliciesModule} from "../shared/field-policies-angular2/field-policies.module";
import {PropertyListModule} from "../shared/property-list/property-list.module";
import {SqlEditorModule} from "../shared/sql-editor/sql-editor.module";
import {BuildQueryComponent} from "./build-query/build-query-ng2.component";
import {ConnectionDialog} from "./build-query/connection-dialog/connection-dialog.component";
import {FlowChartModule} from "./build-query/flow-chart/flow-chart.module";
import {UploadSampleFileComponent} from "./build-query/upload-sample-file.component";
import {VisualQuerySaveService} from "./services/save.service";
import {SparkQueryEngine} from "./services/spark/spark-query-engine";
import {ConnectionErrorValidatorDirective} from "./store/connection-error-validator.directive";
import {SaveOptionsComponent} from "./store/save-options.component";
import {VisualQueryStoreComponent} from "./store/store.component";
import {AnalyzeColumnDialog} from "./transform-data/main-dialogs/analyze-column-dialog";
import {ColumnAnalysisController, HistogramController} from "./transform-data/main-dialogs/column-analysis";
import {QuickCleanDialog} from "./transform-data/main-dialogs/quick-clean-dialog";
import {MiniCategoricalComponent, MiniHistogramComponent} from "./transform-data/main-dialogs/quick-column-components";
import {QuickColumnsDialog} from "./transform-data/main-dialogs/quick-columns-dialog";
import {SampleDialog} from "./transform-data/main-dialogs/sample-dialog";
import {SchemaLayoutDialog} from "./transform-data/main-dialogs/schema-layout-dialog";
import {VisualQueryProfileStatsController} from "./transform-data/main-dialogs/VisualQueryProfileStats";
import {WranglerDataService} from "./transform-data/services/wrangler-data.service";
import {WranglerTableService} from "./transform-data/services/wrangler-table.service";
import {TransformDataComponent} from "./transform-data/transform-data.component";
import {VisualQueryPainterService} from "./transform-data/visual-query-table/visual-query-painter.service";
import {VisualQueryTable} from "./transform-data/visual-query-table/visual-query-table.component";
import {VisualQueryControlDirective} from "./visual-query-control.directive";
import {visualQueryStates} from "./visual-query-states";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component";
import "./visual-query.component.scss";
import {WranglerModule} from "./wrangler/core/wrangler.module";
import {JoinPreviewStepperStep} from "./transform-data/dataset-join-dialog/join-preview-stepper-step";


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
        VisualQueryTable,
        MiniCategoricalComponent,
        MiniHistogramComponent,
        QuickCleanDialog,
        QuickColumnsDialog,
        SampleDialog,
        SchemaLayoutDialog,
        JoinPreviewStepperStep
    ],
    entryComponents: [
        AnalyzeColumnDialog,
        ConnectionDialog,
        SaveOptionsComponent,
        VisualQueryProfileStatsController,
        QuickCleanDialog,
        QuickColumnsDialog,
        SampleDialog,
        SchemaLayoutDialog
    ],
    exports: [
        BuildQueryComponent,
        ConnectionDialog,
        UploadSampleFileComponent,
        VisualQueryControlDirective,
        VisualQueryStepperComponent
    ],
    imports: [
        CatalogDatasetPreviewModule,
        CodemirrorModule,
        CommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentFileModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentMessageModule,
        CovalentVirtualScrollModule,
        DndListModule,
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
        MatSnackBarModule,
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
        VisualQueryPainterService,
        VisualQuerySaveService,
        WranglerDataService,
        WranglerTableService,
        SparkQueryEngine,
    ]
})
export class VisualQueryModule {}

@NgModule({
    imports: [
        VisualQueryModule,
        UIRouterModule.forChild({states: visualQueryStates})
    ]
})
export class VisualQueryRouterModule {
}
