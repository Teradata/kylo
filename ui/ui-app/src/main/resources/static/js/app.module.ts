import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {UpgradeModule} from "@angular/upgrade/static";

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule
    ]
})
export class KyloModule {
    constructor(private upgrade: UpgradeModule) {}
    ngDoBootstrap() {
        let upgrade = this.upgrade;
        System.amdRequire(["routes"], function () {
            upgrade.bootstrap(document.documentElement, ["kylo"]);
        });
    }
}
