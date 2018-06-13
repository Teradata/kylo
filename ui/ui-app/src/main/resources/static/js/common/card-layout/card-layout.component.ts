import * as angular from "angular";
import { Component, Input } from '@angular/core';

@Component({
    selector: 'card-layout',
    templateUrl: 'js/common/card-layout/card-layout.html'
})
export class CardLayoutComponent {

    @Input() headerCss: any;
    @Input() bodyCss: any; 
    @Input() cardCss: any;
    @Input() cardToolbar: any;

    ngOnInit() {

        if(angular.isUndefined(this.cardToolbar)){
            this.cardToolbar = true;
        }
    }
    
}