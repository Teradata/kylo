/**
 * Used to store temporary state of the Edit Feed Nifi Properties
 * when a user clicks the Edit link for the Feed Details so the object can be passed to the template factory
 *
 */

import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

export class EditFeedNifiPropertiesService {
    editFeedModel:any;
    constructor () {

        var self = this;
        this.editFeedModel = {};

    }
}

angular.module(moduleName).service('EditFeedNifiPropertiesService', EditFeedNifiPropertiesService); 