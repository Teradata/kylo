import {ElementRef} from "@angular/core";
import * as angular from "angular";
import {IDirective} from "angular";

import {moduleName} from "../module-name";
import {BrowserHeight} from "./browser-height";

angular.module(moduleName).directive("browserHeight", function () {
    return new class implements IDirective {
        link($scope: angular.IScope, $element: angular.IAugmentedJQuery, attrs: {[k: string]: string}) {
            const browserHeight = new BrowserHeight(new ElementRef($element[0]));
            browserHeight.eleSelector = attrs.browserHeightSelector;
            browserHeight.scrollY = (attrs.browserHeightScrollY == null || attrs.browserHeightScrollY != "false");
            browserHeight.browserHeightWaitAndCalc = (attrs.browserHeightWaitAndCalc != null && attrs.browserHeightWaitAndCalc == "true");
            browserHeight.browserHeightScrollLeft = (attrs.browserHeightScrollLeft != null) ? (attrs.browserHeightScrollLeft == "true") : null;
            browserHeight.browserHeightScrollX = (attrs.browserHeightScrollX == "true");
            browserHeight.bindResize = (attrs.browserHeightResizeEvent == null || attrs.browserHeightResizeEvent == "true");
            browserHeight.offsetHeight = attrs.browserHeightOffset ? parseInt(attrs.browserHeightOffset) : 0;
            browserHeight.ngOnInit();

            $scope.$on("destroy", () => browserHeight.ngOnDestroy());
        }
    }
});
