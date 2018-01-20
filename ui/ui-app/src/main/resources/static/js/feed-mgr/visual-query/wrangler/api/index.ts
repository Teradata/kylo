import {InjectionToken, Injector} from "@angular/core";

import {DialogService} from "./services/dialog.service";

export * from "./column";
export * from "./data-source";
export * from "./rest-model";
export * from "./wrangler-engine";

// Services
export * from "./services/dialog.service";

/**
 * Injection token for the Dialog Service.
 */
export const DIALOG_SERVICE = new InjectionToken<DialogService>("WranglerDialogService");

/**
 * Injection token for the Wrangler injector.
 */
export const INJECTOR = new InjectionToken<Injector>("$$wranglerInjector");
