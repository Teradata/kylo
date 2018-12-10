import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatGridListModule} from '@angular/material/grid-list';
import {MatDialogModule} from '@angular/material/dialog';

import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatStepperModule} from '@angular/material/stepper';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {BrowserModule} from "@angular/platform-browser";

import { CovalentCommonModule } from '@covalent/core/common';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import { CovalentChipsModule } from '@covalent/core/chips';

import {TranslateModule} from "@ngx-translate/core";
import {CodemirrorModule} from "ng2-codemirror";

import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";

import { UIRouterModule } from "@uirouter/angular";
import {templateStates} from "./templates.states";

import { MatSnackBarModule } from "@angular/material/snack-bar";

import { CovalentDataTableModule } from '@covalent/core/data-table';
import { CovalentSearchModule } from '@covalent/core/search';
import { CovalentPagingModule } from '@covalent/core/paging';
import { FormsModule, ReactiveFormsModule, FormControlDirective } from '@angular/forms';
import { RegisteredTemplatesController } from "./RegisteredTemplatesController.component";
import { RegisterTemplateController } from "./template-stepper/RegisterTemplateController.component";
import { RegisterNewTemplateController } from "./new-template/RegisterNewTemplateController.component";
import { ImportTemplateController } from "./import-template/ImportTemplateController.component";
import { RegisterSelectTemplateController } from "./template-stepper/select-template/select-template.component";

import { TemplateAccessControlController } from "./template-stepper/access-control/template-access-control.component";
import { RegisterProcessorPropertiesController } from "./template-stepper/processor-properties/processor-properties.component";
import { RegisterTemplateInprogressDialog } from "./template-stepper/register-template/register-template-inprogress.component";
import { RegisterTemplateErrorDialog } from "./template-stepper/register-template/register-template-error.component";
import { RegisterTemplateCompleteController, RegisterCompleteRegistrationController } from "./template-stepper/register-template/register-template-step.component";
import { CovalentVirtualScrollModule } from '@covalent/core/virtual-scroll';
import { DerivedExpression } from "./template-stepper/processor-properties/derived-expression";

import {KyloFeedManagerModule} from "../feed-mgr.module";
import * as angular from "angular";
import { TemplateDeleteDialog } from "./template-stepper/select-template/template-delete-dialog.component";

import { DndModule } from 'ng2-dnd';
import { TemplateOrderController } from "./template-order/template-order.component";
import { MatTooltipModule } from "@angular/material/tooltip";

// import '../../vendor/ment.io/styles.css';
// import '../../vendor/ment.io/templates.js';

@NgModule({
    declarations: [
        RegisteredTemplatesController,
        RegisterTemplateController,
        RegisterNewTemplateController,
        ImportTemplateController,
        RegisterSelectTemplateController,
        TemplateAccessControlController,
        RegisterProcessorPropertiesController,
        RegisterTemplateInprogressDialog,
        RegisterTemplateErrorDialog,
        RegisterTemplateCompleteController,
        RegisterCompleteRegistrationController,
        TemplateOrderController,
        DerivedExpression,
        TemplateDeleteDialog
    ],
    entryComponents: [
        RegisteredTemplatesController,
        RegisterTemplateController,
        RegisterNewTemplateController,
        ImportTemplateController,
        RegisterSelectTemplateController,
        TemplateAccessControlController,
        RegisterProcessorPropertiesController,
        RegisterTemplateInprogressDialog,
        RegisterTemplateErrorDialog,
        RegisterTemplateCompleteController,
        RegisterCompleteRegistrationController,
        TemplateDeleteDialog,
    ],
    imports: [
        CodemirrorModule,
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentDataTableModule,
        CovalentSearchModule,
        CovalentPagingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        CovalentChipsModule,
        KyloServicesModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatInputModule,
        MatSelectModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatGridListModule,
        MatDialogModule,
        MatStepperModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MatFormFieldModule,
        MatCardModule,
        MatCheckboxModule,
        MatDatepickerModule,
        CovalentVirtualScrollModule,
        MatSnackBarModule,
        CovalentDialogsModule,
        MatTooltipModule,
        DndModule.forRoot(),
        UIRouterModule.forChild({states: templateStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()}
    ]
})
export class TemplateModule {
    constructor() {
    }
}
