import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from "@angular/platform-browser-dynamic";
import {setAngularJSGlobal} from '@angular/upgrade/static';
import {servicesPlugin} from "@uirouter/core";
import * as angular from 'angular';

import {KyloModule} from "./app.module";

setAngularJSGlobal(angular);

enableProdMode();

// Fix @uirouter/core unable to load
servicesPlugin(null);

// Manually bootstrap the Angular app
platformBrowserDynamic().bootstrapModule(KyloModule).catch(err => console.log("Failed to bootstrap KyloModule", err));
