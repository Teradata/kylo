import {Component, OnInit} from '@angular/core';

import {VERSION} from '../../app.constants';

@Component({
    selector: 'jhi-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: [
        'navbar.scss'
    ]
})
export class NavbarComponent implements OnInit {
    version: string;

    constructor() {
        this.version = VERSION ? 'v' + VERSION : '';
    }

    ngOnInit() {
    }
}
