import {HttpClient} from '@angular/common/http';
import {Component} from "@angular/core";
import {ObjectUtils} from "../../../lib/common/utils/object-utils";
import {AboutKyloService} from "../about-kylo/AboutKyloService";

@Component({
    selector: "kylo-options",
    templateUrl: "./kylo-options.html",
    styleUrls: ["./kylo-options-style.css"]
})
export class KyloOptionsComponent {

    menuIcon: any;
    username: any;
    openedMenu: any;
    selectedOption: any;

    ngOnInit() {

        //default the icon to be more_vert
        if (!ObjectUtils.isDefined(this.menuIcon)) {
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

    openMenu () {
        //callback
        if (this.openedMenu) {
            this.openedMenu();
        }
        // $mdOpenMenu(ev);
    };

    openDocs(thisVersion:boolean) {
        const THIS_DOCS_URL_PREFIX = "https://kylo.readthedocs.io/en/v";
        const LATEST_DOCS_URL_PREFIX = "https://kylo.readthedocs.io/en/latest";
        const TRAILING_SLASH = "/";
        const URL = "/proxy/v1/about/version";

        if (thisVersion) {
            this.http.get(URL).toPromise().then(function success(response: any) {
                window.open(THIS_DOCS_URL_PREFIX + response + TRAILING_SLASH);
            }, function failure(response: any) {
                console.log("Could not determine this Kylo version. Will open docs for latest version");
                window.open(LATEST_DOCS_URL_PREFIX + TRAILING_SLASH);
            });
        } else {
            window.open(LATEST_DOCS_URL_PREFIX + TRAILING_SLASH);
        }
    }

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
