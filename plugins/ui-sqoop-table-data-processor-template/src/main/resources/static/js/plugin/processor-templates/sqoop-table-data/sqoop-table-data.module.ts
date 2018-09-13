import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {CovalentCommonModule} from "@covalent/core/common";
import {KyloFeedModule, ProcessorControl} from "@kylo/feed";
import {TranslateModule} from "@ngx-translate/core";

import {DescribeTableDialogComponent} from "./describe-table-dialog.component";
import {GetTableDataComponent} from "./get-table-data.component";
import {ImportSqoopComponent} from "./import-sqoop.component";
import {TablePropertiesComponent} from "./table-properties.component";

@NgModule({
    bootstrap: [
        // DescribeTableDialogComponent
    ],
    declarations: [
        DescribeTableDialogComponent,
        GetTableDataComponent,
        ImportSqoopComponent,
        TablePropertiesComponent
    ],
    entryComponents: [
        DescribeTableDialogComponent,
        GetTableDataComponent,
        ImportSqoopComponent
    ],
    exports: [
        // DescribeTableDialogComponent
    ],
    imports: [
        CommonModule,
        CovalentCommonModule,
        FlexLayoutModule,
        FormsModule,
        KyloFeedModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatDialogModule,
        MatIconModule,
        MatInputModule,
        MatProgressSpinnerModule,
        MatRadioModule,
        MatSelectModule,
        ReactiveFormsModule,
        TranslateModule
    ],
    providers: [
        {
            provide: ProcessorControl,
            useValue: new ProcessorControl(GetTableDataComponent, ["com.thinkbiganalytics.nifi.GetTableData", "com.thinkbiganalytics.nifi.v2.ingest.GetTableData"]),
            multi: true
        }, {
            provide: ProcessorControl,
            useValue: new ProcessorControl(ImportSqoopComponent, "com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop"),
            multi: true
        }
    ]
})
export class SqoopTableDataModule {
}
