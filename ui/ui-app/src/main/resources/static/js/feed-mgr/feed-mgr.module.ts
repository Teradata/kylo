import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {moduleName} from "./module-name"

import {categoriesServiceProvider, domainTypesServiceProvider, entityAccessControlServiceProvider, feedServiceProvider} from "./services/angular2";


@NgModule({
    imports: [
        CommonModule
    ],
    providers: [
        categoriesServiceProvider,
        entityAccessControlServiceProvider,
        feedServiceProvider,
        domainTypesServiceProvider
    ]
})
export class KyloFeedManagerModule {
    constructor(injector: Injector) {
        console.log("Loading KyloFeedManagerModule")
       require("./module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");

    }

}
