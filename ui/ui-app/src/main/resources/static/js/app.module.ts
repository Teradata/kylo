import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {FactoryProvider, NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {UpgradeModule} from "@angular/upgrade/static";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {TranslateLoader, TranslateModule, TranslateModuleConfig, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {UIRouterModule} from "@uirouter/angular";
import {UIRouterUpgradeModule} from "@uirouter/angular-hybrid";

import "routes"; // load AngularJS application
import {KyloCommonModule} from "./common/common.module";
import {AngularHttpInterceptor} from "./services/AngularHttpInterceptor";
import {KyloServicesModule} from "./services/services.module";
import {MatIconModule} from "@angular/material/icon";


export function translateHttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, "locales/", ".json");
}

const translateConfig: TranslateModuleConfig = {
    loader: {
        provide: TranslateLoader,
        useFactory: translateHttpLoaderFactory,
        deps: [HttpClient]
    }
};

@NgModule({
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        CovalentLoadingModule,
        FlexLayoutModule,
        HttpClientModule,
        KyloCommonModule,
        KyloServicesModule,
        MatIconModule,
        TranslateModule.forRoot(translateConfig),
        UIRouterModule,
        UIRouterUpgradeModule,
        UpgradeModule
    ],
    providers: [
        {provide: "$ocLazyLoad", useFactory: (i: any) => i.get("$ocLazyLoad"), deps: ["$injector"]} as FactoryProvider,
        {provide: HTTP_INTERCEPTORS, useClass: AngularHttpInterceptor, multi: true},
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {

    constructor(translate: TranslateService) {
        translate.setDefaultLang("en");
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        translate.use('en');
    }

    ngDoBootstrap() {
        // needed by UpgradeModule
    }
}
