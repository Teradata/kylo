import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {UpgradeModule} from "@angular/upgrade/static";

import {notificationServiceProvider} from "./angular2";

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule
    ],
    providers: [
        notificationServiceProvider
    ]
})
export class KyloServicesModule {

}
