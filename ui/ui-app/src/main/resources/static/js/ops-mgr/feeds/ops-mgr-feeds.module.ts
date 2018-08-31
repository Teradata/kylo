import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import { UIRouterModule } from "@uirouter/angular";
import { feedStates } from "./ops-mgr-feeds.states";
import { FeedDetailsComponent } from "./FeedDetailsComponent";
import { OpsManagerServicesModule } from "../services/ops-mgr.services.module";
import * as angular from "angular";
import { JobsModule } from "../jobs/jobs.module";


import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from '@angular/material/card';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

import {TranslateModule} from "@ngx-translate/core";
import { FeedActivityComponent } from "./FeedActivityComponent";

import { CommonModule } from '@angular/common';  
import { MatIconModule } from "@angular/material/icon";
import { MatListModule } from "@angular/material/list";
import {FlexLayoutModule} from "@angular/flex-layout";

import { NvD3Module } from 'ng2-nvd3';


@NgModule({
    declarations: [
        FeedDetailsComponent,
        FeedActivityComponent
    ],
    entryComponents: [
        FeedDetailsComponent,
        FeedActivityComponent
    ],
    imports: [
        CommonModule,
        JobsModule,
        OpsManagerServicesModule,
        TranslateModule,
        NvD3Module,
        MatProgressSpinnerModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        FlexLayoutModule,
        UIRouterModule.forChild({states: feedStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers : [{provide: "$injector", useFactory: () => angular.element(document.body).injector()}]
})
export class OpsManagerFeedsModule {
    constructor() {
    }
}