import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {FattableComponent} from "./fattable.component";

@NgModule({
    declarations: [
      FattableComponent
        ],
    entryComponents:[

    ],
    exports:[
        FattableComponent
    ],
    imports: [
        CommonModule
    ],
    providers: [    ]
})
export class FattableModule {


}
