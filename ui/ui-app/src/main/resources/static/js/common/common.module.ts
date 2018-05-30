import {NgModule} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {BrowserModule} from "@angular/platform-browser";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";

import {KyloServicesModule} from "../services/services.module";
import {AddButtonComponent} from "./add-button/add-button.component";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";

@NgModule({
    declarations: [
        AddButtonComponent,
        NotificationMenuComponent
    ],
    entryComponents: [
        AddButtonComponent,
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
