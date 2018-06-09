import {HttpClientModule} from "@angular/common/http";
import {FactoryProvider, NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {UpgradeModule} from "@angular/upgrade/static";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {UIRouterModule} from "@uirouter/angular";
import {UIRouterUpgradeModule} from "@uirouter/angular-hybrid";

import "routes"; // load AngularJS application
import {KyloCommonModule} from "./common/common.module";
import {KyloServicesModule} from "./services/services.module";

@NgModule({
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        CovalentLoadingModule,
        FlexLayoutModule,
        HttpClientModule,
        KyloCommonModule,
        KyloServicesModule,
        UIRouterModule,
        UIRouterUpgradeModule,
        UpgradeModule
    ],
    providers: [
        {provide: "$ocLazyLoad", useFactory: (i: any) => i.get("$ocLazyLoad"), deps: ["$injector"]} as FactoryProvider,
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {
    ngDoBootstrap() {
        // needed by UpgradeModule
    }
}
