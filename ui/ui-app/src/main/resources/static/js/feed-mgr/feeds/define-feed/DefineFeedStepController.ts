import {Input, OnInit} from "@angular/core";
import * as angular from "angular";
import {IAugmentedJQuery, IOnInit, IPostLink, ITranscludeFunction} from "angular";

import {StepperController} from "../../../common/stepper/stepper";

/**
 * An individual step in the Define Feed wizard.
 */
export class KyloDefineFeedStep implements IOnInit, OnInit {

    @Input()
    public step: string;

    public stepperController: StepperController;

    @Input()
    public title: string;

    static readonly $inject = ["$scope", "$transclude"];

    constructor($scope: IScope, $transclude: ITranscludeFunction) {
        $scope.$transclude = $transclude;
    }

    public $onInit() {
        this.ngOnInit();
    }

    public ngOnInit() {
        if (this.step != undefined) {
            this.stepperController.assignStepName(this.step, this.title);
        }
        else {
            console.error("UNDEFINED STEP!!!", this);
        }
    }
}

/**
 * Transcludes the HTML contents of a <kylo-define-feed-step/> into the template of kyloDefineFeedStep.
 */
export class KyloDefineFeedStepTransclude implements IPostLink {

    static readonly $inject = ["$scope", "$element"];

    constructor(private $scope: IScope, private $element: IAugmentedJQuery) {
    }

    $postLink() {
        (this.$scope.$parent as IScope).$transclude((clone: IAugmentedJQuery) => {
            this.$element.empty();
            this.$element.append(clone);
        });
    }
}

angular.module(require('./module-name'))
    .component("kyloDefineFeedStep", {
        bindings: {
            step: "<",
            title: "@"
        },
        controller: KyloDefineFeedStep,
        require: {
            stepperController: "^thinkbigStepper"
        },
        templateUrl: "./define-feed-step.html",
        transclude: true
    })
    .component("kyloDefineFeedStepTransclude", {
        controller: KyloDefineFeedStepTransclude
    });
