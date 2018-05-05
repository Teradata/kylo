var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "angular"], function (require, exports, core_1, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * An individual step in the Define Feed wizard.
     */
    var KyloDefineFeedStep = /** @class */ (function () {
        function KyloDefineFeedStep($scope, $transclude) {
            $scope.$transclude = $transclude;
        }
        KyloDefineFeedStep.prototype.$onInit = function () {
            this.ngOnInit();
        };
        KyloDefineFeedStep.prototype.ngOnInit = function () {
            if (this.step != undefined) {
                this.stepperController.assignStepName(this.step, this.title);
            }
            else {
                console.error("UNDEFINED STEP!!!", this);
            }
        };
        KyloDefineFeedStep.$inject = ["$scope", "$transclude"];
        __decorate([
            core_1.Input(),
            __metadata("design:type", String)
        ], KyloDefineFeedStep.prototype, "step", void 0);
        __decorate([
            core_1.Input(),
            __metadata("design:type", String)
        ], KyloDefineFeedStep.prototype, "title", void 0);
        return KyloDefineFeedStep;
    }());
    exports.KyloDefineFeedStep = KyloDefineFeedStep;
    /**
     * Transcludes the HTML contents of a <kylo-define-feed-step/> into the template of kyloDefineFeedStep.
     */
    var KyloDefineFeedStepTransclude = /** @class */ (function () {
        function KyloDefineFeedStepTransclude($scope, $element) {
            this.$scope = $scope;
            this.$element = $element;
        }
        KyloDefineFeedStepTransclude.prototype.$postLink = function () {
            var _this = this;
            this.$scope.$parent.$transclude(function (clone) {
                _this.$element.empty();
                _this.$element.append(clone);
            });
        };
        KyloDefineFeedStepTransclude.$inject = ["$scope", "$element"];
        return KyloDefineFeedStepTransclude;
    }());
    exports.KyloDefineFeedStepTransclude = KyloDefineFeedStepTransclude;
    angular.module(require('feed-mgr/feeds/define-feed/module-name'))
        .component("kyloDefineFeedStep", {
        bindings: {
            step: "<",
            title: "@"
        },
        controller: KyloDefineFeedStep,
        require: {
            stepperController: "^thinkbigStepper"
        },
        templateUrl: "js/feed-mgr/feeds/define-feed/define-feed-step.html",
        transclude: true
    })
        .component("kyloDefineFeedStepTransclude", {
        controller: KyloDefineFeedStepTransclude
    });
});
//# sourceMappingURL=DefineFeedStepController.js.map