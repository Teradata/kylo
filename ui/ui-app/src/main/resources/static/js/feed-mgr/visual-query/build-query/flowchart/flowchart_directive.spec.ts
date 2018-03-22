var mocks = require("angular-mocks");
import "./flowchart_directive";

describe('flowchart-directive', ()=> {
	var testObject: any;
	var mockScope: any;
	var mockDragging: any;
	var mockSvgElement: any;

	//
	// Bring in the flowChart module before each test.
	//
	beforeEach(mocks.module('flowChart'));

	//
	// Helper function to create the controller for each test.
	//
	var createController = ($rootScope: any, $controller: any) =>{

 		mockScope = $rootScope.$new();
 		mockDragging = createMockDragging();
		mockSvgElement = {
			get: ()=> {
				return createMockSvgElement();
			}
		};

      	testObject = $controller('FlowChartController', {
      		$scope: mockScope,
      		dragging: mockDragging,
      		$element: mockSvgElement,
      	});
	};

	//
	// Setup the controller before each test.
	//
	beforeEach(inject(($rootScope: any, $controller: any)=> {

		createController($rootScope, $controller);
	}));

	// 
	// Create a mock DOM element.
	//
	var createMockElement: any = (attr: any, parent: any, scope: any) =>{
		return {
			attr: ()=> {
				return attr;
			},

			parent: ()=> {
				return parent;
			},		

			scope: ()=> {
				return scope || {};
			},

		};
	}

	//
	// Create a mock node data model.
	//
	var createMockNode: any = (inputConnectors: any, outputConnectors: any)=> {
		return {
			x: ()=> { return 0 },
			y: ()=> { return 0 },
			inputConnectors: inputConnectors || [],
			outputConnectors: outputConnectors || [],
			select: jasmine.createSpy('select'),
			selected: ()=> { return false; },
		};
	};

	//
	// Create a mock chart.
	//
	var createMockChart: any = (mockNodes: any, mockConnections: any)=> {
		return {
			nodes: mockNodes,
			connections: mockConnections,

			handleNodeClicked: jasmine.createSpy('handleNodeClicked'),
			handleConnectionMouseDown: jasmine.createSpy('handleConnectionMouseDown'),
			updateSelectedNodesLocation: jasmine.createSpy('updateSelectedNodesLocation'),
			deselectAll: jasmine.createSpy('deselectAll'),
			createNewConnection: jasmine.createSpy('createNewConnection'),
			applySelectionRect: jasmine.createSpy('applySelectionRect'),
		};
	};

	//
	// Create a mock dragging service.
	//
	var createMockDragging: any = ()=> {

		var mockDragging: any = {
			startDrag: (evt: any, config: any)=> {
				mockDragging.evt = evt;
				mockDragging.config = config;
			},
		};

		return mockDragging;
	};

	//
	// Create a mock version of the SVG element.
	//
	var createMockSvgElement: any = ()=> {
		return {
			getScreenCTM: ()=> {
				return {
					inverse: ()=> {
						return this;
					},
				};
			},

			createSVGPoint: ()=> {
				return { 
					x: 0, 
					y: 0 ,
					matrixTransform: ()=> {
						return this;
					},
				};
			}



		};
	};

	it('searchUp returns null when at root 1', ()=> {

		expect(testObject.searchUp(null, "some-class")).toBe(null);
	});


	it('searchUp returns null when at root 2', ()=> {

		expect(testObject.searchUp([], "some-class")).toBe(null);
	});

	it('searchUp returns element when it has requested class', ()=> {

		var whichClass: any= "some-class";
		var mockElement: any= createMockElement(whichClass);

		expect(testObject.searchUp(mockElement, whichClass)).toBe(mockElement);
	});

	it('searchUp returns parent when it has requested class', ()=> {

		var whichClass: any = "some-class";
		var mockParent: any = createMockElement(whichClass);
		var mockElement: any = createMockElement('', mockParent);

		expect(testObject.searchUp(mockElement, whichClass)).toBe(mockParent);
	});

	it('hitTest returns result of elementFromPoint', ()=> {

		var mockElement: any = {};

		// Mock out the document.
		testObject.document = {
			elementFromPoint: ()=> {
				return mockElement;
			},
		};

		expect(testObject.hitTest(12, 30)).toBe(mockElement);
	});

	it('checkForHit returns null when the hit element has no parent with requested class', ()=> {

		var mockElement: any = createMockElement(null, null);

		testObject.jQuery = (input: any)=> {
			return input;
		};

		expect(testObject.checkForHit(mockElement, "some-class")).toBe(null);
	});

	it('checkForHit returns the result of searchUp when found', ()=> {

		var mockConnectorScope: any = {};

		var whichClass: any = "some-class";
		var mockElement: any = createMockElement(whichClass, null, mockConnectorScope);

		testObject.jQuery = (input: any) =>{
			return input;
		};

		expect(testObject.checkForHit(mockElement, whichClass)).toBe(mockConnectorScope);
	});	

	it('checkForHit returns null when searchUp fails', ()=>{

		var mockElement: any = createMockElement(null, null, null);

		testObject.jQuery = (input: any) =>{
			return input;
		};

		expect(testObject.checkForHit(mockElement, "some-class")).toBe(null);
	});	

	it('test node dragging is started on node mouse down',()=> {

		mockDragging.startDrag = jasmine.createSpy('mockDragging.startDrag');

		var mockEvt: any = {};
		var mockNode: any = createMockNode();

		mockScope.nodeMouseDown(mockEvt, mockNode);

		expect(mockDragging.startDrag).toHaveBeenCalled();

	});

	it('test node click handling is forwarded to view model', ()=> {

		mockScope.chart = createMockChart([mockNode]);

		var mockEvt: any = {
			ctrlKey: false,
		};
		var mockNode: any = createMockNode();

		mockScope.nodeMouseDown(mockEvt, mockNode);

		mockDragging.config.clicked();

		expect(mockScope.chart.handleNodeClicked).toHaveBeenCalledWith(mockNode, false);
	});

	it('test control + node click handling is forwarded to view model', ()=> {

		var mockNode: any = createMockNode();

		mockScope.chart = createMockChart([mockNode]);

		var mockEvt: any = {
			ctrlKey: true,
		};

		mockScope.nodeMouseDown(mockEvt, mockNode);

		mockDragging.config.clicked();

		expect(mockScope.chart.handleNodeClicked).toHaveBeenCalledWith(mockNode, true);
	});

	it('test node dragging updates selected nodes location', ()=> {

		var mockEvt: any = {
			view: {
				pageXOffset: 0,
				pageYOffset: 0,
			},
		};

		mockScope.chart = createMockChart([createMockNode()]);

		mockScope.nodeMouseDown(mockEvt, mockScope.chart.nodes[0]);

		var xIncrement: any = 5;
		var yIncrement: any = 15;

		mockDragging.config.dragStarted(0, 0);
		mockDragging.config.dragging(xIncrement, yIncrement);

		expect(mockScope.chart.updateSelectedNodesLocation).toHaveBeenCalledWith(xIncrement, yIncrement);
	});

	it('test node dragging doesnt modify selection when node is already selected', ()=> {

		var mockNode1: any = createMockNode();
		var mockNode2: any = createMockNode();

		mockScope.chart = createMockChart([mockNode1, mockNode2]);

		mockNode2.selected = ()=> { return true; }

		var mockEvt = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.nodeMouseDown(mockEvt, mockNode2);

		mockDragging.config.dragStarted(0, 0);

		expect(mockScope.chart.deselectAll).not.toHaveBeenCalled();
	});

	it('test node dragging selects node, when the node is not already selected', ()=> {

		var mockNode1: any = createMockNode();
		var mockNode2: any = createMockNode();

		mockScope.chart = createMockChart([mockNode1, mockNode2]);

		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.nodeMouseDown(mockEvt, mockNode2);

		mockDragging.config.dragStarted(0, 0);

		expect(mockScope.chart.deselectAll).toHaveBeenCalled();
		expect(mockNode2.select).toHaveBeenCalled();
	});

	it('test connection click handling is forwarded to view model', ()=> {

		var mockNode: any = createMockNode();

		var mockEvt: any = {
			stopPropagation: jasmine.createSpy('stopPropagation'),
			preventDefault: jasmine.createSpy('preventDefault'),
			ctrlKey: false,
		};
		var mockConnection: any = {};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.connectionMouseDown(mockEvt, mockConnection);

		expect(mockScope.chart.handleConnectionMouseDown).toHaveBeenCalledWith(mockConnection, false);
		expect(mockEvt.stopPropagation).toHaveBeenCalled();
		expect(mockEvt.preventDefault).toHaveBeenCalled();
	});

	it('test control + connection click handling is forwarded to view model', ()=> {

		var mockNode: any = createMockNode();

		var mockEvt: any = {
			stopPropagation: jasmine.createSpy('stopPropagation'),
			preventDefault: jasmine.createSpy('preventDefault'),
			ctrlKey: true,
		};
		var mockConnection: any = {};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.connectionMouseDown(mockEvt, mockConnection);

		expect(mockScope.chart.handleConnectionMouseDown).toHaveBeenCalledWith(mockConnection, true);
	});

	it('test selection is cleared when background is clicked', ()=> {

		var mockEvt: any = {};

		mockScope.chart = createMockChart([createMockNode()]);

		mockScope.chart.nodes[0].selected = true;

		mockScope.mouseDown(mockEvt);

		expect(mockScope.chart.deselectAll).toHaveBeenCalled();
	});	

	it('test background mouse down commences selection dragging', ()=> {

		var mockNode: any = createMockNode();
		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.mouseDown(mockEvt);

		mockDragging.config.dragStarted(0, 0);

		expect(mockScope.dragSelecting).toBe(true);
	});

	it('test can end selection dragging', ()=> {

		var mockNode: any = createMockNode();
		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.mouseDown(mockEvt);

 		mockDragging.config.dragStarted(0, 0, mockEvt);
 		mockDragging.config.dragging(0, 0, mockEvt);
 		mockDragging.config.dragEnded();

		expect(mockScope.dragSelecting).toBe(false);		
 	});

	it('test selection dragging ends by selecting nodes', ()=> {

		var mockNode: any = createMockNode();
		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.mouseDown(mockEvt);

 		mockDragging.config.dragStarted(0, 0, mockEvt);
 		mockDragging.config.dragging(0, 0, mockEvt);

 		var selectionRect: any = { 
 			x: 1,
 			y: 2,
 			width: 3,
 			height: 4,
 		};

 		mockScope.dragSelectionRect = selectionRect;

 		mockDragging.config.dragEnded();

		expect(mockScope.chart.applySelectionRect).toHaveBeenCalledWith(selectionRect);
 	});

	xit('test mouse down commences connection dragging', ()=> {

		var mockNode: any = createMockNode();
		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.connectorMouseDown(mockEvt, mockScope.chart.nodes[0], mockScope.chart.nodes[0].inputConnectors[0], 0, false);

		mockDragging.config.dragStarted(0, 0);

		expect(mockScope.draggingConnection).toBe(true);		
	});

	xit('test can end connection dragging', ()=> {

		var mockNode: any = createMockNode();
		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.connectorMouseDown(mockEvt, mockScope.chart.nodes[0], mockScope.chart.nodes[0].inputConnectors[0], 0, false);

 		mockDragging.config.dragStarted(0, 0, mockEvt);
 		mockDragging.config.dragging(0, 0, mockEvt);
 		mockDragging.config.dragEnded();

		expect(mockScope.draggingConnection).toBe(false);		
 	});

	xit('test can make a connection by dragging',()=> {

		var mockNode: any = createMockNode();
		var mockDraggingConnector: any = {};
		var mockDragOverConnector: any = {};
		var mockEvt: any = {
            view: {
                scrollX: 0,
                scrollY: 0,
            },
        };

		mockScope.chart = createMockChart([mockNode]);

		mockScope.connectorMouseDown(mockEvt, mockScope.chart.nodes[0], mockDraggingConnector, 0, false);

 		mockDragging.config.dragStarted(0, 0, mockEvt);
 		mockDragging.config.dragging(0, 0, mockEvt);

 		// Fake out the mouse over connector.
 		mockScope.mouseOverConnector = mockDragOverConnector;

 		mockDragging.config.dragEnded();

 		expect(mockScope.chart.createNewConnection).toHaveBeenCalledWith(mockDraggingConnector, mockDragOverConnector);
 	});

	xit('test connection creation by dragging is cancelled when dragged over invalid connector', ()=> {

		var mockNode: any = createMockNode();
		var mockDraggingConnector: any = {};
		var mockEvt: any = {
			view: {
				scrollX: 0,
				scrollY: 0,
			},
		};

		mockScope.chart = createMockChart([mockNode]);

		mockScope.connectorMouseDown(mockEvt, mockScope.chart.nodes[0], mockDraggingConnector, 0, false);

 		mockDragging.config.dragStarted(0, 0, mockEvt);
 		mockDragging.config.dragging(0, 0, mockEvt);

 		// Fake out the invalid connector.
 		mockScope.mouseOverConnector = null;

 		mockDragging.config.dragEnded();

 		expect(mockScope.chart.createNewConnection).not.toHaveBeenCalled();
 	});

 	it('mouse move over connection caches the connection', ()=> {

 		var mockElement: any = {};
 		var mockConnection: any = {};
 		var mockConnectionScope: any = {
 			connection: mockConnection
 		};
 		var mockEvent: any = {};

 		//
 		// Fake out the function that check if a connection has been hit.
 		//
 		testObject.checkForHit = (element: any, whichClass: any)=> {
 			if (whichClass === testObject.connectionClass) {
 				return mockConnectionScope;
 			}

 			return null;
 		};

 		testObject.hitTest =  ()=> {
 			return mockElement;
 		};

 		mockScope.mouseMove(mockEvent);

 		expect(mockScope.mouseOverConnection).toBe(mockConnection);
 	});

 	it('test mouse over connection clears mouse over connector and node',  ()=> {

		var mockElement: any = {};
 		var mockConnection: any = {};
 		var mockConnectionScope: any = {
 			connection: mockConnection
 		};
 		var mockEvent: any = {};

 		//
 		// Fake out the function that check if a connection has been hit.
 		//
 		testObject.checkForHit = (element: any, whichClass: any)=> {
 			if (whichClass === testObject.connectionClass) {
 				return mockConnectionScope;
 			}

 			return null;
 		};

 		testObject.hitTest = ()=> {
 			return mockElement;
 		};


 		mockScope.mouseOverConnector = {};
 		mockScope.mouseOverNode = {};

 		mockScope.mouseMove(mockEvent);

 		expect(mockScope.mouseOverConnector).toBe(null);
 		expect(mockScope.mouseOverNode).toBe(null);
 	});

 	it('test mouseMove handles mouse over connector', ()=> {

		var mockElement: any = {};
 		var mockConnector: any = {};
 		var mockConnectorScope: any = {
 			connector: mockConnector
 		};
 		var mockEvent: any = {};

 		//
 		// Fake out the function that check if a connector has been hit.
 		//
 		testObject.checkForHit =  (element: any, whichClass: any)=> {
 			if (whichClass === testObject.connectorClass) {
 				return mockConnectorScope;
 			}

 			return null;
 		};

 		testObject.hitTest = ()=> {
 			return mockElement;
 		};

 		mockScope.mouseMove(mockEvent);

 		expect(mockScope.mouseOverConnector).toBe(mockConnector);
 	});

 	it('test mouseMove handles mouse over node', ()=> {

		var mockElement: any = {};
 		var mockNode: any = {};
 		var mockNodeScope: any = {
 			node: mockNode
 		};
 		var mockEvent: any = {};

 		//
 		// Fake out the function that check if a connector has been hit.
 		//
 		testObject.checkForHit = (element: any, whichClass: any)=>{
 			if (whichClass === testObject.nodeClass) {
 				return mockNodeScope;
 			}

 			return null;
 		};

 		testObject.hitTest = ()=> {
 			return mockElement;
 		};

 		mockScope.mouseMove(mockEvent);

 		expect(mockScope.mouseOverNode).toBe(mockNode);
 	});
});
