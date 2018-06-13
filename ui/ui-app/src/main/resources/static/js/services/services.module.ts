import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {UpgradeModule} from "@angular/upgrade/static";

import {addButtonServiceProvider, broadcastServiceProvider, notificationServiceProvider, 
        paginationServiceProvider, tableOptionsServiceProvider, accessControlServiceProvider,
        stateServiceProvider} from "./angular2";

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule
    ],
    providers: [
        addButtonServiceProvider,
        broadcastServiceProvider,
        notificationServiceProvider,
        paginationServiceProvider,
        tableOptionsServiceProvider,
        accessControlServiceProvider,
        stateServiceProvider
    ]
})
export class KyloServicesModule {

}
