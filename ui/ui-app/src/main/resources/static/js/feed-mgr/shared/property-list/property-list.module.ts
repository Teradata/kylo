import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatNativeDateModule, MatOptionModule} from "@angular/material/core";
import {MatDividerModule} from "@angular/material/divider";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatListModule} from "@angular/material/list";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";

import {KyloCommonModule} from "../../../common/common.module";
import {PropertyListComponent} from "./property-list.component";


@NgModule({
    declarations: [
        PropertyListComponent
    ],
    exports: [
        PropertyListComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        KyloCommonModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatDividerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatNativeDateModule,
        MatOptionModule,
        MatRadioModule,
        MatSelectModule,
        MatTabsModule,
        MatToolbarModule,
        ReactiveFormsModule,
        FlexLayoutModule
    ],
    providers: []
})
export class PropertyListModule {

}
