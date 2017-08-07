import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {UpgradeModule} from "@angular/upgrade/static";
import "routes";

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule
    ]
})
export class KyloModule {
    constructor(private upgrade: UpgradeModule) {}
    ngDoBootstrap() {
        this.upgrade.bootstrap(document.documentElement, ["kylo"]);
    }
}
