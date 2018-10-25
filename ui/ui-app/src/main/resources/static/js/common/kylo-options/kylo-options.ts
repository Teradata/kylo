import * as angular from "angular";
import {moduleName} from "../module-name";
import { AboutKyloService } from "../about-kylo/AboutKyloService";

export default class KyloOptions implements ng.IComponentController {

    menuIcon: any;
    username: any;
    openedMenu: any;
    selectedOption: any;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        //default the icon to be more_vert
        if (!angular.isDefined(this.menuIcon)) {
            this.menuIcon = 'more_vert';
        }

        // Get user name
        this.username = "User";
        this.$http.get("/proxy/v1/about/me").then((response: any) => {
            this.username = response.data.systemName;
        });

    }

    openMenu = ($mdOpenMenu: any, ev: any) => {
        //callback
        if (this.openedMenu) {
            this.openedMenu();
        }
        $mdOpenMenu(ev);
    };

    aboutKylo() {
        this.AboutKyloService.showAboutDialog();
        if (this.selectedOption) {
            this.selectedOption()('aboutKylo');
        }
    };

    /**
     * Redirects the user to the logout page.
     */
    logout () {
        this.$window.location.href = "/logout";
    }

    static readonly $inject = ["$http", "$mdDialog", "$window", "AboutKyloService"];

    constructor(private $http: angular.IHttpService, 
                private $mdDialog: angular.material.IDialogService, 
                private $window: angular.IWindowService, 
                private AboutKyloService: AboutKyloService) {}
}

angular.module(moduleName).component("kyloOptions", {
    controller: KyloOptions,
    templateUrl: './kylo-options.html',
});
