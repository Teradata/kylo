import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "../module-name";
import { RegisterTemplateServiceFactory } from '../../services/RegisterTemplateServiceFactory';


export class TemplateOrderController {

    model: any = [];
    addAsNew: any;
    templateId: any;
    templateName: any;
    currentTemplate: any;
    message: any;


    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        //order list
        this.registerTemplateService.getRegisteredTemplates().then(function (response: any) {
            //order by .order
            var templates = _.sortBy(response.data, 'order');
            if (this.addAsNew && (this.templateId == null || this.templateId == undefined)) {
                //append to bottom
                templates.push({ id: 'NEW', templateName: this.templateName, currentTemplate: true });
            }
            else {
                var currentTemplate = _.filter(templates, function (template: any) {
                    return template.id == this.templateId;
                });
                if (currentTemplate && currentTemplate.length == 1) {
                    currentTemplate[0].currentTemplate = true;
                }
            }
            this.model = templates;
        });
    }

    static readonly $inject = ["$http", "$mdToast", "RestUrlService", "RegisterTemplateService"];
    constructor(private $http: angular.IHttpService, private $mdToast: angular.material.IToastService, private RestUrlService: any, private registerTemplateService: RegisterTemplateServiceFactory) {

    }
    saveOrder() {

        var order: any = [];
        _.each(this.model, (template: any) => {
            order.push(template.id);
        });

        var successFn = (response: any) => {
            //toast created!!!
            var message = 'Saved the template order';
            this.message = message;
            this.$mdToast.show(
                this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000)
            );

        }
        var errorFn = (err: any) => {
            var message = 'Error ordering templates ' + err;
            this.message = message;
            this.$mdToast.simple()
                .textContent(message)
                .hideDelay(3000);
        }

        var obj = { templateIds: order };
        var promise = this.$http({
            url: this.RestUrlService.SAVE_TEMPLATE_ORDER_URL,
            method: "POST",
            data: angular.toJson(obj),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);

    }

}
angular.module(moduleName).component("templateOrderController", {
    bindings: {
        templateId: '=',
        templateName: '=',
        model: '=?',
        addAsNew: '=?',
        addSaveBtn: '=?'
    },
    controllerAs: 'vm',
    templateUrl: './template-order.html',
    controller: TemplateOrderController,
})
