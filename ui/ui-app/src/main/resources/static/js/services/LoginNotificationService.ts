//service to check kylo configuration properties and add any notifications
import * as angular from 'angular';
import CommonRestUrlService from "./CommonRestUrlService";
import {NotificationService} from "./notification.service";
import {AccessControlService} from "./AccessControlService";
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
            let allowed = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
            if (allowed) {
                this.templateService.getTemplates().subscribe((template: TemplateMetadata) => {
                        // console.log(template);
                        this.$http.get(this.CommonRestUrlService.CONFIGURATION_PROPERTIES_URL).then((response: any) => {
                            const notification = this.NotificationService.addNotification("Template updates available in repository. Click to dismiss this message.", "update");
                            notification.callback = this.NotificationService.removeNotification.bind(this.NotificationService);
                        })

                    },
                    (error) => console.log(error));
            }
        });
    }
}
angular.module(moduleName).service("LoginNotificationService", LoginNotificationService);