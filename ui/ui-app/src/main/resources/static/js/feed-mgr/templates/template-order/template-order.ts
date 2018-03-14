import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../module-name";


var directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            templateId: '=',
            templateName: '=',
            model: '=?',
            addAsNew: '=?',
            addSaveBtn: '=?'
        },
        scope: {},
        controllerAs: 'vm',
        templateUrl: 'js/feed-mgr/templates/template-order/template-order.html',
        controller: "TemplateOrderController",
        link: function ($scope:any, element:any, attrs:any, controller:any) {
        }
    };
};

export class TemplateOrderController {

    model:any;
    addAsNew:any;
    templateId:any;
    templateName:any;
    currentTemplate:any;
    saveOrder:any;
    message:any;

    constructor(private $http:any, private $mdToast:any,  private RestUrlService:any, private RegisterTemplateService:any) {
        var self = this;
        self.model = [];
        //order list
        RegisterTemplateService.getRegisteredTemplates().then(function (response:any) {


            //order by .order
            var templates = _.sortBy(response.data, 'order');
            if (self.addAsNew && (self.templateId == null || self.templateId == undefined)) {
                //append to bottom
                templates.push({id: 'NEW', templateName: self.templateName, currentTemplate: true});
            }
            else {
                var currentTemplate = _.filter(templates, function (template:any) {
                    return template.id == self.templateId;
                });
                if (currentTemplate && currentTemplate.length == 1) {
                    currentTemplate[0].currentTemplate = true;
                }
            }
            self.model = templates;
        });

        self.saveOrder = function () {

            var order:any = [];
            _.each(self.model, function (template:any) {
                order.push(template.id);
            });

            var successFn = function (response:any) {
                //toast created!!!
                var message = 'Saved the template order';
                self.message = message;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000)
                );

            }
            var errorFn = function (err:any) {
                var message = 'Error ordering templates ' + err;
                self.message = message;
                $mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000);
            }

            var obj = {templateIds: order};
            var promise = $http({
                url: RestUrlService.SAVE_TEMPLATE_ORDER_URL,
                method: "POST",
                data: angular.toJson(obj),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);

        }

    }

}
angular.module(moduleName).controller("TemplateOrderController", ["$http","$mdToast","RestUrlService","RegisterTemplateService",TemplateOrderController]);
angular.module(moduleName).directive("thinkbigTemplateOrder", directive);
