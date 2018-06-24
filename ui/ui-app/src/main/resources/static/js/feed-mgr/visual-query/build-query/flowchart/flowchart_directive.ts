import * as angular from "angular";
import "./flowchart_viewmodel";
import './mouse_capture_service';
import './svg_class';
import './dragging_service';

export default class FlowChartController{
// Controller for the flowchart directive.
// Having a separate controller is better for unit testing, otherwise
// it is painful to unit test a directive without instantiating the DOM
// (which is possible, just not ideal).
document: any;
jQuery: any;
connectionClass: any;
connectorClass: any;
nodeClass: any;
searchUp: any;
hitTest: any;
checkForHit: any;
translateCoordinates: any;

constructor(private $scope: any,
            private dragging: any,
            private $element: any){
            var controller = this;
            // Reference to the document and jQuery, can be overridden for testting.
            this.document = document;
            // Wrap jQuery so it can easily be  mocked for testing.
            this.jQuery = (element: any)=> {
                return $(element);
            }
            // Init data-model variables.
            $scope.draggingConnection = false;
            $scope.connectorSize = 10;
            $scope.dragSelecting = false;
			/* Can use this to test the drag selection rect.
			 $scope.dragSelectionRect = {
			 x: 0,
			 y: 0,
			 width: 0,
			 height: 0,
			 };
			 */

            // Reference to the connection, connector or node that the mouse is currently over.
            $scope.mouseOverConnector = null;
            $scope.mouseOverConnection = null;
            $scope.mouseOverNode = null;
            // The class for connections and connectors.
            this.connectionClass = 'connection';
            this.connectorClass = 'connector';
            this.nodeClass = 'node';
            // Search up the HTML element tree for an element the requested class.
            this.searchUp = (element: any, parentClass: any)=> {
                // Reached the root.
                if (element == null || element.length == 0) {
                    return null;
                }
                // Check if the element has the class that identifies it as a connector.
                if (hasClassSVG(element, parentClass)) {
                    // Found the connector element.
                    return element;
                }
                // Recursively search parent elements.
                return this.searchUp(element.parent(), parentClass);
            };
            // Hit test and retreive node and connector that was hit at the specified coordinates.
            this.hitTest = (clientX: any, clientY: any)=> {
              // Retreive the element the mouse is currently over.
                return this.document.elementFromPoint(clientX, clientY);
            };
            // Hit test and retreive node and connector that was hit at the specified coordinates.

            this.checkForHit = (mouseOverElement: any, whichClass: any)=>{
                // Find the parent element, if any, that is a connector.
                var hoverElement = this.searchUp(this.jQuery(mouseOverElement), whichClass);
                if (!hoverElement) {
                    return null;
                }
                return hoverElement.scope();
            };
            // Translate the coordinates so they are relative to the svg element.
            this.translateCoordinates = (x: any, y: any, evt: any)=> {
                var svg_elem = $element.get(0);
                var matrix = svg_elem.getScreenCTM();
                var point = svg_elem.createSVGPoint();
                point.x = x - evt.view.pageXOffset;
                point.y = y - evt.view.pageYOffset;
                return point.matrixTransform(matrix.inverse());
            };
            // Called on mouse down in the chart.
            $scope.mouseDown = (evt: any)=> {
                $scope.chart.deselectAll();
                dragging.startDrag(evt, {
                    // Commence dragging... setup variables to display the drag selection rect.
                    dragStarted: (x: any, y: any)=>{
                        $scope.dragSelecting = true;
                        var startPoint = controller.translateCoordinates(x, y, evt);
                        $scope.dragSelectionStartPoint = startPoint;
                        $scope.dragSelectionRect = {
                            x: startPoint.x,
                            y: startPoint.y,
                            width: 0,
                            height: 0,
                        };
                    },

                    //
                    // Update the drag selection rect while dragging continues.
                    //
                    dragging: function (x: any, y: any) {
                        var startPoint: any = $scope.dragSelectionStartPoint;
                        var curPoint: any = controller.translateCoordinates(x, y, evt);
                        $scope.dragSelectionRect = {
                            x: curPoint.x > startPoint.x ? startPoint.x : curPoint.x,
                            y: curPoint.y > startPoint.y ? startPoint.y : curPoint.y,
                            width: curPoint.x > startPoint.x ? curPoint.x - startPoint.x : startPoint.x - curPoint.x,
                            height: curPoint.y > startPoint.y ? curPoint.y - startPoint.y : startPoint.y - curPoint.y,
                        };
                    },

                    //
                    // Dragging has ended... select all that are within the drag selection rect.
                    //
                    dragEnded: function () {
                        $scope.dragSelecting = false;
                        $scope.chart.applySelectionRect($scope.dragSelectionRect);
                        delete $scope.dragSelectionStartPoint;
                        delete $scope.dragSelectionRect;
                    },
                });
            };

            //
            // Called for each mouse move on the svg element.
            //
            $scope.mouseMove = (evt: any)=> {
                // Clear out all cached mouse over elements.
                $scope.mouseOverConnection = null;
                $scope.mouseOverConnector = null;
                $scope.mouseOverNode = null;
                var mouseOverElement = controller.hitTest(evt.clientX, evt.clientY);
                if (mouseOverElement == null) {
                    // Mouse isn't over anything, just clear all.
                    return;
                }
                if (!$scope.draggingConnection) { // Only allow 'connection mouse over' when not dragging out a connection.
                    // Figure out if the mouse is over a connection.
                    var scope: any = controller.checkForHit(mouseOverElement, controller.connectionClass);
                    $scope.mouseOverConnection = (scope && scope.connection) ? scope.connection : null;
                    if ($scope.mouseOverConnection) {
                        // Don't attempt to mouse over anything else.
                        return;
                    }
                }
                // Figure out if the mouse is over a connector.
                var scope: any = controller.checkForHit(mouseOverElement, controller.connectorClass);
                $scope.mouseOverConnector = (scope && scope.connector) ? scope.connector : null;
                if ($scope.mouseOverConnector) {
                    // Don't attempt to mouse over anything else.
                    return;
                }
                // Figure out if the mouse is over a node.
                var scope: any = controller.checkForHit(mouseOverElement, controller.nodeClass);
                $scope.mouseOverNode = (scope && scope.node) ? scope.node : null;
            };
            //
            // Handle mousedown on a node.
            //
            $scope.nodeMouseDown = (evt: any, node: any)=>{
                var chart: any = $scope.chart;
                var lastMouseCoords: any;
                dragging.startDrag(evt, {
                    // Node dragging has commenced.
                    dragStarted: function (x: any, y: any) {
                        lastMouseCoords = controller.translateCoordinates(x, y, evt);
                        // If nothing is selected when dragging starts,
                        // at least select the node we are dragging.
                        if (!node.selected()) {
                            chart.deselectAll();
                            node.select();
                        }
                    },
                    // Dragging selected nodes... update their x,y coordinates.
                    dragging: function (x: any, y: any) {
                        var curCoords = controller.translateCoordinates(x, y, evt);
                        var deltaX = curCoords.x - lastMouseCoords.x;
                        var deltaY = curCoords.y - lastMouseCoords.y;
                        chart.updateSelectedNodesLocation(deltaX, deltaY);
                        lastMouseCoords = curCoords;
                    },
                    // The node wasn't dragged... it was clicked.
                    clicked: function () {
                        chart.handleNodeClicked(node, evt.ctrlKey);
                    },

                });
            };
            // Handle mousedown on a connection.
            $scope.connectionMouseDown = (evt: any, connection: any)=>{
                var chart: any = $scope.chart;
                chart.handleConnectionMouseDown(connection, evt.ctrlKey);
                // Don't let the chart handle the mouse down.
                evt.stopPropagation();
                evt.preventDefault();
            };
            // Handle mousedown on an input connector.
            $scope.connectorMouseDown = (evt: any, node: any, connector: any, connectorIndex: any, isInputConnector: any)=> {
                // Initiate dragging out of a connection.
                dragging.startDrag(evt, {
                    // Called when the mouse has moved greater than the threshold distance
                    // and dragging has commenced.
                    dragStarted: function (x: any, y: any) {
                        var curCoords = controller.translateCoordinates(x, y, evt);
                        $scope.draggingConnection = true;
                        //$scope.dragPoint1 = flowchart.computeConnectorPos(node, connectorIndex, isInputConnector);
                        $scope.dragPoint1 = flowchart.FlowchartModelBase.computeConnectorPos(node, connector);
                        $scope.dragPoint2 = {
                            x: curCoords.x,
                            y: curCoords.y
                        };
                        $scope.dragTangent1 = flowchart.FlowchartModelBase.computeConnectionSourceTangent($scope.dragPoint1, $scope.dragPoint2);
                        $scope.dragTangent2 = flowchart.FlowchartModelBase.computeConnectionDestTangent($scope.dragPoint1, $scope.dragPoint2);
                    },

                    //
                    // Called on mousemove while dragging out a connection.
                    //
                    dragging: function (x: any, y: any, evt: any) {
                        var startCoords = controller.translateCoordinates(x, y, evt);
                        //$scope.dragPoint1 = flowchart.computeConnectorPos(node, connectorIndex, isInputConnector);
                        $scope.dragPoint1 = flowchart.FlowchartModelBase.computeConnectorPos(node, connector);
                        $scope.dragPoint2 = {
                            x: startCoords.x,
                            y: startCoords.y
                        };
                        $scope.dragTangent1 = flowchart.FlowchartModelBase.computeConnectionSourceTangent($scope.dragPoint1, $scope.dragPoint2);
                        $scope.dragTangent2 = flowchart.FlowchartModelBase.computeConnectionDestTangent($scope.dragPoint1, $scope.dragPoint2);
                    },

                    //
                    // Clean up when dragging has finished.
                    //
                    dragEnded: function () {

                        if ($scope.mouseOverConnector &&
                            $scope.mouseOverConnector !== connector) {

                            //
                            // Dragging has ended...
                            // The mouse is over a valid connector...
                            // Create a new connection.
                            //
                            $scope.chart.createNewConnection(connector, $scope.mouseOverConnector, $scope.onCreateConnectionCallback, $scope.onEditConnectionCallback);
                        }

                        $scope.draggingConnection = false;
                        delete $scope.dragPoint1;
                        delete $scope.dragTangent1;
                        delete $scope.dragPoint2;
                        delete $scope.dragTangent2;
                    },

                });
            };
            }
}
//
// Flowchart module.
//
 angular.module('flowChart', ['dragging'])
        //
        // Directive that generates the rendered chart from the data model.
        //
        .directive('flowChart',[()=> {
            return {
                restrict: 'E',
                templateUrl: "js/feed-mgr/visual-query/build-query/flowchart/flowchart_table_template.html",
                replace: true,
                scope: {
                    chart: "=chart",
                    onCreateConnectionCallback: '&?',
                    onEditConnectionCallback: '&?'
                },
                // Controller for the flowchart directive.
                // Having a separate controller is better for unit testing, otherwise
                // it is painful to unit test a directive without instantiating the DOM
                // (which is possible, just not ideal).
                controller: 'FlowChartController'
            };
        }])

        //
        // Directive that allows the chart to be edited as json in a textarea.
        //
        .directive('chartJsonEdit', [()=> {
            return {
                restrict: 'A',
                scope: {
                    viewModel: "="
                },
                link: function (scope: any, elem: any, attr: any) {
                    // Serialize the data model as json and update the textarea.
                    var updateJson = function () {
                        if (scope.viewModel) {
                            var json: any = JSON.stringify(scope.viewModel.data, null, 4);
                            $(elem).val(json);
                        }
                    };
                    // First up, set the initial value of the textarea.
                    //
                    updateJson();
                    //
                    // Watch for changes in the data model and update the textarea whenever necessary.
                    //
                    scope.$watch("viewModel.data", updateJson, true);
                    //
                    // Handle the change event from the textarea and update the data model
                    // from the modified json.
                    //
                    $(elem).bind("input propertychange", function () {
                        var json: any = $(elem).val();
                        var dataModel: any = JSON.parse(json);
                        scope.viewModel = new flowchart.ChartViewModel(dataModel);
                        scope.$digest();
                    });
                }
            }
        }])
    .controller('FlowChartController', ['$scope', 'dragging', '$element', FlowChartController]);

