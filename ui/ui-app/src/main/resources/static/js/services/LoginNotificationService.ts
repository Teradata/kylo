//service to check kylo configuration properties and add any notifications
import {CommonRestUrlService} from "./CommonRestUrlService";
import {NotificationService} from "./notification.service";
import {AccessControlService} from "./AccessControlService";
import {TemplateMetadata} from "./model";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { TemplateService } from "./template.service";

@Injectable()
export class LoginNotificationService {

    constructor(private http: HttpClient,
                private commonRestUrlService: CommonRestUrlService,
                private accessControlService: AccessControlService,
                private notificationService: NotificationService,
                private templateService: TemplateService) {
    }

    initNotifications () {
        this.accessControlService.getUserAllowedActions().then((actionSet: any) => {
            let allowed = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
            if (allowed) {
                this.templateService.getTemplates().subscribe((template: TemplateMetadata) => {
                        // console.log(template);
                        this.http.get(this.commonRestUrlService.CONFIGURATION_PROPERTIES_URL).toPromise().then((response: any) => {
                            const notification = this.notificationService.addNotification("Template updates available in repository. Click to dismiss this message.", "update");
                            notification.callback = this.notificationService.removeNotification.bind(this.notificationService);
                        })

                    },
                    (error) => console.log(error));
            }
        });
    }
}
