//service to check kylo configuration properties and add any notifications
import * as angular from 'angular';
import CommonRestUrlService from "./CommonRestUrlService";
import {NotificationService} from "./notification.service";
import AccessControlService from "./AccessControlService";
import {moduleName} from "./module-name";
import {TemplateService} from "../repository/services/template.service";
import {TemplateMetadata} from "../repository/services/model";

export default class LoginNotificationService {
    static readonly $inject = ["$http", "$q", "CommonRestUrlService", "AccessControlService", "NotificationService", "templateService"];

    constructor(private $http: angular.IHttpService,
                private $q: angular.IQService,
                private CommonRestUrlService: CommonRestUrlService,
                private accessControlService: AccessControlService,
                private NotificationService: NotificationService,
                private templateService: TemplateService) {
    }

    initNotifications = () => {
        this.accessControlService.getUserAllowedActions().then((actionSet: any) => {
            this.templateService.getTemplates().subscribe((templates: TemplateMetadata[]) => {
                if(templates.find((t) => !t.installed)) {
                    let allowed = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
                    if (allowed) {
                        this.$http.get(this.CommonRestUrlService.CONFIGURATION_PROPERTIES_URL).then((response: any) => {
                            if (Boolean(response.data['kylo.install.template.notification'])) {
                                this.NotificationService.success("Found uninstalled templates in repository.", 0);
                                // this.NotificationService.addNotification("There are uninstalled templates in repository.", "");
                            }
                        })
                    }
                }
            });
        });
    }
}
angular.module(moduleName).service("LoginNotificationService", LoginNotificationService);