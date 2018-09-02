import {FormControl, FormGroup} from "@angular/forms";
import * as _ from "underscore";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../services/define-feed.service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedItemInfoService} from "../feed-item-info.service";
import {Tag} from "../../../../../model/schema-field";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";


@Component({
    selector:"feed-info-tags",
    templateUrl:"js/feed-mgr/feeds/define-feed-ng2/summary/overview/feed-info-tags/feed-info-tags.component.html"
})
export class FeedInfoTagsComponent  extends AbstractFeedInfoItemComponent implements OnInit{

    /**
     * The feed tag objects
     */
    tags:Tag[]
    /**
     * the new tag names
     */
    tagNames:string[];

    /**
     * the existing feed tags as string array
     */
    originalTagNames:string[];

    /**
     * comma separated list of tag names
     */
    tagNamesString: string;

    constructor( defineFeedService:DefineFeedService,  feedItemInfoService:FeedItemInfoService){
        super(defineFeedService,feedItemInfoService)
    }
    initForm(){
        //no op.
        //FormGroup is not used here
    }

    ngOnInit(){

        this.init();
    }

    init(){
        this.tags = this.feed.tags || [];
        this.tagNames = _.isArray(this.tags) ? this.tags.map(_.property("name")) : [];
        this.originalTagNames = _.isArray(this.tags) ? this.tags.map(_.property("name")) : [];
        this.tagNamesString = this.tagNames.join(", ");
    }


    onAddTag(tagName:string){
        this.tags.push({name:tagName});
    }
    onRemoveTag(tagName:string){
        let tag = this.tags.find(tag => tag.name == tagName);
        if(tag){
            let index = this.tags.indexOf(tag);
            this.tags.splice(index,1);
        }
    }

    save() {
        this.feed.tags = this.tags;
        this.saveFeed(this.feed);
    }

    onSaveSuccess(response:SaveFeedResponse){
        this.init();
    }

    cancel(){
     this.init();
    }

}