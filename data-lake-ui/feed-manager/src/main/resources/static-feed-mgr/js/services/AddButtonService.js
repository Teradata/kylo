

angular.module(MODULE_FEED_MGR).service('AddButtonService', function (BroadcastService) {
    var self = this;

    function AddButtonServiceTag() {
    }

    this.__tag = new AddButtonServiceTag();


    this.addButtons = {};
    this.NEW_ADD_BUTTON_EVENT = 'newAddButton'

    this.registerAddButton = function(state, action){
         self.addButtons[state] = action;
        BroadcastService.notify(self.NEW_ADD_BUTTON_EVENT,state);
    }
    this.isShowAddButton = function(state){
        return self.addButtons[state] != undefined;
    }
    this.unregisterAddButton = function(state){
        self.addButtons[state];
    }
    this.onClick = function(state){

        var action = self.addButtons[state];
        if(action){
            action();
        }
    }


});