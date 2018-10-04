import { CommonModule } from "@angular/common";
import { KyloCommonModule } from "../common/common.module";
import {KyloServicesModule} from "../services/services.module";
import { UIRouterModule } from "@uirouter/angular";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import { NgModule } from "@angular/core";
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MatSelectModule} from '@angular/material/select';
import {MatToolbarModule} from '@angular/material/toolbar';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatChipsModule} from '@angular/material/chips';
import {MatInputModule} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {FlexLayoutModule} from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import * as angular from "angular";
import { TemplateModule } from "../feed-mgr/templates/templates.module";
import { JcrQueryComponent } from "./jcr/JcrQueryComponent";
import { ClusterComponent } from "./cluster/ClusterComponent";
import { adminStates } from "./admin.states";
import { MatButtonModule } from "@angular/material/button";

@NgModule({ 
    declarations: [ 
        JcrQueryComponent,
        ClusterComponent
    ], 
    imports: [ 
        CommonModule, 
        KyloCommonModule, 
        KyloServicesModule,
        TemplateModule,
        MatProgressBarModule,
        MatDividerModule,
        MatListModule,
        MatIconModule,
        MatSelectModule,
        MatToolbarModule,
        CovalentDialogsModule,
        MatFormFieldModule,
        MatChipsModule,
        MatInputModule,
        MatAutocompleteModule,
        MatSnackBarModule,
        FlexLayoutModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        UIRouterModule.forChild({states: adminStates})
    ],
    providers : [
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()},
    ],
    schemas : [NO_ERRORS_SCHEMA]
}) 
export class AdminModule { 
} 
