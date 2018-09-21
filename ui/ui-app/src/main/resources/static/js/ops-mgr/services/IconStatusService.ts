import * as angular from "angular";
import {moduleName} from "../module-name";
import IconUtil from "../../services/icon-util";

export default class IconService {
    iconForFeedHealth = IconUtil.iconForFeedHealth
    iconForHealth = IconUtil.iconForHealth

    iconForServiceComponentAlert = IconUtil.iconForServiceComponentAlert

    iconDataForJobStatus = IconUtil.iconDataForJobStatus

    iconForJobStatus = IconUtil.iconForJobStatus

    iconStyleForJobStatus = IconUtil.iconStyleForJobStatus

    colorForJobStatus = IconUtil.colorForJobStatus

    constructor() {
    }

}

angular.module(moduleName).service('IconService', [IconService]);