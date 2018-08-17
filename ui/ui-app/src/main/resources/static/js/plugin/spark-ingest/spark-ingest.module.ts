import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatDialogModule} from "@angular/material/dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatListModule} from "@angular/material/list";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentMediaModule} from "@covalent/core/media";
import {CovalentDynamicFormsModule} from "@covalent/dynamic-forms";

import {KyloCommonModule} from "../../common/common.module";
import {BrowseDialog} from "./browse.component";
import {ConfigureConnectionComponent} from "./configure-connection.component";
import {PreviewSchemaComponent} from "./preview-schema.component";
import {SelectConnectorComponent} from "./select-connector.component";

@NgModule({
    declarations: [
        BrowseDialog,
        ConfigureConnectionComponent,
        PreviewSchemaComponent,
        SelectConnectorComponent
    ],
    entryComponents: [
        BrowseDialog,
        ConfigureConnectionComponent,
        PreviewSchemaComponent,
        SelectConnectorComponent
    ],
    imports: [
        CommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentDynamicFormsModule,
        CovalentMediaModule,
        FlexLayoutModule,
        FormsModule,
        KyloCommonModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatCardModule,
        MatDialogModule,
        MatDividerModule,
        MatGridListModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule
    ]
})
export class SparkIngestModule {
}
