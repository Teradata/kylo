import {Feed} from "../../../model/feed/feed.model";
import {Templates} from "../../../../../lib/feed-mgr/services/TemplateTypes";
import {RegisterTemplatePropertyService} from "../../../services/RegisterTemplatePropertyService";
import {Injectable, Injector} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {RestUrlConstants} from "../../../services/RestUrlConstants";
import * as _ from "underscore";
import {FeedService} from "../../../services/FeedService";
import {NifiFeedPropertyUtil} from "../../../services/nifi-feed-property-util";

@Injectable()
export class FeedNifiPropertiesService {

    private registerTemplatePropertyService :RegisterTemplatePropertyService;
    private feedService:FeedService;


    constructor(private http:HttpClient,private $$angularInjector: Injector){
        this.registerTemplatePropertyService = $$angularInjector.get("RegisterTemplatePropertyService");
        this.feedService = $$angularInjector.get("FeedService");
    }


    setProcessorRenderTemplateUrl(feed:Feed,mode:string) {
        this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(feed, mode);
    }

    /**
     * Get template properties
     * @param {string} templateId
     * @return {Observable<any>}
     */
    getTemplateProperties(templateId:string):Observable<any>{
        let params  = new HttpParams().append("feedEdit","true").append("allProperties","true");
        return this.http.get(this.registerTemplatePropertyService.GET_REGISTERED_TEMPLATES_URL+"/"+templateId, { params:params});
    }

    /**
     * returns a Feed updated with properties associated with its template
     * @param {Feed} feed
     * @return {Observable<any>}
     */
    mergeFeedWithTemplate(feed:Feed):Observable<any>{
        let steps = feed.steps;
        let feedCopy = feed.copy(false);
        delete feedCopy.steps;
        return this.http.post(RestUrlConstants.MERGE_FEED_WITH_TEMPLATE(feed.id), feedCopy, {headers: {'Content-Type': 'application/json; charset=UTF-8'}})
    }


    sortAndSetupFeedProperties(feed:Feed, setFeedInputProcessor:boolean = false){
        if((feed.inputProcessors == undefined || feed.inputProcessors.length == 0) && feed.registeredTemplate){
            feed.inputProcessors = feed.registeredTemplate.inputProcessors;
        }

        feed.inputProcessors= _.sortBy(feed.inputProcessors, 'name')
        // Find controller services
        _.chain(feed.inputProcessors.concat(feed.nonInputProcessors))
            .pluck("properties")
            .flatten(true)
            .map(prop => {
                NifiFeedPropertyUtil.initSensitivePropertyForEditing(prop);
                NifiFeedPropertyUtil.updateDisplayValue(prop);
                return prop;
            })
            .filter((property) => {
                return property != undefined && property.propertyDescriptor && property.propertyDescriptor.identifiesControllerService && (typeof property.propertyDescriptor.identifiesControllerService == 'string' );
            })
            .each((property:any) => this.feedService.findControllerServicesForProperty(property));

        //find the input processor associated to this feed
        feed.inputProcessor = feed.inputProcessors.find((processor: Templates.Processor) => {
            if (feed.inputProcessorName) {
                return   feed.inputProcessorType == processor.type && feed.inputProcessorName.toLowerCase() == processor.name.toLowerCase()
            }
            else {
                return    feed.inputProcessorType == processor.type;
            }
        });
        if(setFeedInputProcessor && feed.inputProcessor == undefined && feed.inputProcessors && feed.inputProcessors.length >0){
            feed.inputProcessor = feed.inputProcessors[0];
        }

        if (feed.properties) {
            feed.properties.forEach(property => {
                //if it is sensitive treat the value as encrypted... store it off and use it later when saving/posting back if the value has not changed
                NifiFeedPropertyUtil.initSensitivePropertyForEditing(property);
                NifiFeedPropertyUtil.updateDisplayValue(property);
            });
        }



    }

    setupFeedProperties(feed:Feed,template:any, mode:string, setFeedInputProcessor:boolean = false) {
        if(feed.isNew()){
            this.registerTemplatePropertyService.initializeProperties(template, 'create', feed.properties);
        }
        else {
            this.registerTemplatePropertyService.initializeProperties(template, "edit", []);
        }

        //merge the non input processors
        feed.nonInputProcessors = this.registerTemplatePropertyService.removeNonUserEditableProperties(template.nonInputProcessors, false);

        this.sortAndSetupFeedProperties(feed, setFeedInputProcessor);


        // this.inputProcessors = template.inputProcessors;
        feed.allowPreconditions = template.allowPreconditions;
    }
}
