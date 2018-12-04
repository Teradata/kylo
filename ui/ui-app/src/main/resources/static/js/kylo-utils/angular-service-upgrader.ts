import {FactoryProvider} from "@angular/core";

export class AngularServiceUpgrader {
    constructor(){

    }

    // static upgrade(service:any,name:string = service.name) :FactoryProvider{
    //     return {
    //         provide: service,
    //         useFactory: (i: angular.auto.IInjectorService) => i.get(name),
    //         deps: ["$injector"]
    //     }
    // }
}