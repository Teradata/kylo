import * as _ from "underscore";
import {Component, OnInit} from "@angular/core";
import {AbstractDatasetInfoItemComponent} from '../abstract-dataset-info-item.component';
import {DatasetService} from '../../dataset-service';
import {DatasetLoadingService} from '../../dataset-loading-service';
import {InfoItemService} from '../../../../../../shared/info-item/item-info.service';
import {ItemSaveResponse} from '../../../../../../shared/info-item/item-save-response';
import {Tag} from '../../../../../../model/schema-field';
import {StateService} from '@uirouter/core';


@Component({
    selector:"dataset-info-tags",
    templateUrl:"./dataset-info-tags.component.html"
})
export class DatasetInfoTagsComponent  extends AbstractDatasetInfoItemComponent implements OnInit{

    /**
     * The feed tag objects
     */
    tags:Tag[];
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

    constructor(itemInfoService: InfoItemService,
                datasetService: DatasetService,
                datasetLoadingService: DatasetLoadingService,
                stateService: StateService) {
        super(itemInfoService, datasetService, datasetLoadingService, stateService);
    }

    initForm(){
        //no op.
        //FormGroup is not used here
    }

    ngOnInit(){

        this.init();
    }

    init(){
        this.tags = this.dataset.tags || [];
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
        this.dataset.tags = this.tags;
        this.saveDataset(this.dataset);
    }

    onSaveSuccess(response: ItemSaveResponse){
        this.init();
    }

    cancel(){
     this.init();
    }

}