define(['angular',"feed-mgr/visual-query/module-name"], function (angular,moduleName) {

    angular.module(moduleName).factory('TableDataFunctions', function () {

        var Command = function (execute, undo, rows, columns) {
            this.execute = execute;
            this.undo = undo;
            this.value = value;
        }

        /**
         * Generic Table Command with helper function to get the Column by index or name;
         * @param execute
         * @param undo
         * @constructor
         */
        var TableCommand = function (execute, undo) {
            this.execute = execute;
            this.undo = undo;

            TableCommand.prototype.getField = function (columns, col) {
                if (typeof col == 'number') {
                    return columns[col - 1].field;
                }
                else {
                    return col;
                }
            }
        }

        /**
         * General Add Column Command that has a generic Undo command to revert
         * @param execute
         * @constructor
         */
        var AddColumnTableCommand = function (execute) {
            this.execute = execute;
            //generic undo for add column will just delete the column from the array and header
            this.undo = function (tableData) {
                var self = this;
                var data = {};
                //delete the  row
                data.rows = ArrayUtils.deleteColumn(tableData.rows, this.columnName);
                //delete the column
                data.columns = _.reject(tableData.columns, function (col) {
                    return col.field == self.columnName;
                });
                return data;
            };

            AddColumnTableCommand.prototype.getField = function (columns, col) {
                if (typeof col == 'number') {
                    return columns[col - 1].field;
                }
                else {
                    return col;
                }
            }

        }

        var concatColumnsCommand = function (column1, column2, newColumnName) {
            var command = new AddColumnTableCommand(function (tableData) {
                var data = {};
                data.columns = tableData.columns;
                //GET THE COLUMNS BY NUMBER IF NUMER IS PASSED IN
                this.column1 = this.getField(tableData.columns, this.column1);
                this.column2 = this.getField(tableData.columns, this.column2);
                //concat the columns
                data.rows = ArrayUtils.concatColumns(tableData.rows, newColumnName, this.column1, this.column2);
                //add the column
                data.columns.push({field: newColumnName, headerName: newColumnName, width: 100});
                return data;
            });
            command.column1 = column1;
            command.column2 = column2;
            command.columnName = newColumnName;
            return command;
        };

        var addColumnsCommand = function (column1, column2, newColumnName) {
            var command = new AddColumnTableCommand(function (tableData) {
                var data = {};
                data.columns = tableData.columns;
                //GET THE COLUMNS BY NUMBER IF NUMER IS PASSED IN
                this.column1 = this.getField(tableData.columns, this.column1);
                this.column2 = this.getField(tableData.columns, this.column2);
                //concat the columns
                data.rows = ArrayUtils.addColumns(tableData.rows, newColumnName, this.column1, this.column2);
                //add the column
                data.columns.push({field: newColumnName, headerName: newColumnName, width: 100});
                return data;
            });
            command.column1 = column1;
            command.column2 = column2;
            command.columnName = newColumnName;
            return command;
        };

        var subtractColumnsCommand = function (column1, column2, newColumnName) {
            var command = new AddColumnTableCommand(function (tableData) {
                var data = {};
                data.columns = tableData.columns;
                //GET THE COLUMNS BY NUMBER IF NUMER IS PASSED IN
                this.column1 = this.getField(tableData.columns, this.column1);
                this.column2 = this.getField(tableData.columns, this.column2);
                //concat the columns
                data.rows = ArrayUtils.subtractColumns(tableData.rows, newColumnName, this.column1, this.column2);
                //add the column
                data.columns.push({field: newColumnName, headerName: newColumnName, width: 100});
                return data;
            });
            command.column1 = column1;
            command.column2 = column2;
            command.columnName = newColumnName;
            return command;
        };

        var ExpressionFactory = function () {

            ExpressionFactory.prototype.parseColumns = function (s) {
                var str = s.substr(s.indexOf('Columns.'));
                var arr = str.split('Columns.');
                arr = _.reject(arr, function (item) {
                    return item == '';
                })
                var cols = _.map(arr, function (item) {
                    var _item = item
                    if (_item.indexOf(',') >= 0) {
                        _item = _item.substring(0, _item.indexOf(','));
                    }
                    if (_item.indexOf(')') >= 0) {
                        _item = _item.substring(0, _item.indexOf(')'));
                    }
                    return _item;
                })
                return cols;
            }

            ExpressionFactory.prototype.parseColumnLabel = function (str) {
                if (str.indexOf('as:')) {
                    var str = str.substring(str.indexOf('as:'));
                    str = str.replace("'", "");
                    str = str.replace('"', "");
                    return str.trim();
                }
                return null;
            }
            ExpressionFactory.prototype.newCommand = function (str) {
                var cols = this.parseColumns(str);
                var title = 'new col ' + new Date().getTime();
                var command = null;
                if (cols.length == 2) {
                    if (str.indexOf("add(")) {
                        command = new addColumnsCommand(cols[0], cols[1], title)
                    }
                    else if (str.indexOf("subtract(")) {
                        command = new subtractColumnsCommand(cols[0], cols[1], title)
                    }
                    else if (str.indexOf("concat(")) {
                        command = new concatColumnsCommand(cols[0], cols[1], title)
                    }
                    if (command != null) {
                        return command;
                    }
                }
                else {
                    //     console.log('CANT FIND COLUMNS',str)
                }
            }

        }

        var CommandHolder = function (data) {
            var tableData = data;
            var commands = [];
            var undoCommands = [];
            var expressionFactory = new ExpressionFactory();

            function action(command) {
                var name = command.execute.toString().substr(0, command.execute.toString().indexOf("Command"));
                return name.charAt(0).toUpperCase() + name.slice(1);
            }

            return {
                execute: function (command) {
                    tableData = command.execute(tableData);
                    commands.push(command);
                    return tableData;
                },

                executeStr: function (str) {
                    var command = expressionFactory.newCommand(str);
                    if (command != null) {
                        tableData = command.execute(tableData);
                        commands.push(command);
                        //    console.log(action(command) + ": " + tableData);
                    }
                    //LOG THE ERROR!!!
                    return tableData;
                },

                undo: function () {
                    var command = commands.pop();
                    tableData = command.undo(tableData);
                    undoCommands.push(command);
                    //  console.log("Undo " + action(command) + ": " + tableData);
                    return tableData;
                },
                redo: function () {
                    var command = undoCommands.pop();
                    return this.execute(command);
                },
                canUndo: function () {
                    return commands.length > 0;
                },
                canRedo: function () {
                    return undoCommands.length > 0;
                },
                getCurrentValue: function () {
                    return tableData;
                }
            }
        }

        var data = {
            /**
             * Return a new command holder
             * @param tableData  {rows:[],columns:[]}
             * @returns {CommandHolder}
             */
            newCommandHolder: function (tableData) {
                return new CommandHolder(tableData);
            }

        };
        return data;

    });
});