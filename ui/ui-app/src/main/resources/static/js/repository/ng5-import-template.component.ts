import {Component, Directive, ElementRef, Injector, Input} from "@angular/core";
import {UpgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';


/**
 * Angular 5 entry component for Import Template page.
 */
@Component({
    template: `
      <ng5-import-template [template]="template"></ng5-import-template>
    `
})
export class ImportTemplateComponent {

    @Input()
    template: any;
}

/**
 * Upgrades Import Template AngularJS entry component to Angular 5.
 */
@Directive({
    providers: [
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()}
    ],
    selector: 'ng5-import-template'
})
export class ImportTemplateDirective extends UpgradeComponent {

    @Input()
    template: any;

    constructor(elementRef: ElementRef, injector: Injector) {
        super('importTemplateControllerEmbedded', elementRef, injector);
    }
}
