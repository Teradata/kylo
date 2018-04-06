import * as angular from "angular";
import {moduleName} from "../module-name";

export default class EventService{
    constructor(private $rootScope: any){}
}

  angular.module(moduleName).service('EventService',["$rootScope", EventService]);