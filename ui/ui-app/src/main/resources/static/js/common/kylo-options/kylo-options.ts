import * as angular from "angular";
import {moduleName} from "../module-name";
import { AboutKyloService } from "../about-kylo/AboutKyloService";

export default class KyloOptions implements ng.IComponentController {

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        //default the icon to be more_vert
        if (!angular.isDefined(this.$scope.menuIcon)) {
            this.$scope.menuIcon = 'more_vert';
        }

        // Get user name
        this.$scope.username = "User";
        this.$http.get("/proxy/v1/about/me").then((response: any) => {
            this.$scope.username = response.data.systemName;
        });

        this.$scope.openMenu = ($mdOpenMenu: any, ev: any) => {
            //callback
            if (this.$scope.openedMenu) {
                this.$scope.openedMenu();
            }
            $mdOpenMenu(ev);
        };

        this.$scope.aboutKylo = () => {
            this.AboutKyloService.showAboutDialog();
            if (this.$scope.selectedOption) {
                this.$scope.selectedOption()('aboutKylo');
            }
        };

        /**
         * Redirects the user to the logout page.
         */
        this.$scope.logout = () => {
            this.$window.location.href = "/logout";
        }
    }

    static readonly $inject = ["$scope", "$http", "$mdDialog", "$window", "AboutKyloService"];

    constructor(private $scope: IScope,
                private $http: angular.IHttpService, 
                private $mdDialog: angular.material.IDialogService, 
                private $window: angular.IWindowService, 
                private AboutKyloService: AboutKyloService) {}
}

angular.module(moduleName).component("kyloOptions", {
    controller: KyloOptions,
    templateUrl: 'js/common/kylo-options/kylo-options.html',
});
