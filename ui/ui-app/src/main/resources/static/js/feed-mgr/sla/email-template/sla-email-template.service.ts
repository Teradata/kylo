import * as _ from 'underscore';
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import {SlaEmailTemplate} from "./sla-email-template.model";

@Injectable()
export class SlaEmailTemplateService {
    templates: SlaEmailTemplate[] = [];
    templateMap: {[key: string]: SlaEmailTemplate} = {};
    availableActions: any[];
    constructor(private http: HttpClient, private _dialogService:TdDialogService){
        this.getExistingTemplates();
    }

    newTemplate() :SlaEmailTemplate{
        let template = this.newTemplateModel();
        return template;
    };
    getTemplateVariables(){
        var injectables = [{ "item": "$sla.name", "desc": "The SLA name." },
        { "item": "$sla.description", "desc": "The SLA description." },
        { "item": "$assessmentDescription", "desc": "The SLA assessment and result." }];
        return injectables;
    }
    getExistingTemplates() : Observable<SlaEmailTemplate[]>{
        var observable = this.http.get<SlaEmailTemplate[]>("/proxy/v1/feedmgr/sla/email-template");
        observable.subscribe((response: SlaEmailTemplate[]) => {
                this.templates = response;
                this.templateMap = {};
                _.each(this.templates, (template: SlaEmailTemplate) => {
                    this.templateMap[template.id] = template;
                })
        });
        return observable;
    };

    getRelatedSlas(id:string) {
        return this.http.get("/proxy/v1/feedmgr/sla/email-template-sla-references", { params: { "templateId": id } });
    }

    getTemplate(id: string) {
        return this.templateMap[id];
    }

    getAvailableActionItems() {
        if (_.isUndefined(this.availableActions) || this.availableActions.length == 0) {
            return this.http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions");
        }
        else {
         return  Observable.of(this.availableActions);
        }
    }
    validateTemplate(subject: string, templateString: string):Observable<any> {
        if (_.isUndefined(subject)) {
            subject = ''
        }
        if (_.isUndefined(templateString)) {
            templateString = ''
        }
        var testTemplate = { subject: subject, body: templateString };
        return this.http.post( "/proxy/v1/feedmgr/sla/test-email-template",testTemplate);
    }

    sendTestEmail(address: string, subject: string, templateString: string):Observable<any>{
        if (_.isUndefined(subject)) {
            subject = ''
        }
        if (_.isUndefined(templateString)) {
            templateString = ''
        }
        var testTemplate = { emailAddress: address, subject: subject, body: templateString };
        return this.http.post("/proxy/v1/feedmgr/sla/send-test-email-template",testTemplate);
    }
    save(template: any):Observable<any> {
        if(_.isUndefined(template )){
           return Observable.throw("Unable to save empty template");
        }
        return this.http.post("/proxy/v1/feedmgr/sla/email-template", template);
    }
    private accessDeniedDialog() {
       this._dialogService.openAlert({title:"Access Denied",message:"You do not have access to edit templates."});
    }
    private newTemplateModel() :SlaEmailTemplate{
        return {id:null, name: '', subject: '', template: '' ,enabled:false};
    }
}
