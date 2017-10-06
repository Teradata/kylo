import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {UpgradeModule} from "@angular/upgrade/static";
import {UIRouterModule} from "@uirouter/angular";
import {UIRouterUpgradeModule} from "@uirouter/angular-hybrid";

import "routes";

@NgModule({
    imports: [
        BrowserModule,
        UIRouterModule,
        UIRouterUpgradeModule,
        UpgradeModule
    ],
    providers: [
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {
    ngDoBootstrap() {
    }
}
