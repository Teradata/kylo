import {Subject} from "rxjs/Subject";
import {Subscription} from "rxjs/Subscription";

export class DataSetButton {

    public hidden = false;

    private _subject = new Subject<any>();

    constructor(public label: string) {
    }

    click(): void {
        this._subject.next();
    }

    subscribeClick(next: () => void): Subscription {
        return this._subject.subscribe(next);
    }
}
