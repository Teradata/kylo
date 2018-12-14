import {Subject} from "rxjs/Subject";
import {PartialObserver} from "rxjs/Observer";
import {ISubscription} from "rxjs/Subscription";
import {Injectable} from "@angular/core";
import {InfoItemComponent} from "./info-item.component";
import {ItemSaveResponse} from './item-save-response';

@Injectable()
export class InfoItemService{

    itemSavedSubject :Subject<ItemSaveResponse> = new Subject<ItemSaveResponse>();

    activeEdit:InfoItemComponent;

    constructor(){

    }

    isActiveEdit(component:InfoItemComponent){
        return this.activeEdit && this.activeEdit == component;
    }

    clearActiveEdit(cancel:boolean = true){
        if(this.activeEdit && cancel){
            this.activeEdit.onCancel();
        }
        this.activeEdit = undefined;
    }
    setActiveEdit(activeEdit:InfoItemComponent){
        this.clearActiveEdit();
        this.activeEdit = activeEdit;
    }

    subscribe(o:PartialObserver<ItemSaveResponse>):ISubscription{
        return this.itemSavedSubject.subscribe(o)
    }


    onSaved(response:ItemSaveResponse){
        this.itemSavedSubject.next(response);
    }
}