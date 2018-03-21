import * as angular from 'angular';
import {moduleName} from './module-name';
import BroadcastService from "./broadcast-service";

export default class AddButtonService{
    addButtons: any = {};
        NEW_ADD_BUTTON_EVENT: any = 'newAddButton';
        HIDE_ADD_BUTTON_EVENT: any = 'hideAddButton';
        SHOW_ADD_BUTTON_EVENT: any = 'showAddButton';

 constructor(private BroadcastService: any){}
 AddButtonServiceTag() {}

        //__tag = new AddButtonServiceTag();
         __tag = this.AddButtonServiceTag();
        
        registerAddButton = (state: any, action: any)=> {
            this.addButtons[state] = action;
            this.BroadcastService.notify(this.NEW_ADD_BUTTON_EVENT, state);
        }
        hideAddButton = ()=> {
            this.BroadcastService.notify(this.HIDE_ADD_BUTTON_EVENT);
        }
        showAddButton = ()=> {
            this.BroadcastService.notify(this.SHOW_ADD_BUTTON_EVENT);
        }
        isShowAddButton =  (state: any)=> {
           return this.addButtons[state] != undefined;
        }
        unregisterAddButton =  (state: any) =>{
            this.addButtons[state];
        }
        onClick = (state: any) =>{
            var action = this.addButtons[state];
            if (action) {
                action();
            }
        }
    }

 angular.module(moduleName)
 .service('BroadcastService', ["$rootScope", "$timeout",BroadcastService])
 .service('AddButtonService', ["BroadcastService", AddButtonService]);