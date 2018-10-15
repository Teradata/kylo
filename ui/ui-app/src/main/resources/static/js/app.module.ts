import {HttpClient, HttpClientModule} from "@angular/common/http";
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
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatDialogModule} from "@angular/material/dialog";

import "routes"; // load AngularJS application
import {KyloCommonModule} from "./common/common.module";
import {KyloFeedManagerModule} from "./feed-mgr/feed-mgr.module";
import {KyloServicesModule} from "./services/services.module";
import { SideNavModule } from "./side-nav/side-nav.module";
import {HomeComponent} from "./main/HomeComponent";
import {IndexComponent, LoadingDialogComponent} from "./main/index-ng2.component";

import { HomeComponent } from "./main/HomeComponent";
import { AccessDeniedComponent } from "./main/AccessDeniedComponent";

import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

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
    declarations: [
        HomeComponent,
        IndexComponent,
        AccessDeniedComponent,
        LoadingDialogComponent
    ],
    entryComponents: [
        HomeComponent,
        IndexComponent,
        AccessDeniedComponent,
        LoadingDialogComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        CovalentLoadingModule,
        FlexLayoutModule,
        FormsModule,
        HttpClientModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        KyloServicesModule,
        MatDialogModule,
        MatFormFieldModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatToolbarModule,
        SideNavModule,
        TranslateModule.forRoot(translateConfig),
        UIRouterModule,
        UIRouterUpgradeModule,
        UpgradeModule,
        MatProgressBarModule,
    ],
    providers: [
        {provide: "$ocLazyLoad", useFactory: (i: any) => i.get("$ocLazyLoad"), deps: ["$injector"]} as FactoryProvider,
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {

    constructor(translate: TranslateService) {
        translate.setDefaultLang("en");
    }

    ngDoBootstrap() {
        // needed by UpgradeModule
    }
}
