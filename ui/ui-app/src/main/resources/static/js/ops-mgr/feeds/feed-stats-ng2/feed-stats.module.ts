import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import { UIRouterModule } from "@uirouter/angular";
import * as angular from "angular";


import { CommonModule } from '@angular/common';
import {TranslateModule} from "@ngx-translate/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import { FormsModule } from '@angular/forms';

import {KyloServicesModule} from "../../../services/services.module";
import {KyloCommonModule} from "../../../common/common.module";

import { NvD3Module } from 'ng2-nvd3';
import {FeedStatsServiceNg2} from "./feed-stats.service";
import { feedStatsStates } from "./feed-stats.states";

import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {FeedStatsChartsComponent} from "./feed-stats-charts.component";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import {MatSliderModule} from '@angular/material/slider';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';
import {ProvenanceEventStatsServiceNg2} from "./provenance-event-stats.service";
import {FeedStatsComponent} from "./feed-stats.component";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {MatMenuModule} from "@angular/material/menu";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {MatTooltipModule} from "@angular/material/tooltip";
import {CovalentPagingModule} from "@covalent/core/paging";
import {CovalentSearchModule} from "@covalent/core/search";


@NgModule({
    declarations: [
        FeedStatsChartsComponent
    ],
    entryComponents: [
       // FeedStasChartsComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        KyloServicesModule,
        KyloCommonModule,
        FlexLayoutModule,
        TranslateModule,
        NvD3Module,
        MatSlideToggleModule,
        MatProgressSpinnerModule,
        MatProgressBarModule,
        MatCardModule,
        MatButtonModule,
        MatSliderModule,
        MatToolbarModule,
        MatTooltipModule,
        MatInputModule,
        MatFormFieldModule,
        MatIconModule,
        MatSelectModule,
        MatDividerModule,
        MatListModule,
        MatMenuModule,
        CovalentDataTableModule,
        CovalentPagingModule,
        CovalentDialogsModule,
        CovalentSearchModule
    ],
    exports:[
        FeedStatsChartsComponent
    ],
    providers : [
        FeedStatsServiceNg2,
        ProvenanceEventStatsServiceNg2]
})
export class FeedStatsModule {
    constructor() {
    }
}


@NgModule({
imports: [
    FeedStatsModule,
    UIRouterModule.forChild({states: feedStatsStates})
],
    declarations:[
        FeedStatsComponent
    ]
})
export class FeedStatsRouterModule {
    constructor() {
    }
}
