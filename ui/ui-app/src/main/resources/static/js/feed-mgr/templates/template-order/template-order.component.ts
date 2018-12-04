import * as _ from "underscore";
import { RegisterTemplateServiceFactory } from '../../services/RegisterTemplateServiceFactory';
import { Component, Inject, Input } from '@angular/core';
import { RestUrlService } from '../../services/RestUrlService';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ObjectUtils } from '../../../../lib/common/utils/object-utils';
import { TranslateService } from "@ngx-translate/core";

@Component({
    selector: 'thinkbig-template-order',
    templateUrl: './template-order.html'
})
export class TemplateOrderController {

    @Input() addSaveBtn: boolean = false;
    @Input() model: any = [];
    @Input() addAsNew: boolean = false;
    @Input() templateId: string;
    @Input() templateName: string;
    
    currentTemplate: any;
    message: string;

    ngOnInit() {
        //order list
        this.registerTemplateService.getRegisteredTemplates().then((response: any)=> {
            //order by .order
            var templates = _.sortBy(response, 'order');
            if (this.addAsNew && (this.templateId == null || this.templateId == undefined)) {
                //append to bottom
                templates.push({ id: 'NEW', templateName: this.templateName, currentTemplate: true });
            }
            else {
                var currentTemplate = _.filter(templates, (template: any)=> {
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
                private snackBar: MatSnackBar,
                private translate : TranslateService) {

    }
    saveOrder() {

        var order: any = [];
        _.each(this.model, (template: any) => {
            order.push(template.id);
        });

        var successFn = (response: any) => {
            //toast created!!!
            var message = this.translate.instant("FEEDMGR.TEMPLATES.ORDER.ORDER_SAVED");
            this.message = message;
            this.snackBar.open(message,this.translate.instant("view.main.ok"),{
                duration : 3000
            });
        }
        var errorFn = (err: any) => {
            var message = this.translate.instant("FEEDMGR.TEMPLATES.ORDER.ERROR_IN_SAVE", {error : err}) ;
            this.message = message;
            this.snackBar.open(message,this.translate.instant("view.main.ok"),{
                duration : 3000
            });
        }

        var obj = { templateIds: order };
        var promise = this.http.post(this.RestUrlService.SAVE_TEMPLATE_ORDER_URL,
            ObjectUtils.toJson(obj),
            {headers :  new HttpHeaders({'Content-Type':'application/json; charset=UTF-8'})
        }).toPromise().then(successFn, errorFn);

    }

}
