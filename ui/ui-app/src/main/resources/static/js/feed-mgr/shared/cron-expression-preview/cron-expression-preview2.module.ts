import {NgModule} from "@angular/core";
import {TranslateModule} from "@ngx-translate/core";
import {CronExpressionPreviewComponent} from './cron-expression-preview2.component';
import {CommonModule} from '@angular/common';

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
