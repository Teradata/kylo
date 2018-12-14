import {Component, EventEmitter, Input, OnDestroy, Output} from "@angular/core";
import {ISubscription} from "rxjs/Subscription";
import {InfoItemService} from './item-info.service';
import {ItemSaveResponse} from './item-save-response';


@Component({
    selector:"info-item",
    styleUrls:["./info-item.component.scss"],
    templateUrl:"./info-item.component.html"
})
export class InfoItemComponent implements OnDestroy{

    @Input()
    editing:boolean;

    @Input()
    editable:boolean;

    @Input()
    icon:string;
    @Input()
    iconColor:string;

    @Input()
    name:string;

    @Input()
    description:string;

    @Input()
    readonlyList:string[];

    @Input()
    customReadonlyLayout:boolean = false;

    @Input()
    isSaveDisabled:boolean = false;

    @Output()
    edit:EventEmitter<any> = new EventEmitter<any>();

    @Output()
    save:EventEmitter<any> = new EventEmitter<any>();

    @Output()
    cancel:EventEmitter<any> = new EventEmitter<any>();

    itemSubscription:ISubscription;

    constructor( private itemInfoService:InfoItemService){


    }
    ngOnDestroy(){
        if(this.itemInfoService.isActiveEdit(this)) {
            this.itemInfoService.clearActiveEdit();
        }
    }

    onItemSaved(response: ItemSaveResponse) {
        if(response.success){
            this.editing = false;
            this.unsubscribe();
            this.itemInfoService.clearActiveEdit(false)
        }
    }

    onEdit(){
        this.itemInfoService.setActiveEdit(this);
        this.itemSubscription =  this.itemInfoService.subscribe(this.onItemSaved.bind(this))
        this.editing = true
        this.edit.emit();
    }

    onSave(){
        this.save.emit();
    }

    onCancel(){
        this.unsubscribe();
        this.editing = false
        this.cancel.emit();
    }

    private unsubscribe(){
        if(this.itemSubscription && this.itemSubscription != null){
            this.itemSubscription.unsubscribe();
        }
        this.itemSubscription = null;
    }
}