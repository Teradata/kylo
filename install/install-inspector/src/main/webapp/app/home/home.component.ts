import {Component, OnInit} from '@angular/core';
import {NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {JhiEventManager} from 'ng-jhipster';

import {Account, ConfigService, LoginModalService, Principal} from '../shared';
import {Status, StatusState} from '../shared/config/status.model';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss'
    ]

})
export class HomeComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;
    path: String;
    checks: Array<any> = []; // todo make a class
    enabledChecks: Array<any> = []; // todo make a class
    disabledChecks: Array<any> = []; // todo make a class
    isLoading: boolean;
    selectedCheckId = -1;

    constructor(private principal: Principal,
                private loginModalService: LoginModalService,
                private eventManager: JhiEventManager,
                private configService: ConfigService) {
    }

    ngOnInit() {
        const self = this;
        this.principal.identity().then((account) => {
            this.account = account;
        });
        this.registerAuthenticationSuccess();

        this.loadChecks().toPromise().then((checks) => {
            if (checks) {
                console.log('loaded checks', checks);
                checks.forEach(function(check) {
                    console.log('for each check', check);
                    console.log('this', this);
                    console.log('self', self);
                    if (check.status.state !== 'Disabled') {
                        self.enabledChecks.push(check)
                    } else {
                        self.disabledChecks.push(check);
                    }
                });
                if (self.enabledChecks.length > 0) {
                    self.selectedCheckId = self.enabledChecks[0].id;
                } else if (self.disabledChecks.length > 0) {
                    self.selectedCheckId = self.disabledChecks[0].id;
                }
                self.isLoading = false;
                self.checks = checks;
            } else {
                console.log('there are no checks configured on server');
                self.checks = null;
                self.enabledChecks = null;
                self.disabledChecks = null;
                self.isLoading = false;
            }
            return self.checks;
        }).catch((err) => {
            console.log('error getting configured checks from server');
            self.checks = null;
            self.enabledChecks = null;
            self.disabledChecks = null;
            self.isLoading = false;
            return null;
        });
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', (message) => {
            this.principal.identity().then((account) => {
                this.account = account;
            });
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    checkConfig() {
        console.log('checkConfig for ' + this.path);
        this.configService.setPath(this.path).toPromise().then((configuration) => {
            if (configuration) {
                console.log('created new configuration', configuration);
                this.isLoading = false;
            } else {
                console.log('failed to create configuration on path ' + this.path);
                this.isLoading = false;
            }
            return configuration;
        }).then(this.executeChecks)
        .catch((err) => {
            console.log('error creating configuration on path ' + this.path);
            this.isLoading = false;
            return null;
        });
    }

    loadChecks() {
        console.log('loading checks');
        return this.configService.loadChecks();
    }

    executeChecks = (configuration: any) => {
        console.log('execute checks for configuration ' + configuration.path.uri);
        const checks = this.enabledChecks.map((check) => {
            check.status = new Status('Loading');
            return this.configService.executeCheck(configuration.id, check.id).toPromise().then((status) => {
                console.log('check ' + check.id + ' executed with status ' + status, status);
                check.status = status;
                return status;
            }).catch((err) => {
                console.log('error executing check ' + check.id);
                const status = new Status('Failed');
                check.status = status;
                return status;
            });
        });
        return Promise.all(checks)
            .then((res) => {
                res.forEach((status) => {
                    console.log('received stats ', status);
                });
            });
    }

}
