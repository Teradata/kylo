// Global accessor.
var flowchart: any = {};
// Module.
(function () {
	//
	// Width of a node.
	flowchart.defaultNodeWidth = 250;
	// Amount of space reserved for displaying the node's name.
	flowchart.nodeNameHeight = 75;
	// Height of a connector in a node.
	flowchart.connectorHeight = 35;
	flowchart.attributeHeight=25;
	flowchart.CONNECTOR_LOCATION = {TOP:"TOP",BOTTOM:"BOTTOM",LEFT:"LEFT",RIGHT:"RIGHT"};
	flowchart.connectorRadius = 5;
	flowchart.nextConnectorID = 1
	// Compute the Y coordinate of a connector, given its index.
	/**
	 * @Deprecated
	 * @param connectorIndex
	 * @returns {number}
	 */
	flowchart.computeConnectorY=(connectorIndex: any)=>{
		return flowchart.nodeNameHeight + (connectorIndex * flowchart.connectorHeight);
	}
	// Compute the position of a connector in the graph.
	/**
	 * @Deprecated
	 * @param node
	 * @param connectorIndex
	 * @param inputConnector
	 * @returns {{x: *, y: *}}
	 */
	flowchart.computeConnectorPosOld = (node: any, connectorIndex: any, inputConnector: any)=> {
		return {
			x: node.x() + (inputConnector ? 0 : node.width ? node.width() : flowchart.defaultNodeWidth),
			y: node.y() + flowchart.computeConnectorY(connectorIndex),
		};
	};

	flowchart.computeConnectorPos = (node: any, connector: any)=> {
		var x: any = 0;
		if(connector.location() == flowchart.CONNECTOR_LOCATION.LEFT || connector.location() == flowchart.CONNECTOR_LOCATION.RIGHT){
		 x = node.x() + (connector.location() == flowchart.CONNECTOR_LOCATION.LEFT ? 0 : node.width ? node.width() : flowchart.defaultNodeWidth);
		}
		else {
			//center the top and bottom
			x = node.x() + (node.width ? node.width() : flowchart.defaultNodeWidth)/2;
		}
		var y = 0;
		if(connector.location() == flowchart.CONNECTOR_LOCATION.LEFT || connector.location() == flowchart.CONNECTOR_LOCATION.RIGHT){
			y = node.y()+(node.height())/2
		}
		else {
			y = node.y() + (connector.location() == flowchart.CONNECTOR_LOCATION.TOP ? 0 : node.height())
		}

		return {
			x: x,
			y: y
		};
	};
	// View model for a connector.
	flowchart.ConnectorViewModel = (connectorDataModel: any, x: any, y: any, parentNode: any)=> {
		this.data = connectorDataModel;
		this._parentNode = parentNode;
		this._x = x;
		this._y = y;
		this.TRIANGLE_SIZE = 30;
		// The name of the connector.
		this.name =()=>{
			return this.data.name;
		}
		this.trianglePoints=()=>{
			var start: any = "";
			var end: any = "";
			var point: any = "";
			var point2: any = "";
			if(this.location() == flowchart.CONNECTOR_LOCATION.BOTTOM){
				 start = this.x() - this.TRIANGLE_SIZE/2+","+this.y();
				 end = this.x() + this.TRIANGLE_SIZE/2+","+this.y();
				 point = this.x()+","+(this.y()+this.TRIANGLE_SIZE);
				 //point2 = this.x()+","+(this.y()+this.TRIANGLE_SIZE);
			}
			else if(this.location() == flowchart.CONNECTOR_LOCATION.TOP){
				 start = this.x() - this.TRIANGLE_SIZE/2+","+this.y();
				 end = this.x() + this.TRIANGLE_SIZE/2+","+this.y();
				 point = this.x()+","+(this.y()-this.TRIANGLE_SIZE);
			}
			if(this.location() == flowchart.CONNECTOR_LOCATION.LEFT){
				start = this.x()+","+(this.y()- this.TRIANGLE_SIZE/2);
				end = this.x()+","+(this.y()+ this.TRIANGLE_SIZE/2);
				point = (this.x()-this.TRIANGLE_SIZE)+","+this.y();
			}
			else if(this.location() == flowchart.CONNECTOR_LOCATION.RIGHT){
				start = this.x()+","+(this.y()- this.TRIANGLE_SIZE/2);
				end = this.x()+","+(this.y()+ this.TRIANGLE_SIZE/2);
				point = (this.x()+this.TRIANGLE_SIZE)+","+this.y();
			}
				return start+" "+end+" "+point;

		}

		this.location=()=>
		{
			return this.data.location;
		}
		this.id=()=>{
			return this.data.id;
		}
		// X coordinate of the connector.
		this.x =()=>{
			return this._x;
		};

		//
		// Y coordinate of the connector.
		//
		this.y=()=>{ 
			return this._y;
		};

		//
		// The parent node that the connector is attached to.
		//
		this.parentNode=()=> {
			return this._parentNode;
		};
	};

	// Create view model for a list of data models.
	var createConnectorsViewModel: any = (connectorDataModels: any, x: any, parentNode: any)=>{
		var viewModels: any = [];
		if (connectorDataModels) {
			for (var i = 0; i < connectorDataModels.length; ++i) {
				var connectorViewModel = 
					new flowchart.ConnectorViewModel(connectorDataModels[i], x, flowchart.computeConnectorY(i), parentNode);
				viewModels.push(connectorViewModel);
			}
		}
		return viewModels;
	};

	var createConnectorViewModel: any = (connectorDataModel: any, x: any, y: any, parentNode: any)=> {
		var connectorViewModel: any = null
		if (connectorDataModel) {
			 connectorViewModel =
				new flowchart.ConnectorViewModel(connectorDataModel, x, y, parentNode);
		}
		return connectorViewModel;
	};

	//
	// View model for a node.
	//
	flowchart.NodeViewModel = (nodeDataModel: any)=>{
		this.data = nodeDataModel;
		// set the default width value of the node
		if (!this.data.width || this.data.width < 0) {
			this.data.width = flowchart.defaultNodeWidth;
		}
		this.connectors = [];
		this.leftConnectors = [];
		this.rightConnectors = [];
		this.bottomConnectors =[];
		this.topConnectors = [];
		function createConnectors() {
			var connectors: any = this.data.connectors;
			var leftConnector: any = connectors.left;
			if (leftConnector) {
				leftConnector.location = flowchart.CONNECTOR_LOCATION.LEFT;
				leftConnector.id = flowchart.nextConnectorID++;
				var connectorViewModel = createConnectorViewModel(leftConnector, 0, this.height()/2, this);
				this.leftConnectors.push(connectorViewModel);
				this.connectors.push(connectorViewModel);
			}
			var rightConnector = connectors.right;
			if (rightConnector) {
				rightConnector.location = flowchart.CONNECTOR_LOCATION.RIGHT;
				rightConnector.id = flowchart.nextConnectorID++;
				var connectorViewModel = createConnectorViewModel(rightConnector,this.data.width,this.height()/2, this);
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
		this._selected = false;
		// Name of the node.
		this.name = function () {
			return this.data.name || "";
		};
		// X coordinate of the node.
		this.x = ()=> { 
			return this.data.x;
		};
		// Y coordinate of the node.
		this.y = ()=> {
			return this.data.y;
		};

		//
		// Width of the node.
		//
		this.width = ()=> {
			return this.data.width;
		}
		//
		// Height of the node.
		//
		this.height = ()=> {
			if(this.data.height) {
				return this.data.height;
			}
			else if(this.data.nodeAttributes && this.data.nodeAttributes.attributes){
				return 95+ flowchart.attributeHeight*this.data.nodeAttributes.attributes.length;
			}
			else {
				var numConnectors =
					Math.max(
						this.inputConnectors.length,
						this.outputConnectors.length);
				return flowchart.computeConnectorY(numConnectors);
			}
		}
		// Select the node.
		this.select = ()=> {
			this._selected = true;
		};

		// Deselect the node.
		this.deselect = ()=> {
			this._selected = false;
		};

		//
		// Toggle the selection state of the node.
		//
		this.toggleSelected = ()=> {
			this._selected = !this._selected;
		};

		//
		// Returns true if the node is selected.
		//
		this.selected = ()=> {
			return this._selected;
		};

		//
		// Internal function to add a connector.
		this._addConnector = (connectorDataModel: any, x: any, connectorsDataModel: any, connectorsViewModel: any)=> {
			var connectorViewModel: any = 
				new flowchart.ConnectorViewModel(connectorDataModel, x, 
						flowchart.computeConnectorY(connectorsViewModel.length), this);

			connectorsDataModel.push(connectorDataModel);

			// Add to node's view model.
			connectorsViewModel.push(connectorViewModel);
		}

		//
		// Add an input connector to the node.
		//TODO change to AddTop, addLeft, addRight,..etc
		//
		this.addInputConnector = (connectorDataModel: any) =>{

			if (!this.data.inputConnectors) {
				this.data.inputConnectors = [];
			}
			this._addConnector(connectorDataModel, 0, this.data.inputConnectors, this.inputConnectors);
		};

		//
		// Add an ouput connector to the node.
		//
		this.addOutputConnector = (connectorDataModel: any) =>{
			if (!this.data.outputConnectors) {
				this.data.outputConnectors = [];
			}
			this._addConnector(connectorDataModel, this.data.width, this.data.outputConnectors, this.outputConnectors);
		};

		createConnectors();


		this.getAllConnectors = ()=>{
				return this.connectors;
		}
	};

	// 
	// Wrap the nodes data-model in a view-model.
	//
	var createNodesViewModel = (nodesDataModel: any)=> {
		var nodesViewModel : any= [];
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
	flowchart.ConnectionViewModel = (connectionDataModel: any, sourceConnector: any, destConnector: any)=>{
		this.data = connectionDataModel;
		this.source = sourceConnector;
		this.dest = destConnector;
		// Set to true when the connection is selected.
		this._selected = false;
		this.name = ()=> {
			var name : any=  this.data.name || "";
			return name;
		}

		this.sourceCoordX =()=> { 
			return this.source.parentNode().x() + this.source.x();
		};

		this.sourceCoordY =()=> { 
			return this.source.parentNode().y() + this.source.y();
		};

		this.edit = ()=>{
		  this.data.edit(this);
		}


		this.sourceCoord =()=> {
			return {
				x: this.sourceCoordX(),
				y: this.sourceCoordY()
			};
		}

		this.sourceTangentX = ()=> { 
			return flowchart.computeConnectionSourceTangentX(this.sourceCoord(), this.destCoord());
		};

		this.sourceTangentY = ()=> { 
			return flowchart.computeConnectionSourceTangentY(this.sourceCoord(), this.destCoord());
		};

		this.destCoordX = ()=>{ 
			return this.dest.parentNode().x() + this.dest.x();
		};

		this.destCoordY = ()=> { 
			return this.dest.parentNode().y() + this.dest.y();
		};

		this.destCoord = ()=>{
			return {
				x: this.destCoordX(),
				y: this.destCoordY()
			};
		}

		this.destTangentX = ()=>{ 
			return flowchart.computeConnectionDestTangentX(this.sourceCoord(), this.destCoord());
		};

		this.destTangentY = ()=> { 
			return flowchart.computeConnectionDestTangentY(this.sourceCoord(), this.destCoord());
		};

		this.middleX = (scale: any)=> {
			if(typeof(scale)=="undefined")
				scale = 0.5;
			return this.sourceCoordX()*(1-scale)+this.destCoordX()*scale;
		};

		this.middleY = (scale: any)=> {
			if(typeof(scale)=="undefined")
				scale = 0.5;
			return this.sourceCoordY()*(1-scale)+this.destCoordY()*scale;
		};


		//
		// Select the connection.
		//
		this.select = ()=> {
			this._selected = true;
		};

		//
		// Deselect the connection.
		//
		this.deselect = ()=> {
			this._selected = false;
		};

		//
		// Toggle the selection state of the connection.
		//
		this.toggleSelected = ()=>{
			this._selected = !this._selected;
		};

		//
		// Returns true if the connection is selected.
		//
		this.selected = ()=>{
			return this._selected;
		};
	};

	//
	// Helper function.
	//
	var computeConnectionTangentOffset = (pt1:any, pt2:any)=> {
		return (pt2.x - pt1.x) / 2;	
	}

	//
	// Compute the tangent for the bezier curve.
	//
	flowchart.computeConnectionSourceTangentX = (pt1:any, pt2:any)=> {
		return pt1.x + computeConnectionTangentOffset(pt1, pt2);
	};

	//
	// Compute the tangent for the bezier curve.
	//
	flowchart.computeConnectionSourceTangentY =  (pt1:any, pt2:any)=> {
		return pt1.y;
	};

	//
	// Compute the tangent for the bezier curve.
	//
	flowchart.computeConnectionSourceTangent = (pt1:any, pt2:any)=> {
		return {
			x: flowchart.computeConnectionSourceTangentX(pt1, pt2),
			y: flowchart.computeConnectionSourceTangentY(pt1, pt2),
		};
	};

	//
	// Compute the tangent for the bezier curve.
	//
	flowchart.computeConnectionDestTangentX = (pt1:any, pt2:any)=> {

		return pt2.x - computeConnectionTangentOffset(pt1, pt2);
	};

	//
	// Compute the tangent for the bezier curve.
	//
	flowchart.computeConnectionDestTangentY = (pt1:any, pt2:any)=> {

		return pt2.y;
	};

	//
	// Compute the tangent for the bezier curve.
	//
	flowchart.computeConnectionDestTangent = (pt1:any, pt2:any)=> {
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
	flowchart.ChartViewModel = function(chartDataModel:any, onCreateConnectionCallback:any, onEditConnectionCallback:any, onDeleteSelectedCallback:any){
		//
		// Find a specific node within the chart.
		//
		this.findNode = (nodeID: any)=> {

			for (var i = 0; i < this.nodes.length; ++i) {
				var node = this.nodes[i];
				if (node.data.id == nodeID) {
					return node;
				}
			}

			throw new Error("Failed to find node " + nodeID);
		};

		this.findConnector = (nodeID: any, connectorIndex: any)=> {

			var node = this.findNode(nodeID);

			if (!node.connectors || node.connectors.length <= connectorIndex) {
				throw new Error("Node " + nodeID + " has invalid input connectors.");
			}

			return node.connectors[connectorIndex];
		};


		//
		// Find a specific input connector within the chart.
		//
		this.findInputConnector = (nodeID: any, connectorIndex: any)=>{

			var node: any = this.findNode(nodeID);

			if (!node.inputConnectors || node.inputConnectors.length <= connectorIndex) {
				throw new Error("Node " + nodeID + " has invalid input connectors.");
			}

			return node.inputConnectors[connectorIndex];
		};

		//
		// Find a specific output connector within the chart.
		//
		this.findOutputConnector =(nodeID: any, connectorIndex: any)=>{

			var node: any = this.findNode(nodeID);

			if (!node.outputConnectors || node.outputConnectors.length <= connectorIndex) {
				throw new Error("Node " + nodeID + " has invalid output connectors.");
			}

			return node.outputConnectors[connectorIndex];
		};

		//
		// Create a view model for connection from the data model.
		//
		this._createConnectionViewModel = (connectionDataModel: any)=>{

			// Create connection view
			var sourceConnector: any = this.findConnector(connectionDataModel.source.nodeID, connectionDataModel.source.connectorIndex);
			var destConnector: any = this.findConnector(connectionDataModel.dest.nodeID, connectionDataModel.dest.connectorIndex);
			var connectionViewModel: any = new flowchart.ConnectionViewModel(connectionDataModel, sourceConnector, destConnector);

			// Set callback function for editing the connection
			var source = this.findNode(connectionDataModel.source.nodeID);
			var dest = this.findNode(connectionDataModel.dest.nodeID);
			connectionDataModel.edit = ()=> {
				if (onEditConnectionCallback) {
					onEditConnectionCallback(connectionViewModel, connectionDataModel, source, dest);
				}
			};

			// Return connection view
			return connectionViewModel;
		}

		// 
		// Wrap the connections data-model in a view-model.
		//
		this._createConnectionsViewModel = (connectionsDataModel:any)=> {
			var connectionsViewModel:any = [];
			if (connectionsDataModel) {
				for (var i = 0; i < connectionsDataModel.length; ++i) {
					connectionsViewModel.push(this._createConnectionViewModel(connectionsDataModel[i]));
				}
			}
			return connectionsViewModel;
		};

		// Reference to the underlying data.
		this.data = chartDataModel;
		// Create a view-model for nodes.
		this.nodes = createNodesViewModel(this.data.nodes);
		// Create a view-model for connections.
		this.connections = this._createConnectionsViewModel(this.data.connections);
		//
		// Create a view model for a new connection.
		//
		this.createNewConnection = (startConnector:any, endConnector:any, onCreateConnectionCallback:any, onEditConnectionCallback:any)=> {
			var connectionsDataModel:any = this.data.connections;
			if (!connectionsDataModel) {
				connectionsDataModel = this.data.connections = [];
			}
			var connectionsViewModel:any = this.connections;
			if (!connectionsViewModel) {
				connectionsViewModel = this.connections = [];
			}

			var startNode:any = startConnector.parentNode();
			var startNodeConnectors:any = startNode.getAllConnectors();
			var startConnectorIndex:any = startNodeConnectors.indexOf(startConnector);
			var startConnectorType:any = 'input';
			if (startConnectorIndex == -1) {
					throw new Error("Failed to find source connector within either inputConnectors or outputConnectors of source node.");
			}

			var endNode:any = endConnector.parentNode();
			var endNodeConnectors:any = endNode.getAllConnectors();
			var endConnectorIndex:any = endNodeConnectors.indexOf(endConnector);
			var endConnectorType:any = 'input';
				if (endConnectorIndex == -1) {
					throw new Error("Failed to find dest connector within inputConnectors or outputConnectors of dest node.");
				}

			if (startConnectorType == endConnectorType) {
			//	throw new Error("Failed to create connection. Only output to input connections are allowed.")
			}

			if (startNode == endNode) {
				throw new Error("Failed to create connection. Cannot link a node with itthis.")
			}

			var startNode:any = {
				nodeID: startNode.data.id,
				connectorIndex: startConnectorIndex,
				connectorID: startConnector.data.id
			}

			var endNode:any = {
				nodeID: endNode.data.id,
				connectorIndex: endConnectorIndex,
				connectorId: endConnector.data.id
			}

			var connectionDataModel:any = {
				source: startConnectorType == 'output' ? startNode : endNode,
				dest: startConnectorType == 'output' ? endNode : startNode
			};
			connectionsDataModel.push(connectionDataModel);

			var outputConnector:any = startConnectorType == 'output' ? startConnector : endConnector;
			var inputConnector:any = startConnectorType == 'output' ? endConnector : startConnector;

			var src:any = this.findNode(connectionDataModel.source.nodeID);
			var dest:any = this.findNode(connectionDataModel.dest.nodeID);
			//connectionDataModel.name = src.name()+" - "+dest.name();
			connectionDataModel.joinKeys = {};
			connectionDataModel.edit = (viewModel:any)=>{
				if(onEditConnectionCallback){
					onEditConnectionCallback()(connectionViewModel,connectionDataModel,src,dest);
				}
			}
			var connectionViewModel = new flowchart.ConnectionViewModel(connectionDataModel, outputConnector, inputConnector);
			connectionsViewModel.push(connectionViewModel);
			if(onCreateConnectionCallback){
				onCreateConnectionCallback()(connectionViewModel,connectionDataModel,src,dest,inputConnector,outputConnector);

			}
		};
		//
		// Add a node to the view model.
		//
		this.addNode = (nodeDataModel:any)=> {
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
		this.selectAll = ()=> {
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
		this.deselectAll = ()=>{
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
		this.updateSelectedNodesLocation = (deltaX:any, deltaY:any)=>{
			var selectedNodes:any = this.getSelectedNodes();
			for (var i = 0; i < selectedNodes.length; ++i) {
				var node = selectedNodes[i];
				node.data.x += deltaX;
				node.data.y += deltaY;
			}
		};
		// Handle mouse click on a particular node.
		//
		this.handleNodeClicked = (node:any, ctrlKey:any)=>{
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
		this.handleConnectionMouseDown = (connection:any, ctrlKey:any)=> {
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
		this.deleteSelected = ()=> {

			var newNodeViewModels:any = [];
			var newNodeDataModels:any = [];
			var deletedNodeIds:any = [];
			// Sort nodes into:
			//		nodes to keep and 
			//		nodes to delete.
			
			for (var nodeIndex = 0; nodeIndex < this.nodes.length; ++nodeIndex) {
				var node:any = this.nodes[nodeIndex];
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

			var newConnectionViewModels:any = [];
			var newConnectionDataModels:any = [];

			//
			// Remove connections that are selected.
			// Also remove connections for nodes that have been deleted.
			//
			for (var connectionIndex = 0; connectionIndex < this.connections.length; ++connectionIndex) {

				var connection:any = this.connections[connectionIndex];				
				if (!connection.selected() &&
					deletedNodeIds.indexOf(connection.data.source.nodeID) === -1 &&
					deletedNodeIds.indexOf(connection.data.dest.nodeID) === -1)
				{
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

			if (onDeleteSelectedCallback) {
				onDeleteSelectedCallback();
			}
		};

		//
		// Select nodes and connections that fall within the selection rect.
		//
		this.applySelectionRect = (selectionRect:any)=> {
			this.deselectAll();
			for (var i = 0; i < this.nodes.length; ++i) {
				var node = this.nodes[i];
				if (node.x() >= selectionRect.x && 
					node.y() >= selectionRect.y && 
					node.x() + node.width() <= selectionRect.x + selectionRect.width &&
					node.y() + node.height() <= selectionRect.y + selectionRect.height)
				{
					// Select nodes that are within the selection rect.
					node.select();
				}
			}

			for (var i = 0; i < this.connections.length; ++i) {
				var connection:any = this.connections[i];
				if (connection.source.parentNode().selected() &&
					connection.dest.parentNode().selected())
				{
					// Select the connection if both its parent nodes are selected.
					connection.select();
				}
			}

		};
		// Get the array of nodes that are currently selected.
		this.getSelectedNodes = ()=> {
			var selectedNodes:any = [];

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
		this.getSelectedConnections=()=> {
			var selectedConnections:any = [];

			for (var i = 0; i < this.connections.length; ++i) {
				var connection: any = this.connections[i];
				if (connection.selected()) {
					selectedConnections.push(connection);
				}
			}
			return selectedConnections;
		};
	};

})();
