// Global accessor.
var flowchart = {};
// Module.
(function () {
    var _this = this;
    //
    // Width of a node.
    flowchart.defaultNodeWidth = 250;
    // Amount of space reserved for displaying the node's name.
    flowchart.nodeNameHeight = 75;
    // Height of a connector in a node.
    flowchart.connectorHeight = 35;
    flowchart.attributeHeight = 25;
    flowchart.CONNECTOR_LOCATION = { TOP: "TOP", BOTTOM: "BOTTOM", LEFT: "LEFT", RIGHT: "RIGHT" };
    flowchart.connectorRadius = 5;
    flowchart.nextConnectorID = 1;
    // Compute the Y coordinate of a connector, given its index.
    /**
     * @Deprecated
     * @param connectorIndex
     * @returns {number}
     */
    flowchart.computeConnectorY = function (connectorIndex) {
        return flowchart.nodeNameHeight + (connectorIndex * flowchart.connectorHeight);
    };
    // Compute the position of a connector in the graph.
    /**
     * @Deprecated
     * @param node
     * @param connectorIndex
     * @param inputConnector
     * @returns {{x: *, y: *}}
     */
    flowchart.computeConnectorPosOld = function (node, connectorIndex, inputConnector) {
        return {
            x: node.x() + (inputConnector ? 0 : node.width ? node.width() : flowchart.defaultNodeWidth),
            y: node.y() + flowchart.computeConnectorY(connectorIndex),
        };
    };
    flowchart.computeConnectorPos = function (node, connector) {
        var x = 0;
        if (connector.location() == flowchart.CONNECTOR_LOCATION.LEFT || connector.location() == flowchart.CONNECTOR_LOCATION.RIGHT) {
            x = node.x() + (connector.location() == flowchart.CONNECTOR_LOCATION.LEFT ? 0 : node.width ? node.width() : flowchart.defaultNodeWidth);
        }
        else {
            //center the top and bottom
            x = node.x() + (node.width ? node.width() : flowchart.defaultNodeWidth) / 2;
        }
        var y = 0;
        if (connector.location() == flowchart.CONNECTOR_LOCATION.LEFT || connector.location() == flowchart.CONNECTOR_LOCATION.RIGHT) {
            y = node.y() + (node.height()) / 2;
        }
        else {
            y = node.y() + (connector.location() == flowchart.CONNECTOR_LOCATION.TOP ? 0 : node.height());
        }
        return {
            x: x,
            y: y
        };
    };
    // View model for a connector.
    flowchart.ConnectorViewModel = function (connectorDataModel, x, y, parentNode) {
        _this.data = connectorDataModel;
        _this._parentNode = parentNode;
        _this._x = x;
        _this._y = y;
        _this.TRIANGLE_SIZE = 30;
        // The name of the connector.
        _this.name = function () {
            return _this.data.name;
        };
        _this.trianglePoints = function () {
            var start = "";
            var end = "";
            var point = "";
            var point2 = "";
            if (_this.location() == flowchart.CONNECTOR_LOCATION.BOTTOM) {
                start = _this.x() - _this.TRIANGLE_SIZE / 2 + "," + _this.y();
                end = _this.x() + _this.TRIANGLE_SIZE / 2 + "," + _this.y();
                point = _this.x() + "," + (_this.y() + _this.TRIANGLE_SIZE);
                //point2 = this.x()+","+(this.y()+this.TRIANGLE_SIZE);
            }
            else if (_this.location() == flowchart.CONNECTOR_LOCATION.TOP) {
                start = _this.x() - _this.TRIANGLE_SIZE / 2 + "," + _this.y();
                end = _this.x() + _this.TRIANGLE_SIZE / 2 + "," + _this.y();
                point = _this.x() + "," + (_this.y() - _this.TRIANGLE_SIZE);
            }
            if (_this.location() == flowchart.CONNECTOR_LOCATION.LEFT) {
                start = _this.x() + "," + (_this.y() - _this.TRIANGLE_SIZE / 2);
                end = _this.x() + "," + (_this.y() + _this.TRIANGLE_SIZE / 2);
                point = (_this.x() - _this.TRIANGLE_SIZE) + "," + _this.y();
            }
            else if (_this.location() == flowchart.CONNECTOR_LOCATION.RIGHT) {
                start = _this.x() + "," + (_this.y() - _this.TRIANGLE_SIZE / 2);
                end = _this.x() + "," + (_this.y() + _this.TRIANGLE_SIZE / 2);
                point = (_this.x() + _this.TRIANGLE_SIZE) + "," + _this.y();
            }
            return start + " " + end + " " + point;
        };
        _this.location = function () {
            return _this.data.location;
        };
        _this.id = function () {
            return _this.data.id;
        };
        // X coordinate of the connector.
        _this.x = function () {
            return _this._x;
        };
        //
        // Y coordinate of the connector.
        //
        _this.y = function () {
            return _this._y;
        };
        //
        // The parent node that the connector is attached to.
        //
        _this.parentNode = function () {
            return _this._parentNode;
        };
    };
    // Create view model for a list of data models.
    var createConnectorsViewModel = function (connectorDataModels, x, parentNode) {
        var viewModels = [];
        if (connectorDataModels) {
            for (var i = 0; i < connectorDataModels.length; ++i) {
                var connectorViewModel = new flowchart.ConnectorViewModel(connectorDataModels[i], x, flowchart.computeConnectorY(i), parentNode);
                viewModels.push(connectorViewModel);
            }
        }
        return viewModels;
    };
    var createConnectorViewModel = function (connectorDataModel, x, y, parentNode) {
        var connectorViewModel = null;
        if (connectorDataModel) {
            connectorViewModel =
                new flowchart.ConnectorViewModel(connectorDataModel, x, y, parentNode);
        }
        return connectorViewModel;
    };
    //
    // View model for a node.
    //
    flowchart.NodeViewModel = function (nodeDataModel) {
        _this.data = nodeDataModel;
        // set the default width value of the node
        if (!_this.data.width || _this.data.width < 0) {
            _this.data.width = flowchart.defaultNodeWidth;
        }
        _this.connectors = [];
        _this.leftConnectors = [];
        _this.rightConnectors = [];
        _this.bottomConnectors = [];
        _this.topConnectors = [];
        function createConnectors() {
            var connectors = this.data.connectors;
            var leftConnector = connectors.left;
            if (leftConnector) {
                leftConnector.location = flowchart.CONNECTOR_LOCATION.LEFT;
                leftConnector.id = flowchart.nextConnectorID++;
                var connectorViewModel = createConnectorViewModel(leftConnector, 0, this.height() / 2, this);
                this.leftConnectors.push(connectorViewModel);
                this.connectors.push(connectorViewModel);
            }
            var rightConnector = connectors.right;
            if (rightConnector) {
                rightConnector.location = flowchart.CONNECTOR_LOCATION.RIGHT;
                rightConnector.id = flowchart.nextConnectorID++;
                var connectorViewModel = createConnectorViewModel(rightConnector, this.data.width, this.height() / 2, this);
                this.rightConnectors.push(connectorViewModel);
                this.connectors.push(connectorViewModel);
            }
            var topConnector = connectors.top;
            if (topConnector) {
                topConnector.location = flowchart.CONNECTOR_LOCATION.TOP;
                topConnector.id = flowchart.nextConnectorID++;
                var connectorViewModel = this.topConnector = createConnectorViewModel(topConnector, this.data.width / 2, 0, this);
                this.topConnectors.push(connectorViewModel);
                this.connectors.push(connectorViewModel);
            }
            var bottomConnector = connectors.bottom;
            if (bottomConnector) {
                bottomConnector.location = flowchart.CONNECTOR_LOCATION.BOTTOM;
                bottomConnector.id = flowchart.nextConnectorID++;
                var connectorViewModel = createConnectorViewModel(bottomConnector, this.data.width / 2, this.height(), this);
                this.bottomConnectors.push(connectorViewModel);
                this.connectors.push(connectorViewModel);
            }
        }
        // Set to true when the node is selected.
        _this._selected = false;
        // Name of the node.
        _this.name = function () {
            return this.data.name || "";
        };
        // X coordinate of the node.
        _this.x = function () {
            return _this.data.x;
        };
        // Y coordinate of the node.
        _this.y = function () {
            return _this.data.y;
        };
        //
        // Width of the node.
        //
        _this.width = function () {
            return _this.data.width;
        };
        //
        // Height of the node.
        //
        _this.height = function () {
            if (_this.data.height) {
                return _this.data.height;
            }
            else if (_this.data.nodeAttributes && _this.data.nodeAttributes.attributes) {
                return 95 + flowchart.attributeHeight * _this.data.nodeAttributes.attributes.length;
            }
            else {
                var numConnectors = Math.max(_this.inputConnectors.length, _this.outputConnectors.length);
                return flowchart.computeConnectorY(numConnectors);
            }
        };
        // Select the node.
        _this.select = function () {
            _this._selected = true;
        };
        // Deselect the node.
        _this.deselect = function () {
            _this._selected = false;
        };
        //
        // Toggle the selection state of the node.
        //
        _this.toggleSelected = function () {
            _this._selected = !_this._selected;
        };
        //
        // Returns true if the node is selected.
        //
        _this.selected = function () {
            return _this._selected;
        };
        //
        // Internal function to add a connector.
        _this._addConnector = function (connectorDataModel, x, connectorsDataModel, connectorsViewModel) {
            var connectorViewModel = new flowchart.ConnectorViewModel(connectorDataModel, x, flowchart.computeConnectorY(connectorsViewModel.length), _this);
            connectorsDataModel.push(connectorDataModel);
            // Add to node's view model.
            connectorsViewModel.push(connectorViewModel);
        };
        //
        // Add an input connector to the node.
        //TODO change to AddTop, addLeft, addRight,..etc
        //
        _this.addInputConnector = function (connectorDataModel) {
            if (!_this.data.inputConnectors) {
                _this.data.inputConnectors = [];
            }
            _this._addConnector(connectorDataModel, 0, _this.data.inputConnectors, _this.inputConnectors);
        };
        //
        // Add an ouput connector to the node.
        //
        _this.addOutputConnector = function (connectorDataModel) {
            if (!_this.data.outputConnectors) {
                _this.data.outputConnectors = [];
            }
            _this._addConnector(connectorDataModel, _this.data.width, _this.data.outputConnectors, _this.outputConnectors);
        };
        createConnectors();
        _this.getAllConnectors = function () {
            return _this.connectors;
        };
    };
    // 
    // Wrap the nodes data-model in a view-model.
    //
    var createNodesViewModel = function (nodesDataModel) {
        var nodesViewModel = [];
        if (nodesDataModel) {
            for (var i = 0; i < nodesDataModel.length; ++i) {
                nodesViewModel.push(new flowchart.NodeViewModel(nodesDataModel[i]));
            }
        }
        return nodesViewModel;
    };
    //
    // View model for a connection.
    //
    flowchart.ConnectionViewModel = function (connectionDataModel, sourceConnector, destConnector) {
        _this.data = connectionDataModel;
        _this.source = sourceConnector;
        _this.dest = destConnector;
        // Set to true when the connection is selected.
        _this._selected = false;
        _this.name = function () {
            var name = _this.data.name || "";
            return name;
        };
        _this.sourceCoordX = function () {
            return _this.source.parentNode().x() + _this.source.x();
        };
        _this.sourceCoordY = function () {
            return _this.source.parentNode().y() + _this.source.y();
        };
        _this.edit = function () {
            _this.data.edit(_this);
        };
        _this.sourceCoord = function () {
            return {
                x: _this.sourceCoordX(),
                y: _this.sourceCoordY()
            };
        };
        _this.sourceTangentX = function () {
            return flowchart.computeConnectionSourceTangentX(_this.sourceCoord(), _this.destCoord());
        };
        _this.sourceTangentY = function () {
            return flowchart.computeConnectionSourceTangentY(_this.sourceCoord(), _this.destCoord());
        };
        _this.destCoordX = function () {
            return _this.dest.parentNode().x() + _this.dest.x();
        };
        _this.destCoordY = function () {
            return _this.dest.parentNode().y() + _this.dest.y();
        };
        _this.destCoord = function () {
            return {
                x: _this.destCoordX(),
                y: _this.destCoordY()
            };
        };
        _this.destTangentX = function () {
            return flowchart.computeConnectionDestTangentX(_this.sourceCoord(), _this.destCoord());
        };
        _this.destTangentY = function () {
            return flowchart.computeConnectionDestTangentY(_this.sourceCoord(), _this.destCoord());
        };
        _this.middleX = function (scale) {
            if (typeof (scale) == "undefined")
                scale = 0.5;
            return _this.sourceCoordX() * (1 - scale) + _this.destCoordX() * scale;
        };
        _this.middleY = function (scale) {
            if (typeof (scale) == "undefined")
                scale = 0.5;
            return _this.sourceCoordY() * (1 - scale) + _this.destCoordY() * scale;
        };
        //
        // Select the connection.
        //
        _this.select = function () {
            _this._selected = true;
        };
        //
        // Deselect the connection.
        //
        _this.deselect = function () {
            _this._selected = false;
        };
        //
        // Toggle the selection state of the connection.
        //
        _this.toggleSelected = function () {
            _this._selected = !_this._selected;
        };
        //
        // Returns true if the connection is selected.
        //
        _this.selected = function () {
            return _this._selected;
        };
    };
    //
    // Helper function.
    //
    var computeConnectionTangentOffset = function (pt1, pt2) {
        return (pt2.x - pt1.x) / 2;
    };
    //
    // Compute the tangent for the bezier curve.
    //
    flowchart.computeConnectionSourceTangentX = function (pt1, pt2) {
        return pt1.x + computeConnectionTangentOffset(pt1, pt2);
    };
    //
    // Compute the tangent for the bezier curve.
    //
    flowchart.computeConnectionSourceTangentY = function (pt1, pt2) {
        return pt1.y;
    };
    //
    // Compute the tangent for the bezier curve.
    //
    flowchart.computeConnectionSourceTangent = function (pt1, pt2) {
        return {
            x: flowchart.computeConnectionSourceTangentX(pt1, pt2),
            y: flowchart.computeConnectionSourceTangentY(pt1, pt2),
        };
    };
    //
    // Compute the tangent for the bezier curve.
    //
    flowchart.computeConnectionDestTangentX = function (pt1, pt2) {
        return pt2.x - computeConnectionTangentOffset(pt1, pt2);
    };
    //
    // Compute the tangent for the bezier curve.
    //
    flowchart.computeConnectionDestTangentY = function (pt1, pt2) {
        return pt2.y;
    };
    //
    // Compute the tangent for the bezier curve.
    //
    flowchart.computeConnectionDestTangent = function (pt1, pt2) {
        return {
            x: flowchart.computeConnectionDestTangentX(pt1, pt2),
            y: flowchart.computeConnectionDestTangentY(pt1, pt2),
        };
    };
    /**
     * View model for the chart.
     *
     * @public
     * @constructor
     * @param {Object} chartDataModel the data model for the chart
     * @param {function} [onCreateConnectionCallback] the callback to create a function
     * @param {function} [onEditConnectionCallback] the callback to edit a function
     * @param {function} [onDeleteSelectedCallback] the callback when the current selection is deleted
     */
    flowchart.ChartViewModel = function (chartDataModel, onCreateConnectionCallback, onEditConnectionCallback, onDeleteSelectedCallback) {
        //
        // Find a specific node within the chart.
        //
        _this.findNode = function (nodeID) {
            for (var i = 0; i < _this.nodes.length; ++i) {
                var node = _this.nodes[i];
                if (node.data.id == nodeID) {
                    return node;
                }
            }
            throw new Error("Failed to find node " + nodeID);
        };
        _this.findConnector = function (nodeID, connectorIndex) {
            var node = _this.findNode(nodeID);
            if (!node.connectors || node.connectors.length <= connectorIndex) {
                throw new Error("Node " + nodeID + " has invalid input connectors.");
            }
            return node.connectors[connectorIndex];
        };
        //
        // Find a specific input connector within the chart.
        //
        _this.findInputConnector = function (nodeID, connectorIndex) {
            var node = _this.findNode(nodeID);
            if (!node.inputConnectors || node.inputConnectors.length <= connectorIndex) {
                throw new Error("Node " + nodeID + " has invalid input connectors.");
            }
            return node.inputConnectors[connectorIndex];
        };
        //
        // Find a specific output connector within the chart.
        //
        _this.findOutputConnector = function (nodeID, connectorIndex) {
            var node = _this.findNode(nodeID);
            if (!node.outputConnectors || node.outputConnectors.length <= connectorIndex) {
                throw new Error("Node " + nodeID + " has invalid output connectors.");
            }
            return node.outputConnectors[connectorIndex];
        };
        //
        // Create a view model for connection from the data model.
        //
        _this._createConnectionViewModel = function (connectionDataModel) {
            // Create connection view
            var sourceConnector = _this.findConnector(connectionDataModel.source.nodeID, connectionDataModel.source.connectorIndex);
            var destConnector = _this.findConnector(connectionDataModel.dest.nodeID, connectionDataModel.dest.connectorIndex);
            var connectionViewModel = new flowchart.ConnectionViewModel(connectionDataModel, sourceConnector, destConnector);
            // Set callback function for editing the connection
            var source = _this.findNode(connectionDataModel.source.nodeID);
            var dest = _this.findNode(connectionDataModel.dest.nodeID);
            connectionDataModel.edit = function () {
                if (onEditConnectionCallback) {
                    onEditConnectionCallback(connectionViewModel, connectionDataModel, source, dest);
                }
            };
            // Return connection view
            return connectionViewModel;
        };
        // 
        // Wrap the connections data-model in a view-model.
        //
        _this._createConnectionsViewModel = function (connectionsDataModel) {
            var connectionsViewModel = [];
            if (connectionsDataModel) {
                for (var i = 0; i < connectionsDataModel.length; ++i) {
                    connectionsViewModel.push(_this._createConnectionViewModel(connectionsDataModel[i]));
                }
            }
            return connectionsViewModel;
        };
        // Reference to the underlying data.
        _this.data = chartDataModel;
        // Create a view-model for nodes.
        _this.nodes = createNodesViewModel(_this.data.nodes);
        // Create a view-model for connections.
        _this.connections = _this._createConnectionsViewModel(_this.data.connections);
        //
        // Create a view model for a new connection.
        //
        _this.createNewConnection = function (startConnector, endConnector, onCreateConnectionCallback, onEditConnectionCallback) {
            var connectionsDataModel = _this.data.connections;
            if (!connectionsDataModel) {
                connectionsDataModel = _this.data.connections = [];
            }
            var connectionsViewModel = _this.connections;
            if (!connectionsViewModel) {
                connectionsViewModel = _this.connections = [];
            }
            var startNode = startConnector.parentNode();
            var startNodeConnectors = startNode.getAllConnectors();
            var startConnectorIndex = startNodeConnectors.indexOf(startConnector);
            var startConnectorType = 'input';
            if (startConnectorIndex == -1) {
                throw new Error("Failed to find source connector within either inputConnectors or outputConnectors of source node.");
            }
            var endNode = endConnector.parentNode();
            var endNodeConnectors = endNode.getAllConnectors();
            var endConnectorIndex = endNodeConnectors.indexOf(endConnector);
            var endConnectorType = 'input';
            if (endConnectorIndex == -1) {
                throw new Error("Failed to find dest connector within inputConnectors or outputConnectors of dest node.");
            }
            if (startConnectorType == endConnectorType) {
                //	throw new Error("Failed to create connection. Only output to input connections are allowed.")
            }
            if (startNode == endNode) {
                throw new Error("Failed to create connection. Cannot link a node with itthis.");
            }
            var startNode = {
                nodeID: startNode.data.id,
                connectorIndex: startConnectorIndex,
                connectorID: startConnector.data.id
            };
            var endNode = {
                nodeID: endNode.data.id,
                connectorIndex: endConnectorIndex,
                connectorId: endConnector.data.id
            };
            var connectionDataModel = {
                source: startConnectorType == 'output' ? startNode : endNode,
                dest: startConnectorType == 'output' ? endNode : startNode
            };
            connectionsDataModel.push(connectionDataModel);
            var outputConnector = startConnectorType == 'output' ? startConnector : endConnector;
            var inputConnector = startConnectorType == 'output' ? endConnector : startConnector;
            var src = _this.findNode(connectionDataModel.source.nodeID);
            var dest = _this.findNode(connectionDataModel.dest.nodeID);
            //connectionDataModel.name = src.name()+" - "+dest.name();
            connectionDataModel.joinKeys = {};
            connectionDataModel.edit = function (viewModel) {
                if (onEditConnectionCallback) {
                    onEditConnectionCallback()(connectionViewModel, connectionDataModel, src, dest);
                }
            };
            var connectionViewModel = new flowchart.ConnectionViewModel(connectionDataModel, outputConnector, inputConnector);
            connectionsViewModel.push(connectionViewModel);
            if (onCreateConnectionCallback) {
                onCreateConnectionCallback()(connectionViewModel, connectionDataModel, src, dest, inputConnector, outputConnector);
            }
        };
        //
        // Add a node to the view model.
        //
        _this.addNode = function (nodeDataModel) {
            if (!_this.data.nodes) {
                _this.data.nodes = [];
            }
            // 
            // Update the data model.
            //
            _this.data.nodes.push(nodeDataModel);
            // 
            // Update the view model.
            //
            _this.nodes.push(new flowchart.NodeViewModel(nodeDataModel));
        };
        //
        // Select all nodes and connections in the chart.
        //
        _this.selectAll = function () {
            var nodes = _this.nodes;
            for (var i = 0; i < nodes.length; ++i) {
                var node = nodes[i];
                node.select();
            }
            var connections = _this.connections;
            for (var i = 0; i < connections.length; ++i) {
                var connection = connections[i];
                connection.select();
            }
        };
        //
        // Deselect all nodes and connections in the chart.
        //
        _this.deselectAll = function () {
            var nodes = _this.nodes;
            for (var i = 0; i < nodes.length; ++i) {
                var node = nodes[i];
                node.deselect();
            }
            var connections = _this.connections;
            for (var i = 0; i < connections.length; ++i) {
                var connection = connections[i];
                connection.deselect();
            }
        };
        //
        // Update the location of the node and its connectors.
        //
        _this.updateSelectedNodesLocation = function (deltaX, deltaY) {
            var selectedNodes = _this.getSelectedNodes();
            for (var i = 0; i < selectedNodes.length; ++i) {
                var node = selectedNodes[i];
                node.data.x += deltaX;
                node.data.y += deltaY;
            }
        };
        // Handle mouse click on a particular node.
        //
        _this.handleNodeClicked = function (node, ctrlKey) {
            if (ctrlKey) {
                node.toggleSelected();
            }
            else {
                _this.deselectAll();
                node.select();
            }
            // Move node to the end of the list so it is rendered after all the other.
            // This is the way Z-order is done in SVG.
            var nodeIndex = _this.nodes.indexOf(node);
            if (nodeIndex == -1) {
                throw new Error("Failed to find node in view model!");
            }
            _this.nodes.splice(nodeIndex, 1);
            _this.nodes.push(node);
        };
        //
        // Handle mouse down on a connection.
        //
        _this.handleConnectionMouseDown = function (connection, ctrlKey) {
            if (ctrlKey) {
                connection.toggleSelected();
            }
            else {
                _this.deselectAll();
                connection.select();
            }
        };
        //
        // Delete all nodes and connections that are selected.
        //
        _this.deleteSelected = function () {
            var newNodeViewModels = [];
            var newNodeDataModels = [];
            var deletedNodeIds = [];
            // Sort nodes into:
            //		nodes to keep and 
            //		nodes to delete.
            for (var nodeIndex = 0; nodeIndex < _this.nodes.length; ++nodeIndex) {
                var node = _this.nodes[nodeIndex];
                if (!node.selected()) {
                    // Only retain non-selected nodes.
                    newNodeViewModels.push(node);
                    newNodeDataModels.push(node.data);
                }
                else {
                    // Keep track of nodes that were deleted, so their connections can also
                    // be deleted.
                    deletedNodeIds.push(node.data.id);
                }
            }
            var newConnectionViewModels = [];
            var newConnectionDataModels = [];
            //
            // Remove connections that are selected.
            // Also remove connections for nodes that have been deleted.
            //
            for (var connectionIndex = 0; connectionIndex < _this.connections.length; ++connectionIndex) {
                var connection = _this.connections[connectionIndex];
                if (!connection.selected() &&
                    deletedNodeIds.indexOf(connection.data.source.nodeID) === -1 &&
                    deletedNodeIds.indexOf(connection.data.dest.nodeID) === -1) {
                    //
                    // The nodes this connection is attached to, where not deleted,
                    // so keep the connection.
                    //
                    newConnectionViewModels.push(connection);
                    newConnectionDataModels.push(connection.data);
                }
            }
            //
            // Update nodes and connections.
            //
            _this.nodes = newNodeViewModels;
            _this.data.nodes = newNodeDataModels;
            _this.connections = newConnectionViewModels;
            _this.data.connections = newConnectionDataModels;
            if (onDeleteSelectedCallback) {
                onDeleteSelectedCallback();
            }
        };
        //
        // Select nodes and connections that fall within the selection rect.
        //
        _this.applySelectionRect = function (selectionRect) {
            _this.deselectAll();
            for (var i = 0; i < _this.nodes.length; ++i) {
                var node = _this.nodes[i];
                if (node.x() >= selectionRect.x &&
                    node.y() >= selectionRect.y &&
                    node.x() + node.width() <= selectionRect.x + selectionRect.width &&
                    node.y() + node.height() <= selectionRect.y + selectionRect.height) {
                    // Select nodes that are within the selection rect.
                    node.select();
                }
            }
            for (var i = 0; i < _this.connections.length; ++i) {
                var connection = _this.connections[i];
                if (connection.source.parentNode().selected() &&
                    connection.dest.parentNode().selected()) {
                    // Select the connection if both its parent nodes are selected.
                    connection.select();
                }
            }
        };
        // Get the array of nodes that are currently selected.
        _this.getSelectedNodes = function () {
            var selectedNodes = [];
            for (var i = 0; i < _this.nodes.length; ++i) {
                var node = _this.nodes[i];
                if (node.selected()) {
                    selectedNodes.push(node);
                }
            }
            return selectedNodes;
        };
        //
        // Get the array of connections that are currently selected.
        //
        _this.getSelectedConnections = function () {
            var selectedConnections = [];
            for (var i = 0; i < _this.connections.length; ++i) {
                var connection = _this.connections[i];
                if (connection.selected()) {
                    selectedConnections.push(connection);
                }
            }
            return selectedConnections;
        };
    };
})();
//# sourceMappingURL=flowchart_viewmodel.js.map