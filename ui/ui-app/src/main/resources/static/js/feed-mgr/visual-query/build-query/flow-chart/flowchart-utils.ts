import {ElementRef} from "@angular/core";

export class FlowchartUtils {


    // Search up the HTML element tree for an element the requested class.
    public static searchUp(element:any, parentClass: string) :any {
        let $element = $(element);
        // Reached the root.
        if ($element == null || $element.length == 0) {
            return null;
        }
        // Check if the element has the class that identifies it as a connector.
        if (FlowchartUtils.hasClassSVG($element, parentClass)) {
            // Found the connector element.
            return $element;
        }
        // Recursively search parent elements.
        let $parent = $element.parent();
        let parentEle = null;
        if($parent) {
            parentEle =  $parent[0];
        }
        return FlowchartUtils.searchUp(parentEle, parentClass);
    }

    // Hit test and retreive node and connector that was hit at the specified coordinates.
    public static  hitTest(clientX: number, clientY: number) {
        // Retreive the element the mouse is currently over.
        return document.elementFromPoint(clientX, clientY);
    };
    // Hit test and retreive node and connector that was hit at the specified coordinates.
    public static checkForHit(mouseOverElement: Element, whichClass: string): JQuery{
        // Find the parent element, if any, that is a connector.
        var hoverElement = FlowchartUtils.searchUp(mouseOverElement, whichClass);
        if (!hoverElement) {
            return null;
        }
        return hoverElement;
    };
    // Translate the coordinates so they are relative to the svg element.
    public static translateCoordinates(element:any, x:number, y: number, evt: MouseEvent) {
        let svgElem = element;
        if(element.nativeElement) {
            svgElem =  element.nativeElement;
        }

        var matrix = svgElem.getScreenCTM();
        var point :any = {x:0,y:0};

        if(svgElem.createSVGPoint) {
            point = svgElem.createSVGPoint()
        }
        else if(element.nativeElement && element.nativeElement.ownerSVGElement && element.nativeElement.ownerSVGElement.createSVGPoint) {
            point = element.nativeElement.ownerSVGElement.createSVGPoint();
        }

        point.x = x - evt.view.pageXOffset;
        point.y = y - evt.view.pageYOffset;
        return point.matrixTransform(matrix.inverse());
    };

    // http://www.justinmccandless.com/blog/Patching+jQuery's+Lack+of+SVG+Support
// Functions to add and remove SVG classes because jQuery doesn't support this.
// jQuery's removeClass doesn't work for SVG, but this does!
// takes the object obj to remove from, and removes class remove
// returns true if successful, false if remove does not exist in obj
    public static removeClassSVG: any = (obj: any, remove: any)=>{
        var classes: any = obj.attr('class');
        if (!classes) {
            return false;
        }
        var index: any = classes.search(remove);
        // if the class already doesn't exist, return false now
        if (index == -1) {
            return false;
        }
        else {
            // string manipulation to remove the class
            classes = classes.substring(0, index) + classes.substring((index + remove.length), classes.length);
            // set the new string as the object's class
            obj.attr('class', classes);
            return true;
        }
    };

// jQuery's hasClass doesn't work for SVG, but this does!
// takes an object obj and checks for class has
// returns true if the class exits in obj, false otherwise
    public static hasClassSVG: any = (obj: any, has: any)=>{
        var classes = obj.attr('class');
        if (!classes) {
            return false;
        }
        var index: any = classes.search(has);
        if (index == -1) {
            return false;
        }
        else {
            return true;
        }
    };


}