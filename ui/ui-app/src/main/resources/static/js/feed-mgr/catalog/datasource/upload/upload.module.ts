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
import {FileSizePipe} from "./file-size.pipe";
import {UploadComponent} from "./upload.component";
import {uploadStates} from "./upload.states";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatIconModule} from "@angular/material/icon";

@NgModule({
    declarations: [
        FileSizePipe,
        UploadComponent
    ],
    entryComponents: [
        UploadComponent
    ],
    exports: [
        UploadComponent
    ],
    imports: [
        CommonModule,
        CovalentFileModule,
        FlexLayoutModule,
        KyloCommonModule,
        MatButtonModule,
        MatCardModule,
        MatDividerModule,
        MatListModule,
        MatToolbarModule,
        MatIconModule,
        MatProgressBarModule
    ]
})
export class UploadModule {
}

@NgModule({
    imports: [
        UploadModule,
        UIRouterModule.forChild({states: uploadStates})
    ]
})
export class UploadRouterModule {
}
