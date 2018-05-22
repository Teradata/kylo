import {HttpClientModule} from "@angular/common/http";
import {NgModule} from "@angular/core";

import {FileSizePipe} from "./pipes/file-size.pipe";
import {CatalogService} from "./services/catalog.service";
import {FileManagerService} from "./services/file-manager.service";
import {SelectionService} from './services/selection.service';

@NgModule({
    declarations: [
        FileSizePipe
    ],
    exports: [
        FileSizePipe
    ],
    imports: [
        HttpClientModule
    ],
    providers: [
        CatalogService,
        SelectionService,
        FileManagerService,
    ]
})
export class CatalogApiModule {
}
