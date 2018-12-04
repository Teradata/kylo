import { CommonModule } from "@angular/common";
import { KyloCommonModule } from "../common/common.module";
import {KyloServicesModule} from "../services/services.module";
import { UIRouterModule } from "@uirouter/angular";
import {authStates} from "./auth.states";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {UsersTableComponent} from "./users/UsersTableComponent";
import { NgModule } from "@angular/core";
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {UserService} from "./services/UserService";
import {MatSelectModule} from '@angular/material/select';
import {MatToolbarModule} from '@angular/material/toolbar';
import {UserDetailsComponent} from "./users/user-details/UserDetailsComponent";
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatChipsModule} from '@angular/material/chips';
import {MatInputModule} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {FlexLayoutModule} from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {GroupDetailsComponent} from "./groups/group-details/GroupDetailsComponent";
import {GroupsTableComponent} from "./groups/GroupsTableComponent";
import {MatCheckboxModule} from '@angular/material/checkbox';
import {PermissionsTableComponent} from './shared/permissions-table/permissions-table.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import * as angular from "angular";
import { TranslateModule } from "@ngx-translate/core";

@NgModule({ 
    declarations: [ 
        UsersTableComponent,
        UserDetailsComponent,
        GroupDetailsComponent,
        GroupsTableComponent,
        PermissionsTableComponent
    ], 
    imports: [ 
        CommonModule, 
        KyloCommonModule, 
        KyloServicesModule,
        MatProgressBarModule,
        MatDividerModule,
        MatListModule,
        MatIconModule,
        MatCardModule,
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
        MatCheckboxModule,
        TranslateModule,
        UIRouterModule.forChild({states: authStates})
    ],
    providers : [
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()},
        UserService
    ],
    schemas : [NO_ERRORS_SCHEMA]
}) 
export class AuthModule { 
} 
