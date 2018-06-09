import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UpgradeModule} from "@angular/upgrade/static";

import {notificationServiceProvider} from "./angular2";

@NgModule({
    imports: [
        CommonModule,
        UpgradeModule
    ],
    providers: [
        notificationServiceProvider
    ]
})
export class KyloServicesModule {

}
