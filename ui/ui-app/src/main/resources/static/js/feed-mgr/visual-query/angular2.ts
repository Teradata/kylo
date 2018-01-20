import {Component, Directive, ElementRef, Injector, Input} from "@angular/core";
import {UpgradeComponent} from "@angular/upgrade/static";

import {QueryEngine} from "./wrangler/query-engine";

/**
 * Angular 2 entry component for Visual Query page.
 */
@Component({
    template: `
      <visual-query [engine]="engine"></visual-query>
    `
})
export class VisualQueryComponent {

    @Input()
    engine: QueryEngine<any>;
}

/**
 * Upgrades Visual Query AngularJS entry component to Angular 2.
 */
@Directive({
    selector: 'visual-query'
})
export class VisualQueryDirective extends UpgradeComponent {

    @Input()
    engine: QueryEngine<any>;

    constructor(elementRef: ElementRef, injector: Injector) {
        super('visualQuery', elementRef, injector);
    }
}
