import * as angular from 'angular';
import {BroadcastService} from "./broadcast-service";

import {moduleName} from './module-name';

import "./module"; // ensure module is loaded first

export class AddButtonService {
    addButtons: any = {};
    NEW_ADD_BUTTON_EVENT: any = 'newAddButton';
    HIDE_ADD_BUTTON_EVENT: any = 'hideAddButton';
    SHOW_ADD_BUTTON_EVENT: any = 'showAddButton';
    static readonly $inject = ["BroadcastService"]
    constructor(private broadcastService: BroadcastService) { }
    AddButtonServiceTag() { }

    //__tag = new AddButtonServiceTag();
    __tag = this.AddButtonServiceTag();

    registerAddButton = (state: any, action: any) => {
        this.addButtons[state] = action;
        this.broadcastService.notify(this.NEW_ADD_BUTTON_EVENT, state);
    }
    hideAddButton = () => {
        this.broadcastService.notify(this.HIDE_ADD_BUTTON_EVENT);
    }
    showAddButton = () => {
        this.broadcastService.notify(this.SHOW_ADD_BUTTON_EVENT);
    }
    isShowAddButton = (state: any) => {
        return this.addButtons[state] != undefined;
    }
    unregisterAddButton = (state: any) => {
        this.addButtons[state];
    }
    onClick = (state: any) => {
        var action = this.addButtons[state];
        if (action) {
            action();
        }
    }
}
angular.module(moduleName).service('AddButtonService', AddButtonService);
