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
import { FeedFieldPolicyRuleService } from "./feed-field-policy-rules/services/FeedFieldPolicyRuleService";
import { FieldPolicyRuleOptionsFactory } from "./feed-field-policy-rules/services/FieldPolicyRuleOptionsFactory";
import { FeedFieldPolicyRulesDialogComponent } from "./feed-field-policy-rules/feed-field-policy-rules-dialog.component";
import { FeedFieldPolicyRuleDialogComponent } from "./feed-field-policy-rules/FeedFieldPolicyRuleDialog";
import {FieldPoliciesModule} from "./field-policies-angular2/field-policies.module";
import { FieldPolicyOptionsService } from "./field-policies-angular2/field-policy-options.service";
import {PropertyListModule} from "./property-list/property-list.module";
import { MatRadioModule, MatProgressBarModule } from '@angular/material';

@NgModule({
    declarations: [
        PropertiesAdminEditorController,
        PropertiesAdminController,
        PropertyListEditorComponent,
        FeedFieldPolicyRuleDialogComponent,
        
    ],
    entryComponents: [
        PropertiesAdminEditorController,
        PropertiesAdminController,
        PropertyListEditorComponent,
        FeedFieldPolicyRuleDialogComponent
    ],
    imports: [
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatInputModule,
        MatSelectModule,
        MatRadioModule,
        MatProgressSpinnerModule,
        MatProgressBarModule,
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
        ReactiveFormsModule,
        PropertyListModule,
        FieldPoliciesModule,
        KyloFeedManagerModule
    ],
    exports: [PropertiesAdminEditorController, PropertiesAdminController, FeedFieldPolicyRuleDialogComponent,
                PropertyListComponent, PropertyListEditorComponent, FeedFieldPolicyRulesDialogComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [FieldPolicyRuleOptionsFactory, FeedFieldPolicyRuleService, FieldPolicyOptionsService,
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()}
    ]
})
export class SharedModule {
}
