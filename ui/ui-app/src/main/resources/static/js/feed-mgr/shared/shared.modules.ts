import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatGridListModule} from '@angular/material/grid-list';
import { CovalentDialogsModule } from '@covalent/core/dialogs';


import { FormsModule } from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSnackBarModule} from '@angular/material/snack-bar';

import {BrowserModule} from "@angular/platform-browser";

import { CovalentCommonModule } from '@covalent/core/common';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";

import {TranslateModule} from "@ngx-translate/core";

import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";

import {KyloFeedManagerModule} from "../feed-mgr.module";
import * as angular from "angular";
import { CovalentSearchModule } from "@covalent/core/search";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import { ReactiveFormsModule } from '@angular/forms';

import { PropertiesAdminEditorController } from "../shared/properties-admin/properties-admin-editor.component";
import { PropertiesAdminController } from "../shared/properties-admin/properties-admin.component";
import { PropertyListComponent } from "../shared/property-list/property-list.component";
import { PropertyListEditorComponent } from "../shared/property-list/property-list-editor.component";

@NgModule({
    declarations: [
        PropertiesAdminEditorController,
        PropertiesAdminController,
        PropertyListComponent,
        PropertyListEditorComponent

    ],
    entryComponents: [
        PropertiesAdminEditorController,
        PropertiesAdminController,
        PropertyListComponent,
        PropertyListEditorComponent
    ],
    imports: [
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloServicesModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatGridListModule,
        CovalentDialogsModule,
        MatSnackBarModule,
        CovalentSearchModule,
        CovalentDataTableModule,
        FormsModule,
        TranslateModule.forChild(),
        MatFormFieldModule,
        MatCardModule,
        MatCheckboxModule,
        ReactiveFormsModule
    ],
    exports: [PropertiesAdminEditorController, PropertiesAdminController, 
                PropertyListComponent, PropertyListEditorComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()}
    ]
})
export class SharedModule {
}
