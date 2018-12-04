import {Point} from "../draggable.directive";

export class DragSelectionRect
{
    public x:number;
    public y:number
    public width:number = 0;
    public height:number = 0;

    constructor(private startingPoint:Point) {
        this.x = this.startingPoint.x;
        this.y = this.startingPoint.y;
    }

    update(curPoint:Point) {
        this.x = curPoint.x > this.startingPoint.x ? this.startingPoint.x : curPoint.x;
        this.y = curPoint.y > this.startingPoint.y ? this.startingPoint.y : curPoint.y;
        this.width = curPoint.x > this.startingPoint.x ? curPoint.x - this.startingPoint.x : this.startingPoint.x - curPoint.x;
        this.height = curPoint.y > this.startingPoint.y ? curPoint.y - this.startingPoint.y : this.startingPoint.y - curPoint.y;
    }
}