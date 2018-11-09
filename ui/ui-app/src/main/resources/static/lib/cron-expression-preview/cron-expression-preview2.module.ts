import {NgModule} from "@angular/core";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from '@angular/common';
import {CronExpressionPreviewComponent} from './cron-expression-preview2.component';

@NgModule({
    declarations: [
        CronExpressionPreviewComponent
    ],
    entryComponents: [],
    exports: [
        CronExpressionPreviewComponent
    ],
    imports: [
        CommonModule,
        TranslateModule
    ],
    providers: []
})
export class CronExpressionPreviewModule {


}
