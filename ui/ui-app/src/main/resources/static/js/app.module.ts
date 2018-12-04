import {DOCUMENT} from "@angular/common";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {ClassProvider, FactoryProvider, Injector, NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatIconRegistry} from "@angular/material/icon";
import {BrowserModule, DomSanitizer} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {UpgradeModule} from "@angular/upgrade/static";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {TranslateLoader, TranslateModule, TranslateModuleConfig, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {UIRouter, UIRouterModule, UrlService} from "@uirouter/angular";
import {UIRouterUpgradeModule} from "@uirouter/angular-hybrid";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatDialogModule} from "@angular/material/dialog";

import "routes"; // load AngularJS application
import {KyloCommonModule} from "./common/common.module";
import {KyloFeedManagerModule} from "./feed-mgr/feed-mgr.module";
import {AngularHttpInterceptor} from "./services/AngularHttpInterceptor";
import {KyloServicesModule} from "./services/services.module";
import { SideNavModule } from "./side-nav/side-nav.module";
import {HomeComponent} from "./main/HomeComponent";
import {IndexComponent, LoadingDialogComponent} from "./main/index-ng2.component";
import { AccessDeniedComponent } from "./main/AccessDeniedComponent";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

import '../node_modules/@fortawesome/fontawesome-free/sprites/brands.svg';
import '../node_modules/@fortawesome/fontawesome-free/sprites/regular.svg';
import '../node_modules/@fortawesome/fontawesome-free/sprites/solid.svg';
import '../node_modules/@mdi/font/fonts/materialdesignicons-webfont.svg';

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
        KyloServicesModule.forRoot(),
        MatDialogModule,
        MatFormFieldModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatToolbarModule,
        SideNavModule,
        TranslateModule.forRoot(translateConfig),
        UIRouterModule.forChild({
            states: [
                {
                    name: 'catalog.**',
                    url: '/catalog',
                    loadChildren: './feed-mgr/catalog/catalog.module#CatalogRouterModule'
                },
                {
                    name: 'feed-definition.**',
                    url: '/feed-definition',
                    loadChildren: './feed-mgr/feeds/define-feed-ng2/define-feed.module#DefineFeedModule'
                },
                {
                    name: 'repository.**',
                    url: '/repository',
                    loadChildren: './repository/repository.module#RepositoryModule'
                },
                {
                    name: 'template-info.**',
                    url: '/template-info',
                    loadChildren: './repository/repository.module#RepositoryModule'
                },
                {
                    name: 'import-template.**',
                    url: '/importTemplate',
                    loadChildren: './repository/repository.module#RepositoryModule'
                },
                {
                    name: 'visual-query.**',
                    url: '/visual-query/{engine}',
                    params: {
                        engine: null
                    },
                    loadChildren: "./feed-mgr/visual-query/visual-query.module#VisualQueryRouterModule"
                },
                {
                    name: 'sla.**',
                    url: '/sla',
                    loadChildren: './feed-mgr/sla/sla.module#SlaRouterModule'
                },
                {
                    name: 'sla-email-template.**',
                    url: '/sla-email-template',
                    loadChildren: './feed-mgr/sla/email-template/sla-email-template.module#SlaEmailTemplatesRouterModule'
                },
                {
                    name: 'jcr-query.**',
                    url: '/admin/jcr-query',
                    loadChildren: './admin/admin.module#AdminModule'
                },
                {
                    name: 'cluster.**',
                    url: '/admin/cluster',
                    loadChildren: './admin/admin.module#AdminModule'
                }
            ]
        }),
        UIRouterUpgradeModule,
        UpgradeModule
    ],
    providers: [
        {provide: "$ocLazyLoad", useFactory: (i: any) => i.get("$ocLazyLoad"), deps: ["$injector"]} as FactoryProvider,
        {provide: HTTP_INTERCEPTORS, useClass: AngularHttpInterceptor, multi: true} as ClassProvider,
        {provide: MatIconRegistry, useFactory: iconRegistryFactory, deps: [HttpClient, DomSanitizer, DOCUMENT]},
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {

    constructor(private domSanitizer: DomSanitizer, private iconRegistry: MatIconRegistry, private injector: Injector, private translate: TranslateService) {
        this.initIcons();
        this.initTranslation();
    }

    ngDoBootstrap() {
        const upgrade = this.injector.get(UpgradeModule) as UpgradeModule;

        // The DOM must be already be available
        upgrade.bootstrap(document.body, ["kylo"]);

        // Initialize the Angular Module (get() any UIRouter service from DI to initialize it)
        const url: UrlService = this.injector.get(UIRouter).urlService;

        // Instruct UIRouter to listen to URL changes
        url.listen();
        url.sync();
    }

    private initIcons(): void {
        // "Font Awesome Free" icons by @fontawesome (https://fontawesome.com) are licensed under CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/)
        const fabUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("../node_modules/@fortawesome/fontawesome-free/sprites/brands.svg");
        this.iconRegistry.addSvgIconSetInNamespace("fab", fabUrl);

        const farUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("../node_modules/@fortawesome/fontawesome-free/sprites/regular.svg");
        this.iconRegistry.addSvgIconSetInNamespace("far", farUrl);

        const fasUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("../node_modules/@fortawesome/fontawesome-free/sprites/solid.svg");
        this.iconRegistry.addSvgIconSetInNamespace("fas", fasUrl);

        this.iconRegistry.registerFontClassAlias("mdi", "mdi-set");

        const mdiSvgUrl = this.domSanitizer.bypassSecurityTrustResourceUrl("../node_modules/@mdi/font/fonts/materialdesignicons-webfont.svg");
        this.iconRegistry.addSvgIconSetInNamespace("mdi", mdiSvgUrl);
    }

    private initTranslation(): void {
        this.translate.setDefaultLang("en");
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        this.translate.use('en');
    }
}
