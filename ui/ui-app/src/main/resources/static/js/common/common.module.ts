import {NgModule} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {BrowserModule} from "@angular/platform-browser";
import {CovalentLoadingModule, CovalentMenuModule, CovalentNotificationsModule} from "@covalent/core";

import {KyloServicesModule} from "../services/services.module";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";

@NgModule({
    declarations: [
        NotificationMenuComponent
    ],
    entryComponents: [
        NotificationMenuComponent
    ],
    imports: [
        BrowserModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloServicesModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatProgressSpinnerModule
    ]
})
export class KyloCommonModule {

}
