import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";

import {KyloServicesModule} from "../services/services.module";
import {AddButtonComponent} from "./add-button/add-button.component";
import {KyloIconComponent} from "./kylo-icon/kylo-icon.component";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    declarations: [
        AddButtonComponent,
        KyloIconComponent,
        NotificationMenuComponent
    ],
    entryComponents: [
        AddButtonComponent,
        NotificationMenuComponent
    ],
    providers:[
    ],
    imports: [
        CommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloServicesModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatProgressSpinnerModule
    ],
    exports: [
        KyloIconComponent
    ]
})
export class KyloCommonModule {
}
