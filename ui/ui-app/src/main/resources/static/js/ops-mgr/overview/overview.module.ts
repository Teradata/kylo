import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatDialogModule} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {BrowserModule} from "@angular/platform-browser";

import { CovalentCommonModule } from '@covalent/core/common';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";

import {TranslateModule} from "@ngx-translate/core";

import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";

import { UIRouterModule } from "@uirouter/angular";
import {overviewStates} from "./overview.states";

import { MatSnackBarModule } from "@angular/material/snack-bar";

import { CovalentDataTableModule } from '@covalent/core/data-table';
import { CovalentSearchModule } from '@covalent/core/search';
import { CovalentPagingModule } from '@covalent/core/paging';
import { FormsModule, ReactiveFormsModule, FormControlDirective } from '@angular/forms';
import * as angular from "angular";
import { OpsManagerServicesModule } from "../services/ops-mgr.services.module";
import AlertsComponent from "./alerts/AlertsComponent";
import JobStatusIndicator from "./job-status-indicator/JobStatusIndicatorComponent";
import FeedStatusIndicator from "./feed-status-indicator/FeedStatusIndicatorComponent";
import OverviewComponent from "./OverviewComponent";
import { DataDetailsDialogComponent, DataConfidenceIndicatorComponent } from "./data-confidence-indicator/DataConfidenceIndicatorComponent";

import { NvD3Module } from 'ng2-nvd3';
import { ServiceIndicatorComponent, ServiceDetailsDialogComponent } from "./services-indicator/ServiceIndicatorComponent";
import { FeedHealthTableCardComponent } from "./feed-health/FeedHealthTableCardComponent";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

@NgModule({
    declarations: [
        AlertsComponent,
        JobStatusIndicator,
        FeedStatusIndicator,
        OverviewComponent,
        DataDetailsDialogComponent,
        ServiceIndicatorComponent,
        ServiceDetailsDialogComponent,
        DataConfidenceIndicatorComponent,
        FeedHealthTableCardComponent
    ],
    entryComponents: [
        AlertsComponent,
        JobStatusIndicator,
        FeedStatusIndicator,
        OverviewComponent,
        DataDetailsDialogComponent,
        ServiceIndicatorComponent,
        ServiceDetailsDialogComponent,
        DataConfidenceIndicatorComponent,
        FeedHealthTableCardComponent
    ],
    imports: [
        NvD3Module,
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentDataTableModule,
        CovalentSearchModule,
        CovalentPagingModule,
        CovalentMenuModule,
        OpsManagerServicesModule,
        KyloServicesModule,
        KyloCommonModule,
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
        FormsModule,
        ReactiveFormsModule,
        TranslateModule.forChild(),
        MatFormFieldModule,
        MatCardModule,
        MatCheckboxModule,
        MatSnackBarModule,
        CovalentDialogsModule,
        MatTabsModule,
        UIRouterModule.forChild({states: overviewStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [ 
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()}
    ]
})
export class OverviewModule {
    constructor() {
    }
}