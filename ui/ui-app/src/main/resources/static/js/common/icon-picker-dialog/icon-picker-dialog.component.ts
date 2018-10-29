import {moduleName} from "../module-name";
import * as _ from "underscore";
import {Component, Input, Output, Inject, ElementRef} from "@angular/core";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { RestUrlService } from "../../feed-mgr/services/RestUrlService";
import { HttpClient } from "@angular/common/http";

@Component({
    selector: "icon-picker-dialog",
    templateUrl: "js/common/icon-picker-dialog/icon-picker-dialog.html"
})
export class IconPickerDialog implements ng.IComponentController{

    fillStyle: any;
    selectedIconTile: any;
    iconTiles: any;

    iconModel: any;
    selectedColorTile: any;
    colorTiles: any;
    loadingIcons: any;
    loadingColors: any;

    ngOnInit() {

        this.fillStyle = {'fill':'#90A4AE'};
        this.selectedIconTile = {title: ""};
        this.iconTiles = [];
        this.iconModel = this.data.iconModel;

        this.selectedColorTile = null;
        this.colorTiles = [];
        this.loadingIcons = false;
        this.loadingColors = false;

        this.fetchIcons();
        this.fetchColors();

    }
    constructor(private http: HttpClient,
                private dialogRef: MatDialogRef<IconPickerDialog>,
                private RestUrlService: RestUrlService,
                @Inject(MAT_DIALOG_DATA) private data: any){}

    selectIcon (tile: any) {
        this.selectedIconTile = tile;
    };

    selectColor (tile: any) {
        this.selectedColorTile = tile;
        this.fillStyle = {'fill': tile.background };
    };
    
    getBackgroundStyle (tile: any) {
        return {'background-color': tile.background };
    };
    
    save () {
        var data = {icon:this.selectedIconTile.title, color:this.selectedColorTile.background};
        this.dialogRef.close(data);
    };

    hide () {
        this.dialogRef.close();
    };

    cancel () {
        this.dialogRef.close();
    };

    fetchIcons ( ) {
        this.loadingIcons = true;
        this.http.get(this.RestUrlService.ICONS_URL).toPromise().then((response: any) =>{

            var icons = response;
            _.forEach(icons, (icon: any) =>{
                var tile = {title: icon};
                this.iconTiles.push(tile);
                if (this.iconModel.icon !== null && this.iconModel.icon === icon) {
                    this.selectedIconTile = tile;
                }
            });
            this.loadingIcons = false;
        });
    }

    fetchColors () {
        this.loadingColors = true;
        this.http.get(this.RestUrlService.ICON_COLORS_URL).toPromise().then( (response: any) =>{
            var colors = response;
            _.forEach(colors, (color: any)=>{

                var tile = {title: color.name, background: color.color};
                this.colorTiles.push(tile);
                if (this.iconModel.iconColor !== null && this.iconModel.iconColor === color.color) {
                    this.selectedColorTile = tile;
                }
            });

            if (this.selectedColorTile === null) {
                this.selectedColorTile = _.find(this.colorTiles, (c: any)=> {
                    return c.title === 'Teal';
                })
            }
            this.loadingColors = false;
        });
    }
}
