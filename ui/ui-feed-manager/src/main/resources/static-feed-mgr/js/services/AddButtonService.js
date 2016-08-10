

angular.module(MODULE_FEED_MGR).service('AddButtonService', function (BroadcastService) {
    var self = this;

    function AddButtonServiceTag() {
    }

    this.__tag = new AddButtonServiceTag();


    this.addButtons = {};
    this.NEW_ADD_BUTTON_EVENT = 'newAddButton'
    this.HIDE_ADD_BUTTON_EVENT = 'hideAddButton'
    this.SHOW_ADD_BUTTON_EVENT = 'showAddButton'

    this.registerAddButton = function(state, action){
         self.addButtons[state] = action;
        BroadcastService.notify(self.NEW_ADD_BUTTON_EVENT,state);
    }
    this.hideAddButton = function(){
        BroadcastService.notify(self.HIDE_ADD_BUTTON_EVENT);
    }
    this.showAddButton = function(){
        BroadcastService.notify(self.SHOW_ADD_BUTTON_EVENT);
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