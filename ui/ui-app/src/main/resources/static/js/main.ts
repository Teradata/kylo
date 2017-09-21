import {IAngularStatic} from "angular";
import {Injector} from "@angular/core";
import {platformBrowserDynamic} from "@angular/platform-browser-dynamic";
import {UpgradeModule} from "@angular/upgrade/static";
import {getUIRouter} from "@uirouter/angular-hybrid";
import {servicesPlugin, UrlService} from "@uirouter/core";

import {KyloModule} from "./app.module";

declare const angular: IAngularStatic;

// Fix @uirouter/core unable to load
servicesPlugin(null);

// Manually bootstrap the Angular app
platformBrowserDynamic().bootstrapModule(KyloModule).then(platformRef => {
    const injector: Injector = platformRef.injector;
    const upgrade = injector.get(UpgradeModule) as UpgradeModule;

    // The DOM must be already be available
    upgrade.bootstrap(document.body, ["kylo"]);

    // Initialize the Angular Module (get() any UIRouter service from DI to initialize it)
    const url: UrlService = getUIRouter(injector).urlService;

    // Instruct UIRouter to listen to URL changes
    url.listen();
    url.sync();
});
