import * as angular from 'angular';
import * as _ from "underscore";
import { RegisterTemplateServiceFactory } from '../../services/RegisterTemplateServiceFactory';
import { Component, Inject, Input } from '@angular/core';
import { RestUrlService } from '../../services/RestUrlService';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatSnackBar } from '@angular/material';

@Component({
    selector: 'thinkbig-template-order',
    templateUrl: 'js/feed-mgr/templates/template-order/template-order.html'
})
export class TemplateOrderController {

    @Input() addSaveBtn: any;
    @Input() model: any = [];
    @Input() addAsNew: any;
    @Input() templateId: string;
    @Input() templateName: string;
    
    currentTemplate: any;
    message: string;

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

    constructor(private RestUrlService: RestUrlService, 
                private registerTemplateService: RegisterTemplateServiceFactory,
                private http: HttpClient,
                private snackBar: MatSnackBar) {

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
            this.snackBar.open(message,"OK",{
                duration : 3000
            });
        }
        var errorFn = (err: any) => {
            var message = 'Error ordering templates ' + err;
            this.message = message;
            this.snackBar.open(message,"OK",{
                duration : 3000
            });
        }

        var obj = { templateIds: order };
        var promise = this.http.post(this.RestUrlService.SAVE_TEMPLATE_ORDER_URL,
            angular.toJson(obj),
            {headers :  new HttpHeaders({'Content-Type':'application/json; charset=UTF-8'})
        }).toPromise().then(successFn, errorFn);

    }

}