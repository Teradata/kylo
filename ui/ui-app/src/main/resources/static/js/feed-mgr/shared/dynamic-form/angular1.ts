// Angular 1 Vendor Import
import * as angular from 'angular';

import { downgradeInjectable, downgradeComponent } from '@angular/upgrade/static';
import { platformBrowser } from '@angular/platform-browser';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import { StaticProvider } from '@angular/core';


import {DynamicFormComponent} from "./dynamic-form.component";
import {DynamicFormExampleComponent} from "./example/dynamic-form-example.component";

import {DynamicFormService} from "./services/dynamic-form.service";



const formsModule = angular.module("kylo.dynamic-forms",[])

formsModule.directive('dynamicFormExample',downgradeComponent({component:DynamicFormExampleComponent}));

formsModule.service('dynamicFormService',downgradeInjectable(DynamicFormService));

console.log('LOADED THE ng1 dynamic forms ',formsModule)