import { NgModule, NO_ERRORS_SCHEMA } from "@angular/core";
import { businessMetadataStates } from "./business-metadata.states";
import { UIRouterModule } from "@uirouter/angular";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { BusinessMetadataComponent } from "./BusinessMetadataComponent";
import { MatCardModule } from "@angular/material/card";
import { KyloFeedManagerModule } from "../feed-mgr.module";
import { KyloCommonModule } from "../../common/common.module";
import { KyloServicesModule } from "../../services/services.module";
import { CommonModule } from "@angular/common";
import { FlexLayoutModule } from "@angular/flex-layout";
import { SharedModule } from "../shared/shared.modules";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    declarations:[
        BusinessMetadataComponent
    ],
    entryComponents:[
        BusinessMetadataComponent
    ],
    imports:[
        CommonModule,
        SharedModule,
        FlexLayoutModule,
        MatProgressBarModule,
        MatCardModule,
        KyloFeedManagerModule,
        KyloCommonModule,
        KyloServicesModule,
        TranslateModule,
        UIRouterModule.forChild({states: businessMetadataStates})],
    exports:[],
    providers:[],
    schemas: [NO_ERRORS_SCHEMA],
})
export class BusinessMetadataModule{

}