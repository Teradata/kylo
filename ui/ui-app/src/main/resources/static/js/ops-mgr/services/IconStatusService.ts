import {Injectable} from "@angular/core";
import IconUtil from "../../services/icon-util";

@Injectable()
export class IconService {
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
