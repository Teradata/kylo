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
import {FileUploadComponent} from "./file-upload/file-upload.component";
import {CovalentFileModule} from "@covalent/core/file";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";

@NgModule({
    declarations: [
        AddButtonComponent,
        KyloIconComponent,
        NotificationMenuComponent,
        FileUploadComponent
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
        CovalentFileModule,
        KyloServicesModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatProgressSpinnerModule
    ],
    exports: [
        KyloIconComponent,
        FileUploadComponent
    ]
})
export class KyloCommonModule {
}
