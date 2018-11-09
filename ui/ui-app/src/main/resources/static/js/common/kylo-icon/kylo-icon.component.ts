import {Component, ElementRef, Inject, Input, OnChanges, SimpleChanges} from "@angular/core";
import {MatIconRegistry} from "@angular/material/icon";

declare const SVGMorpheus: any;

/**
 * Displays an SVG icon.
 */
@Component({
    selector: "kylo-icon,ng-md-icon",
    template: ""
})
export class KyloIconComponent implements OnChanges {

    static readonly DEFAULT_ICON = "help";

    static readonly DEFAULT_SIZE = "24";

    static readonly DEFAULT_VIEWBOX = "0 0 24 24";

    /**
     * Name of the icon
     */
    @Input()
    public icon: string;

    /**
     * Size of the icon
     */
    @Input()
    public size: string;
    @Input()
    public width?:string;
    @Input()
    public height?:string;

    /**
     * Reference to the <kylo-icon> element
     */
    private element: HTMLElement;

    /**
     * Reference to the <svg> element
     */
    private svg: SVGElement;

    constructor(element: ElementRef, private iconRegistry: MatIconRegistry, @Inject("ngMdIconService") private iconService: any) {
        this.element = element.nativeElement;

        // Manually set template
        this.element.innerHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>";
        this.svg = this.element.childNodes.item(0) as SVGElement;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.icon) {
            if (changes.icon.currentValue === null || typeof changes.icon.currentValue !== "string") {
                this.setIcon();
            } else if (typeof this.iconService.getShape(changes.icon.currentValue) !== "undefined") {
                // Icon is registered with ngMdIconService
                this.setIcon(changes.icon.currentValue);
            } else {
                // Check MatIconRegistry for icon
                const key = changes.icon.currentValue.split(":", 2);
                try {
                    this.iconRegistry.getNamedSvgIcon((key.length == 2) ? key[1] : key[0], (key.length == 2) ? key[0] : "").subscribe(
                        svg => this.setShape(svg.innerHTML, svg.getAttribute("viewBox")),
                        error => {
                            console.error(error);
                            this.setIcon();
                        }
                    );
                } catch (error) {
                    console.error(error);
                    this.setIcon();
                }
            }
        }
        if (changes.width) {
            this.element.style.width = changes.width.currentValue ? changes.width.currentValue + "px" : null;
            this.svg.setAttribute("width", changes.width.currentValue || KyloIconComponent.DEFAULT_SIZE);
        }
        if(changes.height) {
            this.element.style.height = changes.height.currentValue ? changes.height.currentValue + "px" : null;
            this.svg.setAttribute("height", changes.height.currentValue || KyloIconComponent.DEFAULT_SIZE);
        }
        if (changes.size) {
            this.element.style.height = changes.size.currentValue ? changes.size.currentValue + "px" : null;
            this.element.style.width = changes.size.currentValue ? changes.size.currentValue + "px" : null;
            this.svg.setAttribute("height",  changes.size.currentValue || KyloIconComponent.DEFAULT_SIZE);
            this.svg.setAttribute("width", changes.size.currentValue || KyloIconComponent.DEFAULT_SIZE);
        }
    }

    /**
     * Sets the icon with the specified name in {@code ngMdIconService}.
     */
    private setIcon(name?: string): void {
        const shape = this.iconService.getShape(name);
        if (typeof shape !== "undefined") {
            this.setShape(shape, this.iconService.getViewBox(name));
        } else {
            this.setShape(this.iconService.getShape(KyloIconComponent.DEFAULT_ICON), this.iconService.getViewBox(KyloIconComponent.DEFAULT_ICON));
        }
    }

    /**
     * Changes the SVG shape to the specified value.
     */
    private setShape(shape: string, viewBox: string): void {
        if (this.svg.innerHTML == "") {
            // First-time initialization
            this.svg.innerHTML = shape;
        } else if (this.svg.innerHTML !== shape) {
            // Morph old icon into new icon
            this.svg.innerHTML = `<g id="newicon" style="display:none">${shape}</g><g id="oldicon" style="display:none">${this.svg.innerHTML}</g>`;
            try {
                new SVGMorpheus(this.svg).to("newicon", null, () => {
                    const path = this.svg.querySelector('path[fill="rgba(-1,-1,-1,0)"]');
                    if (path) {
                        path.setAttribute("fill", "");
                    }
                });
            } catch {
                // SVGMorpheus not supported
                this.svg.innerHTML = shape;
            }
        }

        // Update the view box
        this.svg.setAttribute("viewBox", viewBox || KyloIconComponent.DEFAULT_VIEWBOX);
    }
}
