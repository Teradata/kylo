(function() {

  var directive = function() {
    return {
      restrict: "EA",
      bindToController: {
        stepIndex: '@'
      },
      require: ['thinkbigVisualQueryTransform', '^thinkbigStepper'],
      scope: {},
      controllerAs: 'vm',
      templateUrl: 'js/visual-query/visual-query-transform.html',
      controller: "VisualQueryTransformController",
      link: function($scope, element, attrs, controllers) {
        var thisController = controllers[0];
        var stepperController = controllers[1];
        //store a reference to the stepper if needed
        thisController.stepperController = stepperController;
      }

    };
  };

  var controller = function($scope, $log, $http, $mdToast, NifiService, VisualQueryService, HiveService, TableDataFunctions,
                            SideNavService, SparkShellService) {

    var self = this;
    //The model passed in from the previous step
    this.model = VisualQueryService.model;
    //Flag to determine if we can move on to the next step
    this.isValid = true;
    //The SQL String from the previous step
    this.sql = this.model.visualQuerySql;
    //The sql model passed over from the previous step
    this.sqlModel = this.model.visualQueryModel;
    //The array of columns with their respective Table, schema and alias passed over from the previous step
    //{column:'name', alias:'alias',tableName:'table name',tableColumn:'alias_name',dataType:'dataType'}
    this.selectedColumnsAndTables = this.model.selectedColumnsAndTables;
    //The query result transformed to the ag-grid model  (@see HiveService.transformToAgGrid
    this.tableData = {columns: [], rows: []};
    //Function History
    this.functionHistory = [];

    //The current formula string
    this.currentFormula = '';

    //flag to indicate if the Hive data is available
    this.hiveDataLoaded = false;
    //flag to indicate codemirror is ready
    this.codemirroLoaded = false;
    //the codemirror editor
    this.codemirrorEditor = null;
    //the tern server reference
    this.ternServer = null;

    //Function Command Holder
    //@see TableDataFunctions.js
    this.functionCommandHolder = null;

    //Flag to show/hide function history panel
    this.isShowFunctionHistory = false;

    //Setup initial grid options
    this.gridOptions = {
      columnDefs: [],
      data: null,
      enableColumnResizing: true,
      enableGridMenu: true,
      flatEntityAccess: true
    };

    // Translates expressions into Spark code
    this.sparkShellService = new SparkShellService(this.sql);

    //Code Mirror options.  Tern Server requires it be in javascript mode
    this.codemirrorOptions = {
      lineWrapping: false,
      indentWithTabs: false,
      smartIndent: false,
      lineNumbers: false,
      matchBrackets: false,
      autofocus: true,
      mode: 'javascript',
      scrollbarStyle: null
    };

    /**
     * Show and hide the Funciton History
     */
    this.toggleFunctionHistory = function() {
      self.isShowFunctionHistory = !self.isShowFunctionHistory;
    };

    //Callback when Codemirror has been loaded (reference is in the html page at:
    // ui-codemirror="{ onLoad : vm.codemirrorLoaded }"
    this.codemirrorLoaded = function(_editor) {
      //assign the editor to a variable on this object for future reference
      self.codemirrorEditor = _editor;
      //Set the width,height of the editor. Code mirror needs an explicit width/height
      _editor.setSize(700, 25);

      //disable users ability to add new lines.  The Formula bar is only 1 line
      _editor.on("beforeChange", function(instance, change) {
        var newtext = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
        change.update(change.from, change.to, [newtext]);
        return true;
      });

      //hide the scrollbar
      _editor.on("change", function(instance, change) {
        //$(".CodeMirror-hscrollbar").css('display', 'none');
      });
      //set the flag to be loaded and then call out to update Autocomplete options
      self.codemirroLoaded = true;
      self.updateCodeMirrorAutoComplete();
    };

    /**
     * Creates a Tern server.
     */
    function createTernServer() {
      $http.get('js/vendor/tern/defs/tableFunctions.json').success(function(code) {
        self.sparkShellService.setFunctionDefs(code);

        self.ternServer = new CodeMirror.TernServer({defs: [code]});
        self.ternServer.server.addDefs(self.sparkShellService.getColumnDefs());

        var _editor = self.codemirrorEditor;
        _editor.setOption("extraKeys", {
          "Ctrl-Space": function(cm) {
            self.ternServer.complete(cm);
          },
          "Ctrl-I": function(cm) {
            self.ternServer.showType(cm);
          },
          "Ctrl-O": function(cm) {
            self.ternServer.showDocs(cm);
          },
          "Alt-.": function(cm) {
            self.ternServer.jumpToDef(cm);
          },
          "Alt-,": function(cm) {
            self.ternServer.jumpBack(cm);
          },
          "Ctrl-Q": function(cm) {
            self.ternServer.rename(cm);
          },
          "Ctrl-.": function(cm) {
            self.ternServer.selectName(cm);
          }
        });
        _editor.on("blur", function() {
          self.ternServer.hideDoc();
        });
        _editor.on("cursorActivity", self.showHint);
        _editor.on("focus", self.showHint);
      });
    }

    /**
     * Setup the CodeMirror and Tern Server autocomplete. This will only execute when both Hive and Code Mirror are fully
     * initialized.
     */
    this.updateCodeMirrorAutoComplete = function() {
      if (self.codemirroLoaded && self.hiveDataLoaded) {
        if (self.ternServer === null) {
          createTernServer();
        } else {
          var defs = self.sparkShellService.getColumnDefs();
          self.ternServer.server.deleteDefs(defs["!name"]);
          self.ternServer.server.addDefs(defs);
        }
      }
    };

    /**
     * Makes an asynchronous request to get the list of completions available at the cursor.
     *
     * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
     * @param {Function} callback the callback function
     */
    this.getHint = function(cm, callback) {
      self.ternServer.getHint(cm, function(data) {
        // Complete function calls so arg hints can be displayed
        CodeMirror.on(data, "pick", function(completion) {
          if (completion.data.type.substr(0, 3) === "fn(") {
            var cursor = cm.getCursor();
            cm.replaceRange("(", cursor, cursor, "complete");
          }
        });

        // Display hints
        callback(data);
      });
    };
    this.getHint.async = true;

    /**
     * Shows either argument hints or identifier hints depending on the context.
     *
     * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
     */
    this.showHint = function(cm) {
      // Show args if in a function
      var cursor = cm.getCursor();
      var token = cm.getTokenAt(cursor);
      var lexer = token.state.lexical;

      if (lexer.info === "call" && token.type !== "variable") {
        self.ternServer.updateArgHints(cm);
      } else {
        self.ternServer.hideDoc();
      }

      // Show completions if available
      if (cursor.ch === 0 || token.type === "variable" || (token.string === "." && lexer.type === "stat")) {
        cm.showHint({
          completeSingle: false,
          hint: self.getHint
        });
      }
    };

    /**
     * Query Hive using the query from the previous step. Set the Grids rows and columns
     */
    this.query = function() {
      //flag to indicate query is running
      this.executingQuery = true;

      return self.sparkShellService.transform().then(function(response) {
        //mark the query as finished
        self.executingQuery = false;
        //transform the result to the agGrid model
        var result = HiveService.transformQueryResultsToUiGridModel({data: response.data.results});
        //store the result for use in the commands
        self.tableData = result;
        //update the ag-grid
        self.gridOptions.columnDefs = result.columns;
        self.gridOptions.data = result.rows;
        //mark the flag to indicate Hive is loaded
        self.hiveDataLoaded = true;
        self.updateCodeMirrorAutoComplete();

        //Initialize the Command function holder
        self.functionCommandHolder = TableDataFunctions.newCommandHolder({rows: result.rows, columns: result.columns});
      });
    };

    function updateGrid(tableData) {
      self.gridOptions.columnDefs = tableData.columns;
      self.gridOptions.data = tableData.rows;
    }

    /**
     * Called when the user clicks Add on the function bar
     */
    this.onAddFunction = function() {
      var tableData = self.functionCommandHolder.executeStr(self.currentFormula);
      if (tableData != null && tableData != undefined) {
        updateGrid(tableData);
        self.functionHistory.push(self.currentFormula);

        self.ternServer.server.addFile("[doc]", self.currentFormula);
        var file = self.ternServer.server.findFile("[doc]");
        self.sparkShellService.push(file.ast);
        self.query();
      }
    };

    //reference to the last command that was undone
    var lastUndo = '';

    this.onUndo = function() {
      var tableData = self.functionCommandHolder.undo();
      updateGrid(tableData);
      lastUndo = self.functionHistory.pop();
    };
    this.onRedo = function() {
      var tableData = self.functionCommandHolder.redo();
      updateGrid(tableData);
      self.functionHistory.push(lastUndo);
    };

    this.canUndo = function() {
      if (self.functionCommandHolder) {
        return self.functionCommandHolder.canUndo();
      } else {
        return false;
      }
    };
    this.canRedo = function() {
      if (self.functionCommandHolder) {
        return self.functionCommandHolder.canRedo();
      } else {
        return false;
      }
    };

    //Hide the left side nav bar
    SideNavService.hideSideNav();

    this.query();

    $scope.$on('$destroy', function() {
      //clean up code here
    });
  };

  angular.module(MODULE_FEED_MGR).controller('VisualQueryTransformController', controller);
  angular.module(MODULE_FEED_MGR)
      .directive('thinkbigVisualQueryTransform', directive);
})();
