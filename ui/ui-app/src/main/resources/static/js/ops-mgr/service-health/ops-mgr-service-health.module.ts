import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
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


import { serviceHealthStates } from "./ops-mgr-service-health.states";
import { ServiceComponentHealthDetailsComponent } from "./ServiceComponentHealthDetailsComponent";
import { ServiceHealthComponent } from "./ServiceHealthComponent";
import { ServiceHealthDetailsComponent } from "./ServiceHealthDetailsComponent";
import { MatIconModule } from "@angular/material/icon";
import { TranslateModule } from "@ngx-translate/core";


@NgModule({
    declarations: [
        ServiceComponentHealthDetailsComponent,
        ServiceHealthComponent,
        ServiceHealthDetailsComponent
    ],
    entryComponents: [
        ServiceComponentHealthDetailsComponent,
        ServiceHealthComponent,
        ServiceHealthDetailsComponent
    ],
    imports: [
        CommonModule,
        KyloServicesModule,
        KyloCommonModule,
        OpsManagerServicesModule,
        MatProgressBarModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        FlexLayoutModule,
        TranslateModule.forChild(),
        UIRouterModule.forChild({states: serviceHealthStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers : [{provide: "$injector", useFactory: () => angular.element(document.body).injector()}]
})
export class OpsManagerServiceHealthModule {
    constructor() {
    }
}