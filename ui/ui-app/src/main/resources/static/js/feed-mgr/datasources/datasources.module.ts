import {KyloServicesModule} from "../../services/services.module";
import { UIRouterModule } from "@uirouter/angular";
import {datasourcesStates} from "./datasources.states";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import { NgModule } from "@angular/core";
import {NO_ERRORS_SCHEMA} from '@angular/core';
import { CovalentDataTableModule } from '@covalent/core/data-table';
import { CovalentPagingModule } from '@covalent/core/paging';
import {MatToolbarModule} from '@angular/material/toolbar';
import { CovalentSearchModule } from '@covalent/core/search';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import { MatDialogModule} from '@angular/material/dialog'
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {FlexLayoutModule} from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule, FormControlDirective } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { DatasourcesTableComponent } from "./DatasourcesTableComponent";
import { DatasourcesService } from "../services/DatasourcesService";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatButtonModule} from '@angular/material/button';
import { DatasourcesDetailsComponent, SaveDatasourceDialogComponent } from "./DatasourcesDetailsComponent";
import { CommonModule } from "@angular/common";
import { KyloCommonModule } from "../../common/common.module";

@NgModule({ 
    declarations: [  
        DatasourcesTableComponent,
        DatasourcesDetailsComponent,
        SaveDatasourceDialogComponent
    ], 
    entryComponents : [SaveDatasourceDialogComponent],
    imports: [ 
        CommonModule,
        KyloCommonModule,
        KyloServicesModule,
        MatProgressBarModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        MatTableModule,
        MatIconModule,
        MatInputModule,
        MatDialogModule,
        MatCardModule, 
        CovalentDataTableModule,
        CovalentPagingModule,
        MatToolbarModule,
        CovalentSearchModule,
        CovalentDialogsModule,
        MatFormFieldModule,
        MatSelectModule,
        FlexLayoutModule,
        FormsModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        BrowserModule,
        UIRouterModule.forChild({states: datasourcesStates}) 
    ],
    providers : [DatasourcesService],
    schemas : [NO_ERRORS_SCHEMA],
}) 
export class DataSourcesModule { 
} 