import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {TranslateModule} from "@ngx-translate/core";
import { UIRouterModule } from "@uirouter/angular";
import { OpsManagerServicesModule } from "../services/ops-mgr.services.module";
import * as angular from "angular";


import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from '@angular/material/card';


import { CommonModule } from '@angular/common';  
import { MatListModule } from "@angular/material/list";
import {FlexLayoutModule} from "@angular/flex-layout";

import { MatProgressBarModule } from "@angular/material/progress-bar";
import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";
import { schedulerStates } from "./ops-mgr-schduler.states";
import { SchedulerComponent } from "./SchedulerComponent";
import { MatDividerModule } from "@angular/material/divider";


@NgModule({
    declarations: [
        SchedulerComponent
    ],
    entryComponents: [
        SchedulerComponent
    ],
    imports: [
        CommonModule,
        KyloServicesModule,
        KyloCommonModule,
        OpsManagerServicesModule,
        MatProgressBarModule,
        MatCardModule,
        MatButtonModule,
        MatListModule,
        MatDividerModule,
        FlexLayoutModule,
        UIRouterModule.forChild({states: schedulerStates}),
        TranslateModule.forChild()
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers : [{provide: "$injector", useFactory: () => angular.element(document.body).injector()}]
})
export class OpsManagerSchedulerModule {
    constructor() {
    }
}
