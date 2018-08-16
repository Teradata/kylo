import * as _ from 'underscore';
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from '../../services/RestUrlService';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {  TdDialogService } from '@covalent/core/dialogs';
import { ObjectUtils } from '../../../common/utils/object-utils';

@Injectable()
export default class SlaEmailTemplateService {
    data: any = {};
    template: any = null;
    templates: any[];
    templateMap: any;
    availableActions: any[];
    
    constructor(private RestUrlService: RestUrlService,
                private http: HttpClient,
                private _tdDialogService : TdDialogService
               ) {
        
        this.data = {
            template:this.template,
            templates:this.templates,
            templateMap:{},
            availableActions:[]
        }            
        this.getExistingTemplates();

    };
    newTemplate () {
        this.data.template = this.newTemplateModel();
    };
    getTemplateVariables () {
        var injectables = [{ "item": "$sla.name", "desc": "The SLA Name." },
        { "item": "$sla.description", "desc": "The SLA Description." },
        { "item": "$assessmentDescription", "desc": "The SLA Assessment and result." }];
        return injectables;
    }
    getExistingTemplates () {
        var promise = this.http.get("/proxy/v1/feedmgr/sla/email-template").toPromise();
        promise.then((response: any) => {
            if (response) {
                this.data.templates = response;
                this.data.templateMap = {};
                _.each(this.data.templates, (template: any) => {
                    this.data.templateMap[template.id] = template;
                })
            }
        });
        return promise;
    };

    getRelatedSlas (id: any) {
        return this.http.get("/proxy/v1/feedmgr/sla/email-template-sla-references", { params: { "templateId": id } });
    };
    getTemplate (id: any) {
        return this.data.templateMap[id];
    };
    getAvailableActionItems () : Promise<any>{
        if (this.data.availableActions == undefined || this.data.availableActions == null || this.data.availableActions.length == 0) {
            return new Promise<any>((resolve,reject) => {
                this.http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions").toPromise().then((response: any) => {
                    if (response) {
                        this.data.availableActions = response;
                        resolve(this.data.availableActions);
                    }else{
                        reject();
                    }
                });
            })
        }
        else {
            new Promise<any>((resolve,reject) => { resolve(this.data.availableActions)});
        }
    }
    validateTemplate (subject: any, templateString: any) {
        if (subject) {
            subject = this.template.subject;
        }
        if (ObjectUtils.isUndefined(templateString)) {
            templateString = this.template.template;
        }
        var testTemplate = { subject: subject, body: templateString };
        return this.http.post("/proxy/v1/feedmgr/sla/test-email-template",
                ObjectUtils.toJson(testTemplate),
                {headers :  new HttpHeaders({'Content-Type':'application/json; charset=utf-8'})
        });
    };
    sendTestEmail (address: any, subject: any, templateString: any) {
        if (ObjectUtils.isUndefined(subject)) {
            subject = this.template.subject;
        }
        if (ObjectUtils.isUndefined(templateString)) {
            templateString = this.template.template;
        }
        var testTemplate = { emailAddress: address, subject: subject, body: templateString };
        return this.http.post("/proxy/v1/feedmgr/sla/send-test-email-template",
        ObjectUtils.toJson(testTemplate),
            {headers: new HttpHeaders({'Content-Type': 'application/json; charset=UTF-8'})
        });
    };
    save (template: any) {
        if (ObjectUtils.isUndefined(template)) {
            template = this.data.template;
        }
        if (template != null) {
            return this.http.post("/proxy/v1/feedmgr/sla/email-template",
                ObjectUtils.toJson(template),
                {headers: new HttpHeaders({'Content-Type': 'application/json; charset=UTF-8'})
            });
        }
    };
    accessDeniedDialog () {
        this._tdDialogService.openAlert({
            disableClose : false,
            title: "Access Denied",
            message: "You do not have access to edit templates.",
            ariaLabel : "Access denied to edit templates",
            closeButton : "OK"

        });
    }
    newTemplateModel () {
        this.template = { name: '', subject: '', template: '' };
        return this.template;
    }
}
