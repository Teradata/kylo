import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {CovalentFileModule} from "@covalent/core/file";
import {UIRouterModule} from "@uirouter/angular";

import {KyloCommonModule} from "../../../../common/common.module";
import {CatalogApiModule} from "../../api/catalog-api.module";
import {UploadComponent} from "./upload.component";
import {uploadStates} from "./upload.states";

@NgModule({
    declarations: [
        UploadComponent
    ],
    entryComponents: [
        UploadComponent
    ],
    exports:[
        UploadComponent
    ],
    imports: [
        CatalogApiModule,
        CommonModule,
        CovalentFileModule,
        FlexLayoutModule,
        KyloCommonModule,
        MatButtonModule,
        MatCardModule,
        MatDividerModule,
        MatListModule,
        MatProgressBarModule,
        UIRouterModule.forChild({states: uploadStates})
    ]
})
export class UploadModule {
}
