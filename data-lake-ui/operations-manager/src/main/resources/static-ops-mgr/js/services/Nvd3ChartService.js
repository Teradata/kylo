/*
 * Copyright (c) 2015.
 */

/**
 *
 */
angular.module(MODULE_OPERATIONS).service('Nvd3ChartService', function ($timeout) {

    var self = this;
    this.renderEndUpdated = {};
    this.timeoutMap = {};

    this.expireRenderEnd = function(chart){
        delete self.renderEndUpdated[chart];
    }

    this.shouldManualUpdate = function(chart){
        if(self.renderEndUpdated[chart] == undefined){
            self.renderEndUpdated[chart] = chart;
            if(self.timeoutMap[chart] != undefined){
                $timeout.cancel(self.timeoutMap[chart]);
            }
            self.timeoutMap[chart] = $timeout(function(){
                self.expireRenderEnd(chart) ;
            },3000);
            return true;
        }
        else {
            return false;
        }
    }


});