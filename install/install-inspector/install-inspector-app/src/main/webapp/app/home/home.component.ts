/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import {Component, OnInit} from '@angular/core';

import {ConfigService} from '../shared';
import {AbstractControl, FormControl, ValidatorFn, Validators} from '@angular/forms';
import {saveAs} from 'file-saver/FileSaver';
import {ProfileService} from '../shared/profile/profile.service';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss'
    ]
})
export class HomeComponent implements OnInit {
    isProd = true;
    checks: Array<any> = [];
    isLoading: boolean;
    selectedCheckId = -1;
    path = new FormControl('', [Validators.required, configurationValidator(this)]);
    devMode = new FormControl(false, [Validators.required]);
    selectAll = new FormControl(false, [Validators.required]);
    configuration: any;
    docBase: 'http://kylo.readthedocs.io/en/latest';

    constructor(private configService: ConfigService, private profileService: ProfileService) {
    }

    ngOnInit() {
        const self = this;

        this.profileService.getProfileInfo().toPromise().then((profiles) => {
            console.log('loaded application profiles: ' + profiles);
            this.isProd = profiles.indexOf('prod') !== -1;
        }).catch((err) => {
            this.isProd = true;
            console.log('failed to load active profiles from server');
        });

        // this.loadChecks().toPromise().then((checks) => {
        //     if (checks) {
        //         console.log('loaded checks', checks);
        //         checks.forEach(function(check) {
        //             console.log('for each check', check);
        //             console.log('this', this);
        //             console.log('self', self);
        //             check.enabled = new FormControl(false);
        //             check.status = {};
        //         });
        //         self.checks = checks;
        //         if (self.checks.length > 0) {
        //             self.selectedCheckId = self.checks[0].id;
        //         }
        //         self.isLoading = false;
        //     } else {
        //         console.log('there are no checks configured on server');
        //         self.checks = null;
        //         self.isLoading = false;
        //     }
        //     return self.checks;
        // }).catch((err) => {
        //     console.log('error getting configured checks from server');
        //     self.checks = null;
        //     self.isLoading = false;
        //     return null;
        // });
    }

    checkConfig() {
        console.log('checkConfig for ' + this.path);
        this.reset();
        this.configService.setPath(this.path.value, this.devMode.value).toPromise().then((configuration) => {
            if (configuration) {
                console.log('created new configuration', configuration);
                this.configuration = configuration;
                this.checks = configuration.inspections;
                this.isLoading = false;
            } else {
                console.log('failed to create configuration on path ' + this.path);
                this.configuration = {error: true, errorPath: this.path.value, devmode: this.devMode.value};
                this.path.updateValueAndValidity();
                this.isLoading = false;
            }
            return configuration;
        });
        // .then(this.executeChecks)
        //     .catch((err) => {
        //         console.log('error creating configuration on path ' + this.path);
        //         this.configuration = {error: true, errorPath: this.path.value, devmode: this.devMode.value};
        //         this.path.updateValueAndValidity();
        //         this.isLoading = false;
        //         return null;
        //     });
    }

    downloadReport() {
        console.log('download report');
        const result = {
            configuration: this.configuration,
            inspections: this.checks.map((inspection) => {
                return {
                    name: inspection.name,
                    description: inspection.description,
                    enabled: inspection.enabled.value,
                    valid: inspection.status.valid,
                    errors: inspection.status.errors
                }
            })
        };
        const blob = new Blob([JSON.stringify(result)], {type: 'application/json'});
        saveAs(blob, 'kylo-configuration-inspection.json');
    }

    loadChecks() {
        console.log('loading checks');
        return this.configService.loadChecks();
    }

    executeChecks = (configuration: any) => {
        console.log('execute checks for configuration ' + configuration.path.uri);
        const checks = this.checks
            .filter((check) => check.enabled.value)
            .map((check) => {
                check.isLoading = true;
                return this.configService.executeCheck(configuration.id, check.id).toPromise().then((status) => {
                    console.log('check ' + check.id + ' executed with status ' + status, status);
                    check.isLoading = false;
                    check.status = status;
                    return check.status;
                }).catch((err) => {
                    console.log('error executing check ' + check.id, err);
                    check.isLoading = false;
                    check.status = {valid: false, description: 'Unknown error occurred', error: err};
                    return check.status;
                });
            });
        return Promise.all(checks)
            .then((res) => {
                res.forEach((status) => {
                    console.log('received stats ', status);
                });
            });
    };

    getErrorMessage() {
        if (this.path.hasError('required')) {
            return 'You must enter a value';
        }
        if (this.path.hasError('configurationError')) {
            return 'Failed to read configuration on path "' + this.configuration.errorPath + '"';
        }
        return '';
    }

    onSelectAllChange() {
        if (this.checks) {
            this.checks.forEach((check) => check.enabled.setValue(this.selectAll.value));
        }
    }

    onDevModeChange() {
        this.path.updateValueAndValidity();
    }

    reset() {
        this.configuration = {};
        if (this.checks) {
            this.checks.forEach((check) => check.status = {});
        }
    }

    hasErrors(check: any) {
        return check.status.errors && check.status.errors.length > 0;
    }

    hasDescriptions(check: any) {
        return check.status.descriptions && check.status.descriptions.length > 0;
    }

    docsLink(check: any): string {
        return this.docBase + check.status.docsLink;
    }
}

export function configurationValidator(homeComponent: HomeComponent): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } => {
        if (homeComponent.configuration !== undefined
            && homeComponent.configuration.error
            && homeComponent.configuration.errorPath === control.value
            && homeComponent.configuration.devmode === homeComponent.devMode.value) {
            return {'configurationError': {value: control.value}};
        } else {
            return null;
        }
    };
}
