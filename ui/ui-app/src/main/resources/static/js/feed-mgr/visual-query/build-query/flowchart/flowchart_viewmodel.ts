namespace flowchart {

	export class FlowchartModelBase{
	// Width of a node.
	static defaultNodeWidth = 250;
	// Amount of space reserved for displaying the node's name.
	static nodeNameHeight = 75;
	// Height of a connector in a node.
	static connectorHeight = 35;
	static attributeHeight = 25;
	static CONNECTOR_LOCATION = { TOP: "TOP", BOTTOM: "BOTTOM", LEFT: "LEFT", RIGHT: "RIGHT" };
	static connectorRadius = 5;
	static nextConnectorID = 1
	// Compute the Y coordinate of a connector, given its index.
	/**
	 * @Deprecated
	 * @param connectorIndex
	 * @returns {number}
	 */
	static computeConnectorY = (connectorIndex: any) => {
		return FlowchartModelBase.nodeNameHeight + (connectorIndex * FlowchartModelBase.connectorHeight);
	}
	// Compute the position of a connector in the graph.
	/**
	 * @Deprecated
	 * @param node
	 * @param connectorIndex
	 * @param inputConnector
	 * @returns {{x: *, y: *}}
	 */
	static computeConnectorPosOld = (node: any, connectorIndex: any, inputConnector: any) => {
		return {
			x: node.x() + (inputConnector ? 0 : node.width ? node.width() : FlowchartModelBase.defaultNodeWidth),
			y: node.y() + FlowchartModelBase.computeConnectorY(connectorIndex),
		};
	};

	static computeConnectorPos = (node: any, connector: any) => {
		var x: any = 0;
		if (connector.location() == FlowchartModelBase.CONNECTOR_LOCATION.LEFT || connector.location() == FlowchartModelBase.CONNECTOR_LOCATION.RIGHT) {
			x = node.x() + (connector.location() == FlowchartModelBase.CONNECTOR_LOCATION.LEFT ? 0 : node.width ? node.width() : FlowchartModelBase.defaultNodeWidth);
		}
		else {
			//center the top and bottom
			x = node.x() + (node.width ? node.width() : FlowchartModelBase.defaultNodeWidth) / 2;
		}
		var y = 0;
		if (connector.location() == FlowchartModelBase.CONNECTOR_LOCATION.LEFT || connector.location() == FlowchartModelBase.CONNECTOR_LOCATION.RIGHT) {
			y = node.y() + (node.height()) / 2
		}
		else {
			y = node.y() + (connector.location() == FlowchartModelBase.CONNECTOR_LOCATION.TOP ? 0 : node.height())
		}

		return {
			x: x,
			y: y
		};
	};

	// Create view model for a list of data models.
	static createConnectorsViewModel = (connectorDataModels: any, x: any, parentNode: any) => {
		var viewModels: any = [];
		if (connectorDataModels) {
			for (var i = 0; i < connectorDataModels.length; ++i) {
				var connectorViewModel =
					new flowchart.ConnectorViewModel(connectorDataModels[i], x, FlowchartModelBase.computeConnectorY(i), parentNode);
				viewModels.push(connectorViewModel);
			}
		}
		return viewModels;
	};

	static createConnectorViewModel = (connectorDataModel: any, x: any, y: any, parentNode: any) => {
		var connectorViewModel: any = null
		if (connectorDataModel) {
			connectorViewModel =
				new flowchart.ConnectorViewModel(connectorDataModel, x, y, parentNode);
		}
		return connectorViewModel;
	};

	//
	// Helper function.
	//
	static computeConnectionTangentOffset = (pt1: any, pt2: any) => {
		return (pt2.x - pt1.x) / 2;
	}

	//
	// Compute the tangent for the bezier curve.
	//
	static computeConnectionSourceTangentX = (pt1: any, pt2: any) => {
		return pt1.x + FlowchartModelBase.computeConnectionTangentOffset(pt1, pt2);
	};

	//
	// Compute the tangent for the bezier curve.
	//
	static computeConnectionSourceTangentY = (pt1: any, pt2: any) => {
		return pt1.y;
	};

	//
	// Compute the tangent for the bezier curve.
	//
	static computeConnectionSourceTangent = (pt1: any, pt2: any) => {
		return {
			x: FlowchartModelBase.computeConnectionSourceTangentX(pt1, pt2),
			y: FlowchartModelBase.computeConnectionSourceTangentY(pt1, pt2),
		};
	};

	//
	// Compute the tangent for the bezier curve.
	//
	static computeConnectionDestTangentX = (pt1: any, pt2: any) => {

		return pt2.x - FlowchartModelBase.computeConnectionTangentOffset(pt1, pt2);
	};

	//
	// Compute the tangent for the bezier curve.
	//
	static computeConnectionDestTangentY = (pt1: any, pt2: any) => {

		return pt2.y;
	};

	//
	// Compute the tangent for the bezier curve.
	//
	static computeConnectionDestTangent = (pt1: any, pt2: any) => {
		return {
			x: FlowchartModelBase.computeConnectionDestTangentX(pt1, pt2),
			y: FlowchartModelBase.computeConnectionDestTangentY(pt1, pt2),
		};
	};
	//
	// Wrap the nodes data-model in a view-model.
	//
	static createNodesViewModel = (nodesDataModel: any) => {
		var nodesViewModel: any = [];
		if (nodesDataModel) {
			for (var i = 0; i < nodesDataModel.length; ++i) {
				nodesViewModel.push(new flowchart.NodeViewModel(nodesDataModel[i]));
			}
		}
		return nodesViewModel;
	};
	}

	// View model for a connector.
	export class ConnectorViewModel extends FlowchartModelBase {

		data : any;
		_parentNode : any;
		_x : any;
		_y : any;
		TRIANGLE_SIZE : number = 30;
		constructor(connectorDataModel: any, x: any, y: any, parentNode: any) {
			super();
			this.data = connectorDataModel;
			this._parentNode = parentNode;
			this._x = x;
			this._y = y;
			this.TRIANGLE_SIZE = 30;

		};
		// The name of the connector.
		name = () => {
			return this.data.name;
		}
		trianglePoints = () => {
			var start: any = "";
			var end: any = "";
			var point: any = "";
			var point2: any = "";
			if (this.location() == ConnectorViewModel.CONNECTOR_LOCATION.BOTTOM) {
				start = this.x() - this.TRIANGLE_SIZE / 2 + "," + this.y();
				end = this.x() + this.TRIANGLE_SIZE / 2 + "," + this.y();
				point = this.x() + "," + (this.y() + this.TRIANGLE_SIZE);
				//point2 = this.x()+","+(this.y()+this.TRIANGLE_SIZE);
			}
			else if (this.location() == ConnectorViewModel.CONNECTOR_LOCATION.TOP) {
				start = this.x() - this.TRIANGLE_SIZE / 2 + "," + this.y();
				end = this.x() + this.TRIANGLE_SIZE / 2 + "," + this.y();
				point = this.x() + "," + (this.y() - this.TRIANGLE_SIZE);
			}
			if (this.location() == ConnectorViewModel.CONNECTOR_LOCATION.LEFT) {
				start = this.x() + "," + (this.y() - this.TRIANGLE_SIZE / 2);
				end = this.x() + "," + (this.y() + this.TRIANGLE_SIZE / 2);
				point = (this.x() - this.TRIANGLE_SIZE) + "," + this.y();
			}
			else if (this.location() == ConnectorViewModel.CONNECTOR_LOCATION.RIGHT) {
				start = this.x() + "," + (this.y() - this.TRIANGLE_SIZE / 2);
				end = this.x() + "," + (this.y() + this.TRIANGLE_SIZE / 2);
				point = (this.x() + this.TRIANGLE_SIZE) + "," + this.y();
			}
			return start + " " + end + " " + point;

		}

		location = () => {
			return this.data.location;
		}
		id = () => {
			return this.data.id;
		}
		// X coordinate of the connector.
		x = () => {
			return this._x;
		};

		//
		// Y coordinate of the connector.
		//
		y = () => {
			return this._y;
		};

		//
		// The parent node that the connector is attached to.
		//
		parentNode = () => {
			return this._parentNode;
		};
	};

	//
	// View model for a node.
	//
	export class NodeViewModel extends FlowchartModelBase{

		data : any;
		connectors : any[] = [];
		leftConnectors : any[] = [];
		rightConnectors : any[] = [];
		bottomConnectors : any[] = [];
		topConnectors : any[] = [];
		// Set to true when the node is selected.
		_selected : boolean = false;
		//@Greg please check for these properties as they are never getting assigned anything
		inputConnectors:any;
		outputConnectors:any;

		constructor(nodeDataModel: any) {
			super();
			this.data = nodeDataModel;
			// set the default width value of the node
			if (!this.data.width || this.data.width < 0) {
				this.data.width = NodeViewModel.defaultNodeWidth;
			}
			this.createConnectors();
		};
		createConnectors = () => {
			var connectors: any = this.data.connectors;
			var leftConnector: any = connectors.left;
			if (leftConnector) {
				leftConnector.location = NodeViewModel.CONNECTOR_LOCATION.LEFT;
				leftConnector.id = NodeViewModel.nextConnectorID++;
				var connectorViewModel = NodeViewModel.createConnectorViewModel(leftConnector, 0, this.height() / 2, this);
				this.leftConnectors.push(connectorViewModel);
				this.connectors.push(connectorViewModel);
			}
			var rightConnector = connectors.right;
			if (rightConnector) {
				rightConnector.location = NodeViewModel.CONNECTOR_LOCATION.RIGHT;
				rightConnector.id = NodeViewModel.nextConnectorID++;
				var connectorViewModel = NodeViewModel.createConnectorViewModel(rightConnector, this.data.width, this.height() / 2, this);
				this.rightConnectors.push(connectorViewModel);
				this.connectors.push(connectorViewModel);
			}
			var topConnector = connectors.top;
			if (topConnector) {
				topConnector.location = NodeViewModel.CONNECTOR_LOCATION.TOP;
				topConnector.id = NodeViewModel.nextConnectorID++;
				var connectorViewModel  = NodeViewModel.createConnectorViewModel(topConnector, this.data.width / 2, 0, this);
				this.topConnectors.push(connectorViewModel);
				this.connectors.push(connectorViewModel);
			}
			var bottomConnector = connectors.bottom;
			if (bottomConnector) {
				bottomConnector.location = NodeViewModel.CONNECTOR_LOCATION.BOTTOM;
				bottomConnector.id = NodeViewModel.nextConnectorID++;
				var connectorViewModel = NodeViewModel.createConnectorViewModel(bottomConnector, this.data.width / 2, this.height(), this);
				this.bottomConnectors.push(connectorViewModel);
				this.connectors.push(connectorViewModel);
			}
		}
		// Name of the node.
		name = () => {
			return this.data.name || "";
		};
		// X coordinate of the node.
		x = () => {
			return this.data.x;
		};
		// Y coordinate of the node.
		y = () => {
			return this.data.y;
		};

		//
		// Width of the node.
		//
		width = () => {
			return this.data.width;
		}
		//
		// Height of the node.
		//
		height = () => {
			if (this.data.height) {
				return this.data.height;
			}
			else if (this.data.nodeAttributes && this.data.nodeAttributes.attributes) {
				return 95 + NodeViewModel.attributeHeight * this.data.nodeAttributes.attributes.length;
			}
			else {
				var numConnectors =
					Math.max(
						this.inputConnectors.length,
						this.outputConnectors.length);
				return NodeViewModel.computeConnectorY(numConnectors);
			}
		}
		// Select the node.
		select = () => {
			this._selected = true;
		};

		// Deselect the node.
		deselect = () => {
			this._selected = false;
		};

		//
		// Toggle the selection state of the node.
		//
		toggleSelected = () => {
			this._selected = !this._selected;
		};

		//
		// Returns true if the node is selected.
		//
		selected = () => {
			return this._selected;
		};

		//
		// Internal function to add a connector.
		_addConnector = (connectorDataModel: any, x: any, connectorsDataModel: any, connectorsViewModel: any) => {
			var connectorViewModel: any =
				new flowchart.ConnectorViewModel(connectorDataModel, x,
					NodeViewModel.computeConnectorY(connectorsViewModel.length), this);

			connectorsDataModel.push(connectorDataModel);

			// Add to node's view model.
			connectorsViewModel.push(connectorViewModel);
		}

		//
		// Add an input connector to the node.
		//TODO change to AddTop, addLeft, addRight,..etc
		//
		addInputConnector = (connectorDataModel: any) => {

			if (!this.data.inputConnectors) {
				this.data.inputConnectors = [];
			}
			this._addConnector(connectorDataModel, 0, this.data.inputConnectors, this.inputConnectors);
		};

		//
		// Add an ouput connector to the node.
		//
		addOutputConnector = (connectorDataModel: any) => {
			if (!this.data.outputConnectors) {
				this.data.outputConnectors = [];
			}
			this._addConnector(connectorDataModel, this.data.width, this.data.outputConnectors, this.outputConnectors);
		};
		getAllConnectors = () => {
			return this.connectors;
		}
	}
	//
	// View model for a connection.
	//
	export class ConnectionViewModel extends FlowchartModelBase{

		data : any;
		source : any;
		dest : any;
		_selected : boolean;
		name : any;

		constructor(connectionDataModel: any, sourceConnector: any = undefined, destConnector: any = undefined) {
			super();
			this.data = connectionDataModel;
			this.source = sourceConnector;
			this.dest = destConnector;
			// Set to true when the connection is selected.
			this._selected = false;
			this.name = () => {
				var name: any = this.data.name || "";
				return name;
			}
		};
		sourceCoordX = () => {
			return this.source.parentNode().x() + this.source.x();
		};

		sourceCoordY = () => {
			return this.source.parentNode().y() + this.source.y();
		};

		edit = () => {
			this.data.edit(this);
		}


		sourceCoord = () => {
			return {
				x: this.sourceCoordX(),
				y: this.sourceCoordY()
			};
		}

		sourceTangentX = () => {
			return ConnectionViewModel.computeConnectionSourceTangentX(this.sourceCoord(), this.destCoord());
		};

		sourceTangentY = () => {
			return ConnectionViewModel.computeConnectionSourceTangentY(this.sourceCoord(), this.destCoord());
		};

		destCoordX = () => {
			return this.dest.parentNode().x() + this.dest.x();
		};

		destCoordY = () => {
			return this.dest.parentNode().y() + this.dest.y();
		};

		destCoord = () => {
			return {
				x: this.destCoordX(),
				y: this.destCoordY()
			};
		}

		destTangentX = () => {
			return ConnectionViewModel.computeConnectionDestTangentX(this.sourceCoord(), this.destCoord());
		};

		destTangentY = () => {
			return ConnectionViewModel.computeConnectionDestTangentY(this.sourceCoord(), this.destCoord());
		};

		middleX = (scale: any) => {
			if (typeof (scale) == "undefined")
				scale = 0.5;
			return this.sourceCoordX() * (1 - scale) + this.destCoordX() * scale;
		};

		middleY = (scale: any) => {
			if (typeof (scale) == "undefined")
				scale = 0.5;
			return this.sourceCoordY() * (1 - scale) + this.destCoordY() * scale;
		};


		//
		// Select the connection.
		//
		select = () => {
			this._selected = true;
		};

		//
		// Deselect the connection.
		//
		deselect = () => {
			this._selected = false;
		};

		//
		// Toggle the selection state of the connection.
		//
		toggleSelected = () => {
			this._selected = !this._selected;
		};

		//
		// Returns true if the connection is selected.
		//
		selected = () => {
			return this._selected;
		};

	}
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
	export class ChartViewModel extends FlowchartModelBase{

		data: any;
		nodes: any;
		connections: any;
		constructor(private chartDataModel: any, private onCreateConnectionCallback: any = undefined, private onEditConnectionCallback: any = undefined, private onDeleteSelectedCallback: any = undefined) {
			super();
			// Reference to the underlying data.
			this.data = chartDataModel;
			// Create a view-model for nodes.
			this.nodes = ChartViewModel.createNodesViewModel(this.data.nodes);
			// Create a view-model for connections.
			this.connections = this._createConnectionsViewModel(this.data.connections);

		};
		//
		// Create a view model for a new connection.
		//
		createNewConnection = (startConnector: any, endConnector: any, onCreateConnectionCallback: any, onEditConnectionCallback: any) => {
			var connectionsDataModel: any = this.data.connections;
			if (!connectionsDataModel) {
				connectionsDataModel = this.data.connections = [];
			}
			var connectionsViewModel: any = this.connections;
			if (!connectionsViewModel) {
				connectionsViewModel = this.connections = [];
			}

			var startNode: any = startConnector.parentNode();
			var startNodeConnectors: any = startNode.getAllConnectors();
			var startConnectorIndex: any = startNodeConnectors.indexOf(startConnector);
			var startConnectorType: any = 'input';
			if (startConnectorIndex == -1) {
				throw new Error("Failed to find source connector within either inputConnectors or outputConnectors of source node.");
			}

			var endNode: any = endConnector.parentNode();
			var endNodeConnectors: any = endNode.getAllConnectors();
			var endConnectorIndex: any = endNodeConnectors.indexOf(endConnector);
			var endConnectorType: any = 'input';
			if (endConnectorIndex == -1) {
				throw new Error("Failed to find dest connector within inputConnectors or outputConnectors of dest node.");
			}

			if (startConnectorType == endConnectorType) {
				//	throw new Error("Failed to create connection. Only output to input connections are allowed.")
			}

			if (startNode == endNode) {
				throw new Error("Failed to create connection. Cannot link a node with itthis.")
			}

			var startNode: any = {
				nodeID: startNode.data.id,
				connectorIndex: startConnectorIndex,
				connectorID: startConnector.data.id
			}

			var endNode: any = {
				nodeID: endNode.data.id,
				connectorIndex: endConnectorIndex,
				connectorId: endConnector.data.id
			}

			var connectionDataModel: any = {
				source: startConnectorType == 'output' ? startNode : endNode,
				dest: startConnectorType == 'output' ? endNode : startNode
			};
			connectionsDataModel.push(connectionDataModel);

			var outputConnector: any = startConnectorType == 'output' ? startConnector : endConnector;
			var inputConnector: any = startConnectorType == 'output' ? endConnector : startConnector;

			var src: any = this.findNode(connectionDataModel.source.nodeID);
			var dest: any = this.findNode(connectionDataModel.dest.nodeID);
			//connectionDataModel.name = src.name()+" - "+dest.name();
			connectionDataModel.joinKeys = {};
			connectionDataModel.edit = (viewModel: any) => {
				if (onEditConnectionCallback) {
					onEditConnectionCallback()(connectionViewModel, connectionDataModel, src, dest);
				}
			}
			var connectionViewModel = new flowchart.ConnectionViewModel(connectionDataModel, outputConnector, inputConnector);
			connectionsViewModel.push(connectionViewModel);
			if (onCreateConnectionCallback) {
				onCreateConnectionCallback()(connectionViewModel, connectionDataModel, src, dest, inputConnector, outputConnector);

			}
		};
		//
		// Add a node to the view model.
		//
		addNode = (nodeDataModel: any) => {
			if (!this.data.nodes) {
				this.data.nodes = [];
			}
			// 
			// Update the data model.
			//
			this.data.nodes.push(nodeDataModel);

			// 
			// Update the view model.
			//
			this.nodes.push(new flowchart.NodeViewModel(nodeDataModel));
		}

		//
		// Select all nodes and connections in the chart.
		//
		selectAll = () => {
			var nodes = this.nodes;
			for (var i = 0; i < nodes.length; ++i) {
				var node = nodes[i];
				node.select();
			}
			var connections = this.connections;
			for (var i = 0; i < connections.length; ++i) {
				var connection = connections[i];
				connection.select();
			}
		}

		//
		// Deselect all nodes and connections in the chart.
		//
		deselectAll = () => {
			var nodes = this.nodes;
			for (var i = 0; i < nodes.length; ++i) {
				var node = nodes[i];
				node.deselect();
			}

			var connections = this.connections;
			for (var i = 0; i < connections.length; ++i) {
				var connection = connections[i];
				connection.deselect();
			}
		};

		//
		// Update the location of the node and its connectors.
		//
		updateSelectedNodesLocation = (deltaX: any, deltaY: any) => {
			var selectedNodes: any = this.getSelectedNodes();
			for (var i = 0; i < selectedNodes.length; ++i) {
				var node = selectedNodes[i];
				node.data.x += deltaX;
				node.data.y += deltaY;
			}
		};
		// Handle mouse click on a particular node.
		//
		handleNodeClicked = (node: any, ctrlKey: any) => {
			if (ctrlKey) {
				node.toggleSelected();
			}
			else {
				this.deselectAll();
				node.select();
			}

			// Move node to the end of the list so it is rendered after all the other.
			// This is the way Z-order is done in SVG.

			var nodeIndex = this.nodes.indexOf(node);
			if (nodeIndex == -1) {
				throw new Error("Failed to find node in view model!");
			}
			this.nodes.splice(nodeIndex, 1);
			this.nodes.push(node);
		};

		//
		// Handle mouse down on a connection.
		//
		handleConnectionMouseDown = (connection: any, ctrlKey: any) => {
			if (ctrlKey) {
				connection.toggleSelected();
			}
			else {
				this.deselectAll();
				connection.select();
			}
		};

		//
		// Delete all nodes and connections that are selected.
		//
		deleteSelected = () => {

			var newNodeViewModels: any = [];
			var newNodeDataModels: any = [];
			var deletedNodeIds: any = [];
			// Sort nodes into:
			//		nodes to keep and 
			//		nodes to delete.

			for (var nodeIndex = 0; nodeIndex < this.nodes.length; ++nodeIndex) {
				var node: any = this.nodes[nodeIndex];
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

			var newConnectionViewModels: any = [];
			var newConnectionDataModels: any = [];

			//
			// Remove connections that are selected.
			// Also remove connections for nodes that have been deleted.
			//
			for (var connectionIndex = 0; connectionIndex < this.connections.length; ++connectionIndex) {

				var connection: any = this.connections[connectionIndex];
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
			this.nodes = newNodeViewModels;
			this.data.nodes = newNodeDataModels;
			this.connections = newConnectionViewModels;
			this.data.connections = newConnectionDataModels;

			if (this.onDeleteSelectedCallback) {
				this.onDeleteSelectedCallback();
			}
		};

		//
		// Select nodes and connections that fall within the selection rect.
		//
		applySelectionRect = (selectionRect: any) => {
			this.deselectAll();
			for (var i = 0; i < this.nodes.length; ++i) {
				var node = this.nodes[i];
				if (node.x() >= selectionRect.x &&
					node.y() >= selectionRect.y &&
					node.x() + node.width() <= selectionRect.x + selectionRect.width &&
					node.y() + node.height() <= selectionRect.y + selectionRect.height) {
					// Select nodes that are within the selection rect.
					node.select();
				}
			}

			for (var i = 0; i < this.connections.length; ++i) {
				var connection: any = this.connections[i];
				if (connection.source.parentNode().selected() &&
					connection.dest.parentNode().selected()) {
					// Select the connection if both its parent nodes are selected.
					connection.select();
				}
			}

		};
		// Get the array of nodes that are currently selected.
		getSelectedNodes = () => {
			var selectedNodes: any = [];

			for (var i = 0; i < this.nodes.length; ++i) {
				var node = this.nodes[i];
				if (node.selected()) {
					selectedNodes.push(node);
				}
			}
			return selectedNodes;
		};

		//
		// Get the array of connections that are currently selected.
		//
		getSelectedConnections = () => {
			var selectedConnections: any = [];

			for (var i = 0; i < this.connections.length; ++i) {
				var connection: any = this.connections[i];
				if (connection.selected()) {
					selectedConnections.push(connection);
				}
			}
			return selectedConnections;
		};
		//
		// Find a specific node within the chart.
		//
		findNode = (nodeID: any) => {

			for (var i = 0; i < this.nodes.length; ++i) {
				var node = this.nodes[i];
				if (node.data.id == nodeID) {
					return node;
				}
			}

			throw new Error("Failed to find node " + nodeID);
		};

		findConnector = (nodeID: any, connectorIndex: any) => {

			var node = this.findNode(nodeID);

			if (!node.connectors || node.connectors.length <= connectorIndex) {
				throw new Error("Node " + nodeID + " has invalid input connectors.");
			}

			return node.connectors[connectorIndex];
		};


		//
		// Find a specific input connector within the chart.
		//
		findInputConnector = (nodeID: any, connectorIndex: any) => {

			var node: any = this.findNode(nodeID);

			if (!node.inputConnectors || node.inputConnectors.length <= connectorIndex) {
				throw new Error("Node " + nodeID + " has invalid input connectors.");
			}

			return node.inputConnectors[connectorIndex];
		};

		//
		// Find a specific output connector within the chart.
		//
		findOutputConnector = (nodeID: any, connectorIndex: any) => {

			var node: any = this.findNode(nodeID);

			if (!node.outputConnectors || node.outputConnectors.length <= connectorIndex) {
				throw new Error("Node " + nodeID + " has invalid output connectors.");
			}

			return node.outputConnectors[connectorIndex];
		};

		//
		// Create a view model for connection from the data model.
		//
		_createConnectionViewModel = (connectionDataModel: any) => {

			// Create connection view
			var sourceConnector: any = this.findConnector(connectionDataModel.source.nodeID, connectionDataModel.source.connectorIndex);
			var destConnector: any = this.findConnector(connectionDataModel.dest.nodeID, connectionDataModel.dest.connectorIndex);
			var connectionViewModel: any = new flowchart.ConnectionViewModel(connectionDataModel, sourceConnector, destConnector);

			// Set callback function for editing the connection
			var source = this.findNode(connectionDataModel.source.nodeID);
			var dest = this.findNode(connectionDataModel.dest.nodeID);
			connectionDataModel.edit = () => {
				if (this.onEditConnectionCallback) {
					this.onEditConnectionCallback(connectionViewModel, connectionDataModel, source, dest);
				}
			};

			// Return connection view
			return connectionViewModel;
		}

		//
		// Wrap the connections data-model in a view-model.
		//
		_createConnectionsViewModel = (connectionsDataModel: any) => {
			var connectionsViewModel: any = [];
			if (connectionsDataModel) {
				for (var i = 0; i < connectionsDataModel.length; ++i) {
					connectionsViewModel.push(this._createConnectionViewModel(connectionsDataModel[i]));
				}
			}
			return connectionsViewModel;
		};

	}
}
