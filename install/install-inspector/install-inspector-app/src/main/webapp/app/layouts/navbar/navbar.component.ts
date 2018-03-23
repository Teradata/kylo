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

import {VERSION} from '../../app.constants';
import {JhiLanguageHelper} from '../../shared/language/language.helper';
import {JhiLanguageService} from 'ng-jhipster';

@Component({
    selector: 'jhi-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: [
        'navbar.scss'
    ]
})
export class NavbarComponent implements OnInit {
    version: string;
    languages: any[];

    constructor(
        private languageService: JhiLanguageService,
        private languageHelper: JhiLanguageHelper,
    ) {
        this.version = VERSION ? 'v' + VERSION : '';
    }

    ngOnInit() {
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
            this.languageService.changeLanguage(this.languages[0]);
        });
    }
}
