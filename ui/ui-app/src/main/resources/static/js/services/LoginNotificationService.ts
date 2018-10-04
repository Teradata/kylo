//service to check kylo configuration properties and add any notifications
import CommonRestUrlService from "./CommonRestUrlService";
import {NotificationService} from "./notification.service";
import AccessControlService from "./AccessControlService";
import {TemplateMetadata} from "./model";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { TemplateService } from "./template.service";

@Injectable()
export default class LoginNotificationService {

    constructor(private http: HttpClient,
                private commonRestUrlService: CommonRestUrlService,
                // private accessControlService: AccessControlService,
                private notificationService: NotificationService,
                private templateService: TemplateService) {
    }

    initNotifications () {
        // this.accessControlService.getUserAllowedActions().then((actionSet: any) => {
        //     this.templateService.getTemplates().subscribe((templates: TemplateMetadata[]) => {
        //         if(templates.find((t) => !t.installed)) {
        //             let allowed = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
        //             if (allowed) {
        //                 this.http.get(this.commonRestUrlService.CONFIGURATION_PROPERTIES_URL).toPromise().then((response: any) => {
        //                     if (Boolean(response['kylo.install.template.notification'])) {
        //                         this.notificationService.success("Found uninstalled templates in repository.", 2000);
        //                         // this.NotificationService.addNotification("There are uninstalled templates in repository.", "");
        //                     }
        //                 })
        //             }
        //         }
        //     });
        // });
    }
}