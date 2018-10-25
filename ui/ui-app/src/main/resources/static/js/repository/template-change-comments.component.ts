import {Component, Inject, Input} from "@angular/core";

@Component({
    selector: 'template-change-comments',
    styleUrls:["./template-change-comments.component.css"],
    templateUrl: './template-change-comments.component.html',
})
export class TemplateChangeCommentsComponent {

    @Input()
    updates: any[];
}