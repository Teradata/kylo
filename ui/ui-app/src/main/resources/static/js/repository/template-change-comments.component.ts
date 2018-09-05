import {Component, Inject, Input} from "@angular/core";

@Component({
    selector: 'template-change-comments',
    styleUrls:["js/repository/template-change-comments.component.css"],
    templateUrl: 'js/repository/template-change-comments.component.html',
})
export class TemplateChangeCommentsComponent {

    @Input()
    updates: any[];
}