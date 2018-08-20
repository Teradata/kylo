import {CommonModule} from "@angular/common";
import {FactoryProvider, Injector, NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";


import {VisualQueryStepperComponent} from "./visual-query-stepper.component"

import AccessConstants from "../../constants/AccessConstants";
import {moduleName} from "./module-name";
import {visualQueryStates} from "./visual-query-states";
import {MatStepperModule} from "@angular/material/stepper";
import {MatCardModule} from "@angular/material/card";
import {DynamicFormModule} from "../shared/dynamic-form/dynamic-form.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatListModule} from "@angular/material/list";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {MatSelectModule} from "@angular/material/select";
import {MatDividerModule} from "@angular/material/divider";
//import {queryEngineFactoryProvider} from "./angular2"
import {WranglerModule} from "./wrangler/core/wrangler.module";
import {KyloCommonModule} from "../../common/common.module";
import {KyloFeedManagerModule} from "../feed-mgr.module";
import {MatButtonModule} from "@angular/material/button";
import {INJECTOR} from "./wrangler/api/index";
import {BuildQueryComponent} from "./build-query/build-query-ng2.component";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {UploadSampleFileComponent} from "./build-query/upload-sample-file.component";
import {CovalentFileModule} from "@covalent/core/file";
import {FieldPoliciesModule} from "../shared/field-policies-angular2/field-policies.module";
import {FlowChartModule} from "./build-query/flow-chart/flow-chart.module";
import {VisualQueryNg2Component} from "./visual-query-ng2.component";
import {SparkQueryEngine} from "./services/spark/spark-query-engine";
import {QueryEngineFactory, registerQueryEngine} from "./wrangler/query-engine-factory.service";
import {ConnectionDialog} from "./build-query/connection-dialog/connection-dialog.component";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {FlexLayoutModule} from "@angular/flex-layout";

registerQueryEngine('spark',SparkQueryEngine)


@NgModule({
    declarations: [
        BuildQueryComponent,
        ConnectionDialog,
        UploadSampleFileComponent,
        VisualQueryNg2Component,
        VisualQueryStepperComponent

    ],
    entryComponents:[
        ConnectionDialog
    ],
    imports: [
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
        KyloCommonModule,
        KyloFeedManagerModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatCardModule,
        MatDividerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatSelectModule,
        MatStepperModule,
        MatToolbarModule,
        ReactiveFormsModule,
        WranglerModule,
        UIRouterModule.forChild({states: visualQueryStates})
    ],
    providers:[
        {provide:"VisualQueryEngineFactory", useClass: QueryEngineFactory},
        {provide: INJECTOR, useFactory: function() {
                return QueryEngineFactory.$$wranglerInjector;
            }, deps: []}
    ],
    exports:[
        BuildQueryComponent,
        ConnectionDialog,
        UploadSampleFileComponent,
        VisualQueryNg2Component,
        VisualQueryStepperComponent
    ]
})
export class VisualQueryNg2Module {

    constructor(injector: Injector) {
        console.log("Loading VisualQueryModule ng2")
       injector.get("$ocLazyLoad").inject(moduleName);
       //still need ng1 module for some classes such as the DatasourcesProviderService
        // Lazy load AngularJS module and entry component
        require("./module");
        //add in the ng2 stuff
        require("./module-require-ng2");

    }
}
