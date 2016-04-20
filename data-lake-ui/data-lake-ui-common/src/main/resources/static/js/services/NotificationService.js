angular.module(COMMON_APP_MODULE_NAME).service('NotificationService', function ($timeout,$mdToast) {

    var self = this;
    this.alerts = {};

    this.addAlert = function(errorType,message, detailMsg,type, timeout, groupKey) {
        var id = IDGenerator.generateId("alert");
        var alert = {type:type,msg:message,id:id,detailMsg:detailMsg, errorType:errorType, hasDetail:false};
        if(detailMsg != undefined && detailMsg != ""){
            alert.hasDetail = true;
        }
        this.alerts[id] = alert;
     /*   if(timeout){
            $timeout(function(){
                self.removeAlert(id)
            },timeout);
        }*/
        if(groupKey){
            alert.groupKey = groupKey;a
        }
        self.toastAlert(alert,timeout);
        return alert;
    }

    this.toastAlert = function(alert,timeout) {

        console.log('TOASTING ',alert,$mdToast)
        var options = {hideDelay:false, msg:alert.msg}
        if(timeout){
            options.hideDelay = timeout;
        }
        if(alert.hasDetail){
            options.msg += " "+alert.detailMsg;
        }

        var alertId = alert.id;
        var toast = $mdToast.simple()
            .textContent(options.msg)
            .action('Ok')
            .highlightAction(true)
            .hideDelay(options.hideDelay)
           // .position(pinTo);
        $mdToast.show(toast).then(function(response) {
            if ( response == 'ok' ) {
                $mdToast.hide();
                self.removeAlert(alertId)
            }
        });

        if(timeout){
            $timeout(function(){
                self.removeAlert(id)
            },timeout);
        }


    }

    this.getAlertWithGroupKey = function(groupKey){
        var returnedAlert = null;
        angular.forEach(this.alerts,function(alert,id){
            if(returnedAlert == null && alert.groupKey && alert.groupKey == groupKey){
                returnedAlert = alert;
            }
        });
        return returnedAlert;
    }

    this.success = function(message, timeout) {
        return this.addAlert("Success",message,undefined,"success", timeout);
    }
    this.error = function(message, timeout) {
      //  console.error("ERROR ",message)
        return this.addAlert("Error",message,undefined,"danger", timeout);
    }
    this.errorWithErrorType = function(errorType,message, timeout) {
       // console.error("ERROR ",message)
        return this.addAlert(errorType,message,undefined,"danger", timeout);
    }

    this.errorWithDetail = function(errorType,message,detailMsg, timeout) {
      //  console.error("ERROR ",message, detailMsg)
        return this.addAlert(errorType,message,undefined,"danger",detailMsg, timeout);
    }

    this.errorWithGroupKey = function(errorType,message,groupKey,detailMsg) {
     //   console.error("ERROR ",message, detailMsg)
        //Only add the error if it doesnt already exist
        if(groupKey != undefined) {
            if (this.getAlertWithGroupKey(groupKey) == null) {
                return this.addAlert(errorType,message, detailMsg,"danger",undefined, groupKey);
            }
        }
        else {
            this.error(message,undefined);
        }
    }
    this.removeAlert = function(id){
        delete this.alerts[id];
    }

    this.getAlerts = function(){
        return this.alerts;
    }



});