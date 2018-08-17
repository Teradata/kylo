import {Injectable} from "@angular/core";
import {Subject} from "rxjs/Subject";
import {Subscription} from "rxjs/Subscription";

import {DataSetButton} from "../models/dataset-button";


@Injectable()
export class DataSetService {

    private actionButtonsList: DataSetButton[] = [];

    private actionButtonsSubject: Subject<void> = new Subject<void>();

    private readySubject: Subject<boolean> = new Subject<boolean>();

    addActionButton(label: string): DataSetButton {
        const button = new DataSetButton(label);
        this.actionButtonsList.push(button);
        this.actionButtonsSubject.next();
        return button;
    }

    getActionButtons(): DataSetButton[] {
        return this.actionButtonsList;
    }

    setReady(isReady: boolean): void {
        this.readySubject.next(isReady);
    }

    subscribeActionButtonsChanged(next: () => void): Subscription {
        return this.actionButtonsSubject.subscribe(next);
    }

    subscribeReady(next: (ready: boolean) => void): Subscription {
        return this.readySubject.subscribe(next);
    }
}
