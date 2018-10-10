import {NgModule} from "@angular/core";

import {CatalogService} from "./services/catalog.service";
import {FileManagerService} from "./services/file-manager.service";
import {SelectionService} from './services/selection.service';

@NgModule({
    providers: [
        CatalogService,
        SelectionService,
        FileManagerService
    ]
})
export class CatalogApiModule {
}
