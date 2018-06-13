import {Component, ElementRef} from "@angular/core";
import * as angular from "angular";
import {moduleName} from "../module-name";
import { AboutKyloService } from "../about-kylo/AboutKyloService";
import { HttpClient } from '@angular/common/http';

@Component({
    selector: "kylo-options",
    templateUrl: "js/common/kylo-options/kylo-options.html",
    styleUrls: ["js/common/kylo-options/kylo-options-style.css"]
})
export class KyloOptionsComponent {

    menuIcon: any;
    username: any;
    openedMenu: any;
    selectedOption: any;

    ngOnInit() {

        //default the icon to be more_vert
        if (!angular.isDefined(this.menuIcon)) {
            this.menuIcon = 'more_vert';
        }

        // Get user name
        this.username = "User";
        this.http.get("/proxy/v1/about/me").toPromise().then((response: any) => {
            this.username = response.systemName;
        });

    }

    constructor(private http: HttpClient,
                private AboutKyloService: AboutKyloService) {}

    openMenu = ($mdOpenMenu: any, ev: any) => {
        //callback
        if (this.openedMenu) {
            this.openedMenu();
        }
        // $mdOpenMenu(ev);
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
        window.location.href = "/logout";
    }

}