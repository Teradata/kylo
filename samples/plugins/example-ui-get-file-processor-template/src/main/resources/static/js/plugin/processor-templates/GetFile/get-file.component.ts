import {Component, OnDestroy, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ProcessorRef} from "@kylo/feed";
import {interval} from "rxjs/observable/interval";
import {Subscription} from "rxjs/Subscription";

@Component({
    selector: "kylo-get-file-processor",
    templateUrl: "./get-file.component.html"
})
export class GetFileComponent implements OnDestroy, OnInit {

    /**
     * A list of icons to cycle through
     */
    icons = ['event', 'build', 'bug_report', 'backup', 'book', 'dashboard'];

    /**
     * A list of colors
     */
    colors = ['red', 'blue', 'green', 'grey', 'black'];

    /**
     * Selected icon
     */
    icon = this.icons[0];

    /**
     * Selected color
     */
    color = this.colors[0];

    /**
     * Interval subscription for cycling icon and colors
     */
    interval: Subscription;

    /**
     * Processor form group
     */
    form = new FormGroup({});

    constructor(readonly processor: ProcessorRef, private snackBar: MatSnackBar) {
        this.processor.control = this.form;
        this.form.valueChanges.subscribe(() => {
            this.snackBar.open("Property changed", null, {duration: 3000});
        });
    }

    ngOnInit(): void {
        this.interval = interval(3000).subscribe(() => {
            this.icon = this.nextIcon();
            this.color = this.randomColor();
        })
    }

    ngOnDestroy(): void {
        if (this.interval) {
            this.interval.unsubscribe();
        }
    }

    /**
     * Get the next Icon in the list
     */
    private nextIcon() {
        let index = this.icons.indexOf(this.icon);
        if (index == this.icons.length - 1) {
            index = 0;
        } else {
            index++;
        }
        return this.icons[index];
    }

    /**
     * Get a random color
     */
    private randomColor() {
        return this.colors[Math.floor(Math.random() * this.colors.length)];
    }
}
