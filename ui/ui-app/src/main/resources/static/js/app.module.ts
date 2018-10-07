import {DOCUMENT} from "@angular/common";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {FactoryProvider, NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatIconRegistry} from "@angular/material/icon";
import {BrowserModule, DomSanitizer} from "@angular/platform-browser";
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


/**
 * Overrides {@link MatIconRegistry} to include the viewBox when building SVG elements.
 */
export function iconRegistryFactory(http: HttpClient, sanitizer: DomSanitizer, document: any) {
    const registry: any = new MatIconRegistry(http, sanitizer, document);
    const _toSvgElement = registry._toSvgElement.bind(registry);
    registry._toSvgElement = function (element: Element) {
        const svgElement = _toSvgElement(element);
        if (element.hasAttribute("viewBox")) {
            svgElement.setAttribute("viewBox", element.getAttribute("viewBox"));
        }
        return svgElement;
    };
    return registry;
}

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
        KyloServicesModule.forRoot(),
        TranslateModule.forRoot(translateConfig),
        UIRouterModule,
        UIRouterUpgradeModule,
        UpgradeModule
    ],
    providers: [
        {provide: "$ocLazyLoad", useFactory: (i: any) => i.get("$ocLazyLoad"), deps: ["$injector"]} as FactoryProvider,
        {provide: HTTP_INTERCEPTORS, useClass: AngularHttpInterceptor, multi: true},
        {provide: MatIconRegistry, useFactory: iconRegistryFactory, deps: [HttpClient, DomSanitizer, DOCUMENT]},
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {

    constructor(private domSanitizer: DomSanitizer, private iconRegistry: MatIconRegistry, private translate: TranslateService) {
        this.initIcons();
        this.initTranslation();
    }

    ngDoBootstrap() {
        // needed by UpgradeModule
    }

    private initIcons(): void {
        // "Font Awesome Free" icons by @fontawesome (https://fontawesome.com) are licensed under CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/)
        const fabUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("node_modules/@fortawesome/fontawesome-free/sprites/brands.svg");
        this.iconRegistry.addSvgIconSetInNamespace("fab", fabUrl);

        const farUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("node_modules/@fortawesome/fontawesome-free/sprites/regular.svg");
        this.iconRegistry.addSvgIconSetInNamespace("far", farUrl);

        const fasUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("node_modules/@fortawesome/fontawesome-free/sprites/solid.svg");
        this.iconRegistry.addSvgIconSetInNamespace("fas", fasUrl);

        this.iconRegistry.registerFontClassAlias("mdi","mdi-set")

        const mdiSvgUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("node_modules/@mdi/fonts/materialdesignicons-webfont.svg");
        this.iconRegistry.addSvgIconSetInNamespace("mdi", mdiSvgUrl);
    }

    private initTranslation(): void {
        this.translate.setDefaultLang("en");
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        this.translate.use('en');
    }
}
