package com.thinkbiganalytics.nifi.rest.client.layout;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.PositionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Align Nifi Components under a supplied ProcessGroupId
 */
public class AlignProcessGroupComponents {

    private static final Logger log = LoggerFactory.getLogger(AlignProcessGroupComponents.class);


    NiFiRestClient niFiRestClient;

    /**
     * Map storing the various LayoutGroups computed for the ProcessGroups within the supplied {@code parentProcessGroupId}
     */
    Map<String, LayoutGroup> layoutGroups = new HashMap<>();
    /**
     * The ProcessGroup to inspect
     */
    private String parentProcessGroupId;


    private String processGroupName;

    /**
     * Internal counter as to the number of {@code LayoutGroup}s created
     */
    private Integer groupNumber = 0;
    /**
     * Configuration as to the height, padding for the various components
     */
    private AlignComponentsConfig alignmentConfig;

    /**
     * The Group matching the supplied {@code parentProcessGroupId}
     */
    private ProcessGroupDTO parentProcessGroup;

    /**
     * map of all the groups under the supplied parent
     */
    private Map<String, ProcessGroupDTO> processGroupDTOMap;

    /**
     * map of all the outputs under the supplied parent
     */
    private Map<String, PortDTO> outputPortMap = new HashMap<>();

    /**
     * map of all the inputs under the supplied parent
     */
    private Map<String, PortDTO> inputPortMap = new HashMap<>();


    /**
     * Map of the processGroupId to object with connections
     */
    private Map<String, ProcessGroupAndConnections> processGroupWithConnectionsMap = new HashMap<>();


    /**
     * Flag to indicate when Alignment is complete
     */
    private boolean aligned = false;

    /**
     * Pointer to the last Positioned LayoutGroup
     */
    private LayoutGroup lastPositionedGroup;


    Map<String, Set<PortDTO>> groupIdToOutputPorts = new HashMap<>();

    Map<String, Set<PortDTO>> groupIdToInputPorts = new HashMap<>();


    private List<ConnectedProcessGroup> connectedProcessGroupsLayouts = new ArrayList<>();


    private List<LayoutOrder> connectedProcessGroups;

    public AlignProcessGroupComponents(NiFiRestClient niFiRestClient, String parentProcessGroupId, AlignComponentsConfig alignmentConfig) {
        this.niFiRestClient = niFiRestClient;
        this.parentProcessGroupId = parentProcessGroupId;
        this.alignmentConfig = alignmentConfig;
    }

    public AlignProcessGroupComponents(NiFiRestClient niFiRestClient, String parentProcessGroupId) {
        this(niFiRestClient, parentProcessGroupId, new AlignComponentsConfig());
    }

    /**
     * For the passed in {@code parentProcessGroupId} it will group the items and then apply different
     */
    public ProcessGroupDTO autoLayout() {
        try {
            groupItems();
            //organize each group of items on the screen
            if (TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME.equalsIgnoreCase(processGroupName)) {
                autoLayoutReusableFlows();
            } else {
                layoutGroups.entrySet().stream().sorted(Map.Entry.<String, LayoutGroup>comparingByKey()).forEachOrdered(entry -> arrangeProcessGroup(entry.getValue()));
            }
            aligned = true;
        } catch (Exception e) {
            log.error("Error Aligning items in Process Group {}. {}", parentProcessGroupId, e.getMessage());
        }
        return parentProcessGroup;
    }


    public ProcessGroupDTO autoLayoutReusableFlows() {
        try {
            //render the items that dont have destinations linking to other process groups
            List<ProcessGroupToProcessGroup>
                processGroupToProcessGroupList =
                layoutGroups.values().stream().filter(group -> (group instanceof ProcessGroupToProcessGroup)).map(g -> (ProcessGroupToProcessGroup) g).collect(Collectors.toList());

            Set<String> sourceConnectedProcessGroupIds = processGroupToProcessGroupList.stream().flatMap(s -> s.getSources().stream()).map(s -> s.getId()).collect(Collectors.toSet());
            Set<String> destConnectedProcessGroupIds = processGroupToProcessGroupList.stream().flatMap(s -> s.getDestinations().stream()).map(s -> s.getId()).collect(Collectors.toSet());
            Set<String> connectedGroups = new HashSet<>();
            connectedGroups.addAll(sourceConnectedProcessGroupIds);
            connectedGroups.addAll(destConnectedProcessGroupIds);

            List<InputPortToProcessGroup>
                inputPortToProcessGroup = layoutGroups.values().stream().filter(group -> (group instanceof InputPortToProcessGroup)).map(g -> (InputPortToProcessGroup) g).collect(Collectors.toList());

            Set<LayoutGroup>
                isolatedGroups =
                inputPortToProcessGroup.stream().filter(i -> i.getProcessGroupDTOs().stream().allMatch(g -> !connectedGroups.contains(g.getId()))).collect(Collectors.toSet());

            isolatedGroups.forEach(g -> arrangeProcessGroup(g));

            int counter = 0;
            for (ConnectedProcessGroup layoutGroup : connectedProcessGroupsLayouts) {
                Double start = lastPositionedGroup == null ? 0.0d : lastPositionedGroup.getBottomY();
                if (counter > 0) {
                    start += alignmentConfig.getGroupPadding();
                }
                layoutGroup.setTopAndBottom(start, new Double(layoutGroup.getHeight() + start));
                layoutGroup.setGroupNumber(groupNumber++);
                layoutGroup.render(start);
                lastPositionedGroup = layoutGroup;
                lastPositionedGroup.setTopAndBottom(start, layoutGroup.getyValue());
            }

            List<ProcessGroupWithoutConnections>
                processGroupWithoutConnections =
                layoutGroups.values().stream().filter(group -> (group instanceof ProcessGroupWithoutConnections)).map(g -> (ProcessGroupWithoutConnections) g).collect(Collectors.toList());
            processGroupWithoutConnections.forEach(g -> arrangeProcessGroup(g));

            aligned = true;
        } catch (Exception e) {
            log.error("Error Aligning items in Process Group {}. {}", parentProcessGroupId, e.getMessage());
        }
        return parentProcessGroup;
    }


    /**
     * Group the items together into different {@code LayoutGroup} based upon connections from the ProcessGroup to its various ports.
     */
    public Map<String, LayoutGroup> groupItems() {
        layoutGroups = new HashMap<>();
        //find the parent and children
        if (parentProcessGroupId == "root") {
            parentProcessGroup = niFiRestClient.processGroups().findRoot();
        } else {
            parentProcessGroup = niFiRestClient.processGroups().findById(parentProcessGroupId, false, true).orElse(null);
        }
        final Set<ProcessGroupDTO> children = parentProcessGroup.getContents().getProcessGroups();
        processGroupName = parentProcessGroup.getName();
        processGroupDTOMap = new HashMap<>();
        children.stream().forEach(group -> processGroupDTOMap.put(group.getId(), group));
        children.stream().forEach(group -> processGroupWithConnectionsMap.put(group.getId(), new ProcessGroupAndConnections(group)));

        parentProcessGroup.getContents().getOutputPorts().stream().forEach(portDTO -> outputPortMap.put(portDTO.getId(), portDTO));

        parentProcessGroup.getContents().getInputPorts().stream().forEach(portDTO -> inputPortMap.put(portDTO.getId(), portDTO));

        //map any ports to processgroups
        //group the items by their respective output ports
        createLayoutGroups();
        return layoutGroups;

    }

    /**
     * Based upon the LayoutGroup apply the correct Rendering technique to layout the components
     */
    private void arrangeProcessGroup(LayoutGroup layoutGroup) {

        log.debug("Arrange Group {}", layoutGroup.getClass().getSimpleName());
        //set the starting Y coords for this group
        Double start = lastPositionedGroup == null ? 0.0d : lastPositionedGroup.getBottomY() + alignmentConfig.getGroupPadding();
        layoutGroup.setTopAndBottom(start, new Double(layoutGroup.getHeight() + start));
        layoutGroup.setGroupNumber(groupNumber++);

        if (layoutGroup instanceof InputPortToProcessGroup) {
            arrangeInputPortToProcessGroupLayout((InputPortToProcessGroup) layoutGroup);
        } else if (layoutGroup instanceof ProcessGroupToOutputPort) {
            arrangeProcessGroupToOutputPortLayout((ProcessGroupToOutputPort) layoutGroup);
        } else if (layoutGroup instanceof ProcessGroupToProcessGroup) {
            arrangeProcessGroupLayout((ProcessGroupToProcessGroup) layoutGroup);
        } else if (layoutGroup instanceof ProcessGroupWithoutConnections) {
            arrangeProcessGroupWithoutConnectionsLayout((ProcessGroupWithoutConnections) layoutGroup);
        } else if (layoutGroup instanceof ConnectedProcessGroup) {

            ConnectedProcessGroup connectedProcessGroup = (ConnectedProcessGroup) layoutGroup;
            connectedProcessGroup.render(start);
        }

        lastPositionedGroup = layoutGroup;

    }

    public Map<String, ProcessGroupAndConnections> getProcessGroupWithConnectionsMap() {
        return processGroupWithConnectionsMap;
    }

    public boolean isAligned() {
        return aligned;
    }

    private void arrangeProcessGroupWithoutConnectionsLayout(ProcessGroupWithoutConnections layout) {
        defaultProcessGroupLayoutArrangement(layout);
    }

    /**
     * Arrange top and bottom
     */
    private void arrangeProcessGroupLayout(ProcessGroupToProcessGroup layout) {

        //ClockRenderer clock = new ClockRenderer(layoutGroups,alignmentConfig);
        //arrange the dest in the center
        TopBottomRowsRenderer topBottomRowsRenderer = new TopBottomRowsRenderer(layout, alignmentConfig);

        SingleRowRenderer rowRenderer = new SingleRowRenderer(layout, alignmentConfig, layout.getMiddleY());
        alignProcessGroups(layout.getDestinations(), rowRenderer);

        alignProcessGroups(layout.getProcessGroupDTOs(), topBottomRowsRenderer);

    }

    private void arrangeProcessGroupToOutputPortLayout(ProcessGroupToOutputPort layout) {

        if (!layout.getPorts().isEmpty()) {
            Integer outputPortCount = layout.getPorts().size();

            ColumnRenderer columnRenderer = new ColumnRenderer(layout, alignmentConfig, (alignmentConfig.getCenterX() - (alignmentConfig.getPortWidth() / 2)), outputPortCount);
            columnRenderer
                .updateHeight((columnRenderer.getItemCount() * alignmentConfig.getPortHeight() + alignmentConfig.getProcessGroupHeight() + (2 * alignmentConfig.getProcessGroupPaddingTopBottom())));
            alignOutputPorts(layout, columnRenderer);
        }
        SingleRowRenderer rowRenderer = new SingleRowRenderer(layout, alignmentConfig, layout.getMiddleY(alignmentConfig.getProcessGroupHeight() / 2));
        alignProcessGroups(layout.getProcessGroupDTOs(), rowRenderer);

    }

    private void arrangeInputPortToProcessGroupLayout(InputPortToProcessGroup layout) {

        if (!layout.getPorts().isEmpty()) {
            Integer outputPortCount = layout.getPorts().size();

            ColumnRenderer columnRenderer = new ColumnRenderer(layout, alignmentConfig, (alignmentConfig.getCenterX() - (alignmentConfig.getPortWidth() / 2)), outputPortCount);
            columnRenderer
                .updateHeight((columnRenderer.getItemCount() * alignmentConfig.getPortHeight() + alignmentConfig.getProcessGroupHeight() + (2 * alignmentConfig.getProcessGroupPaddingTopBottom())));
            alignInputPorts(layout, columnRenderer);
        }
        SingleRowRenderer rowRenderer = new SingleRowRenderer(layout, alignmentConfig, layout.getMiddleY(alignmentConfig.getProcessGroupHeight() / 2));
        alignProcessGroups(layout.getProcessGroupDTOs(), rowRenderer);

    }


    private void alignOutputPorts(ProcessGroupToOutputPort layoutGroup, AbstractRenderer renderer) {
        layoutGroup.getPorts().values().stream().forEach(port -> {
            PortDTO positionPort = new PortDTO();
            positionPort.setId(port.getId());
            PositionDTO lastPosition = renderer.getLastPosition();
            PositionDTO newPosition = renderer.getNextPosition(lastPosition);
            positionPort.setPosition(newPosition);
            niFiRestClient.ports().updateOutputPort(parentProcessGroupId, positionPort);
            log.debug("Aligned Port {} at {},{}", port.getName(), positionPort.getPosition().getX(), positionPort.getPosition().getY());
        });
    }

    private void alignInputPorts(InputPortToProcessGroup layoutGroup, AbstractRenderer renderer) {
        alignPorts(layoutGroup.getPorts().values(), renderer);
    }

    private void alignPorts(Collection<PortDTO> ports, AbstractRenderer renderer) {
        ports.stream().forEach(port -> {
            PortDTO positionPort = new PortDTO();
            positionPort.setId(port.getId());
            PositionDTO lastPosition = renderer.getLastPosition();
            PositionDTO newPosition = renderer.getNextPosition(lastPosition);
            positionPort.setPosition(newPosition);
            niFiRestClient.ports().updateInputPort(parentProcessGroupId, positionPort);
            log.debug("Aligned Port {} at {},{}", port.getName(), positionPort.getPosition().getX(), positionPort.getPosition().getY());
        });
    }


    private void alignProcessGroups(Collection<ProcessGroupDTO> processGroups, AbstractRenderer renderer) {
        processGroups.stream().forEach(processGroupDTO -> {
            ProcessGroupDTO positionProcessGroup = new ProcessGroupDTO();
            positionProcessGroup.setId(processGroupDTO.getId());
            PositionDTO lastPosition = renderer.getLastPosition();
            PositionDTO newPosition = renderer.getNextPosition(lastPosition);
            positionProcessGroup.setPosition(newPosition);
            niFiRestClient.processGroups().update(positionProcessGroup);
            log.debug("Aligned ProcessGroup {} at {},{}", processGroupDTO.getName(), positionProcessGroup.getPosition().getX(), positionProcessGroup.getPosition().getY());
        });
    }


    private void defaultProcessGroupLayoutArrangement(LayoutGroup layoutGroup) {
        SingleRowRenderer rowRenderer = new SingleRowRenderer(layoutGroup, alignmentConfig, layoutGroup.getMiddleY(alignmentConfig.getProcessGroupHeight() / 2));
        alignProcessGroups(layoutGroup.getProcessGroupDTOs(), rowRenderer);
    }


    private boolean isGroupToGroupConnection(ConnectionDTO connectionDTO) {
        return (processGroupDTOMap.containsKey(connectionDTO.getDestination().getGroupId()) && processGroupDTOMap.containsKey(connectionDTO.getSource().getGroupId()));
    }

    private boolean isOutputPortToGroupConnection(ConnectionDTO connectionDTO) {
        return (outputPortMap.containsKey(connectionDTO.getDestination().getId()) || outputPortMap.containsKey(connectionDTO.getSource().getId()))
               && (processGroupDTOMap.containsKey(connectionDTO.getDestination().getGroupId()) || processGroupDTOMap.containsKey(connectionDTO.getSource().getGroupId()));
    }

    private boolean isInputPortToGroupConnection(ConnectionDTO connectionDTO) {
        return (inputPortMap.containsKey(connectionDTO.getDestination().getId()) || inputPortMap.containsKey(connectionDTO.getSource().getId()))
               && (processGroupDTOMap.containsKey(connectionDTO.getDestination().getGroupId()) || processGroupDTOMap.containsKey(connectionDTO.getSource().getGroupId()));
    }

    /**
     * Group the items together to create the various LayoutGroups needed for different Rendering
     */
    private void createLayoutGroups() {
        Map<String, Set<ProcessGroupDTO>> outputPortIdToGroup = new HashMap<String, Set<ProcessGroupDTO>>();
        groupIdToOutputPorts = new HashMap<>();

        Map<String, Set<ProcessGroupDTO>> inputPortIdToGroup = new HashMap<String, Set<ProcessGroupDTO>>();
        groupIdToInputPorts = new HashMap<>();

        Map<String, Set<String>> groupIdToGroup = new HashMap<>();

        List<ProcessGroupDTO> connectedGroups = new LinkedList<>();

        parentProcessGroup.getContents().getConnections().stream().filter(
            connectionDTO -> (isOutputPortToGroupConnection(connectionDTO) || isGroupToGroupConnection(connectionDTO) || isInputPortToGroupConnection(connectionDTO)))
            .forEach(connectionDTO -> {
                PortDTO
                    outputPort =
                    outputPortMap.get(connectionDTO.getDestination().getId()) == null ? outputPortMap.get(connectionDTO.getSource().getId())
                                                                                      : outputPortMap.get(connectionDTO.getDestination().getId());

                PortDTO
                    inputPort =
                    inputPortMap.get(connectionDTO.getSource().getId()) == null ? inputPortMap.get(connectionDTO.getDestination().getId())
                                                                                : inputPortMap.get(connectionDTO.getSource().getId());

                ProcessGroupDTO
                    destinationGroup =
                    processGroupDTOMap.get(connectionDTO.getDestination().getGroupId());

                ProcessGroupDTO
                    sourceGroup =
                    processGroupDTOMap.get(connectionDTO.getSource().getGroupId());

                if (outputPort != null) {
                    ProcessGroupDTO processGroup = destinationGroup == null ? sourceGroup : destinationGroup;
                    outputPortIdToGroup.computeIfAbsent(outputPort.getId(), (key) -> new HashSet<ProcessGroupDTO>()).add(processGroup);
                    groupIdToOutputPorts.computeIfAbsent(processGroup.getId(), (key) -> new HashSet<PortDTO>()).add(outputPort);
                    if (processGroupWithConnectionsMap.containsKey(processGroup.getId())) {
                        processGroupWithConnectionsMap.get(processGroup.getId()).addConnection(connectionDTO).addPort(outputPort);
                    }
                }
                if (inputPort != null) {
                    ProcessGroupDTO processGroup = destinationGroup == null ? sourceGroup : destinationGroup;
                    inputPortIdToGroup.computeIfAbsent(inputPort.getId(), (key) -> new HashSet<ProcessGroupDTO>()).add(processGroup);
                    groupIdToInputPorts.computeIfAbsent(processGroup.getId(), (key) -> new HashSet<PortDTO>()).add(inputPort);
                    if (processGroupWithConnectionsMap.containsKey(processGroup.getId())) {
                        processGroupWithConnectionsMap.get(processGroup.getId()).addConnection(connectionDTO).addPort(outputPort);
                    }
                } else if (destinationGroup != null && sourceGroup != null) {
                    groupIdToGroup.computeIfAbsent(sourceGroup.getId(), (key) -> new HashSet<String>()).add(destinationGroup.getId());
                }

            });

        // group port connections together
        groupIdToOutputPorts.entrySet().stream().forEach(entry -> {
            String processGroupId = entry.getKey();
            String portKey = entry.getValue().stream().map(portDTO -> portDTO.getId()).sorted().collect(Collectors.joining(","));
            portKey = "AAA" + portKey;
            layoutGroups.computeIfAbsent(portKey, (key) -> new ProcessGroupToOutputPort(entry.getValue())).add(processGroupDTOMap.get(processGroupId));
        });

        // group port connections together
        groupIdToInputPorts.entrySet().stream().forEach(entry -> {
            String processGroupId = entry.getKey();
            String portKey = entry.getValue().stream().map(portDTO -> portDTO.getId()).sorted().collect(Collectors.joining(","));
            portKey = "BBB" + portKey;
            layoutGroups.computeIfAbsent(portKey, (key) -> new InputPortToProcessGroup(entry.getValue())).add(processGroupDTOMap.get(processGroupId));
        });

        groupIdToGroup.entrySet().stream().forEach(entry -> {
            String sourceGroupId = entry.getKey();
            String processGroupKey = entry.getValue().stream().sorted().collect(Collectors.joining(","));
            processGroupKey = "CCC" + processGroupKey;

            layoutGroups.computeIfAbsent(processGroupKey, (key) -> new ProcessGroupToProcessGroup(entry.getValue())).add(processGroupDTOMap.get(entry.getKey()));
        });

        //add in any groups that dont have connections to ports
        processGroupDTOMap.values().stream().filter(
            processGroupDTO -> !groupIdToGroup.values().stream().flatMap(set -> set.stream()).collect(Collectors.toSet()).contains(processGroupDTO.getId()) && !groupIdToInputPorts
                .containsKey(processGroupDTO.getId()) && !groupIdToOutputPorts.containsKey(processGroupDTO.getId()) && !groupIdToGroup.containsKey(processGroupDTO.getId()))
            .forEach(group -> {
                layoutGroups.computeIfAbsent("NO_PORTS", (key) -> new ProcessGroupWithoutConnections()).add(group);
            });

        // identify the sequence of processgroups if they are connected to each other

        List<String> startingProcessorIds = groupIdToGroup.keySet().stream().filter(id -> !groupIdToGroup.values().stream().anyMatch(ids -> ids.contains(id))).collect(Collectors.toList());

        connectedProcessGroups = new ArrayList<>();
        //start with these and attempt to create flows
        startingProcessorIds.forEach(id -> {
            LayoutOrder layoutOrder = new LayoutOrder(0, 0, processGroupDTOMap.get(id), new LinkedList<>());
            addPorts(layoutOrder, id);
            connectedProcessGroups.add(layoutOrder);
            buildLayoutOrder(layoutOrder, processGroupDTOMap.get(id), groupIdToGroup);
            ConnectedProcessGroup connectedProcessGroup = new ConnectedProcessGroup(layoutOrder);
            connectedProcessGroupsLayouts.add(connectedProcessGroup);
        });


    }

    private void addPorts(LayoutOrder nextOrder, String processGroupId) {
        Set<PortDTO> outputPorts = groupIdToOutputPorts.get(processGroupId);
        Set<PortDTO> inputPorts = groupIdToInputPorts.get(processGroupId);
        if (outputPorts != null) {
            outputPorts.stream().forEach(p -> nextOrder.addPort(p));
        }
        if (inputPorts != null) {
            inputPorts.stream().forEach(p -> nextOrder.addPort(p));
        }
    }

    private Set<String> processedProcessGroups = new HashSet<>();

    private void buildLayoutOrder(LayoutOrder layoutOrder, ProcessGroupDTO group, Map<String, Set<String>> groupIdToGroup) {
        Set<String> next = groupIdToGroup.get(group.getId());
        if (next != null) {
            int nextLevel = layoutOrder.getLevel() + 1;
            List<ProcessGroupDTO> levelGroups = new LinkedList<>();
            next.forEach(dest -> {
                if (!processedProcessGroups.contains(dest)) {
                    processedProcessGroups.add(dest);
                    int order = 0;
                    ProcessGroupDTO groupDTO = processGroupDTOMap.get(dest);
                    levelGroups.add(groupDTO);
                    LayoutOrder nextOrder = new LayoutOrder(nextLevel, order, groupDTO, levelGroups);
                    addPorts(nextOrder, dest);
                    layoutOrder.addNext(nextOrder);
                    order++;
                    buildLayoutOrder(nextOrder, groupDTO, groupIdToGroup);
                }
            });
        }
    }


    public class LayoutOrder {

        private int level;
        private int order;
        private String processGroupId;
        private ProcessGroupDTO processGroupDTO;
        private List<ProcessGroupDTO> levelGroups;
        private Set<PortDTO> ports;
        private List<LayoutOrder> next;
        private List<LayoutOrder> previous;

        public LayoutOrder(int level, int order, ProcessGroupDTO processGroup, List<ProcessGroupDTO> levelGroups) {
            this.level = level;
            this.order = order;
            this.processGroupDTO = processGroup;
            this.processGroupId = processGroup.getId();
            this.levelGroups = levelGroups;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getProcessGroupId() {
            return processGroupId;
        }

        public List<LayoutOrder> getNext() {
            if (next == null) {
                next = new LinkedList<>();
            }
            return next;
        }

        public void addNext(LayoutOrder next) {
            getNext().add(next);
            next.addPrevious(this);
        }

        public void addPrevious(LayoutOrder next) {
            getPrevious().add(next);
        }

        public void setNext(List<LayoutOrder> next) {
            this.next = next;
        }

        public List<LayoutOrder> getPrevious() {
            if (previous == null) {
                previous = new LinkedList<>();
            }
            return previous;
        }


        public ProcessGroupDTO getProcessGroupDTO() {
            return processGroupDTO;
        }


        public Set<PortDTO> getPorts() {
            if (ports == null) {
                ports = new HashSet<>();
            }
            return ports;
        }

        public void setPorts(Set<PortDTO> ports) {
            this.ports = ports;
        }

        public void addPort(PortDTO portDTO) {
            getPorts().add(portDTO);
        }

        public Integer getLevelSize() {
            return layoutGroups != null && !levelGroups.isEmpty() ? levelGroups.size() : 1;
        }

        public Integer getMaxProcessGroupsAtLevel() {
            int count = levelGroups.size();
            if (getNext() != null) {
                for (LayoutOrder l : getNext()) {
                    count = l.getMaxProcessGroupsAtLevel() > count ? l.getMaxProcessGroupsAtLevel() : count;
                }
            }
            return count;

        }

        public Integer getProcessGroupCount() {
            int count = getMaxProcessGroupsAtLevel();
            int currentLevel = getLevel();
            if (getNext() != null) {
                for (LayoutOrder l : getNext()) {
                    if (currentLevel != l.getLevel()) {
                        count += l.getProcessGroupCount();
                    }
                    currentLevel = l.getLevel();
                }
            }
            return count;

        }
    }


    public void setNiFiRestClient(NiFiRestClient niFiRestClient) {
        this.niFiRestClient = niFiRestClient;
    }


    /**
     * Layout where a ProcessGroup has no Connections
     */
    public class ProcessGroupWithoutConnections extends LayoutGroup {

        public ProcessGroupWithoutConnections() {

        }

        public Integer calculateHeight() {
            return (alignmentConfig.getProcessGroupHeight()) + (alignmentConfig.getProcessGroupPaddingTopBottom());
        }

    }

    /**
     * Layout Group where a ProcessGroup is connected directly to another ProcessGroup
     */
    public class ConnectedProcessGroup extends LayoutGroup {

        private LayoutOrder layoutOrder;
        private Set<String> renderedProcessGroups = new HashSet<>();

        public ConnectedProcessGroup(LayoutOrder layoutOrder) {
            this.layoutOrder = layoutOrder;
            this.level = 0;
        }

        private Double xValue;

        private Double yValue;


        public Set<ProcessGroupDTO> getSources() {
            return super.getProcessGroupDTOs();
        }

        @Override
        public Integer calculateHeight() {
            return layoutOrder.getProcessGroupCount() * (alignmentConfig.getProcessGroupHeight() + alignmentConfig.getProcessGroupPaddingTopBottom());
        }

        public LayoutOrder getLayoutOrder() {
            return layoutOrder;
        }

        private Integer level = -1;

        public void renderLayout(LayoutOrder layoutOrder) {

            if (level != layoutOrder.getLevel()) {

            }
            //always reset the x value back to the center
            this.xValue = alignmentConfig.getCenterX() - (alignmentConfig.getProcessGroupWidth() / 2);

            if (layoutOrder.getPorts() != null) {
                //render the ports in a column on the same X location
                ColumnRenderer columnRenderer = new ColumnRenderer(this, alignmentConfig, xValue, layoutOrder.getPorts().size());
                columnRenderer.setAlignLastToBottom(false);
                //store the height the same as the process group height
                columnRenderer.setHeight(layoutOrder.getLevelSize() * (alignmentConfig.getProcessGroupHeight() + alignmentConfig.getProcessGroupPaddingTopBottom()));
                //columnRenderer.setHeight(layoutOrder.getPorts().size()*alignmentConfig.getPortHeight());
                columnRenderer.storePosition(xValue, yValue);

                alignPorts(layoutOrder.getPorts(), columnRenderer);
                //increment x over
                xValue = columnRenderer.getLastPosition().getX() + alignmentConfig.getProcessGroupWidth();
            }
            ColumnRenderer processGroupRenderer = new ColumnRenderer(this, alignmentConfig, xValue, layoutOrder.getLevelSize());
            processGroupRenderer.setAlignLastToBottom(false);
            processGroupRenderer.setHeight(layoutOrder.getLevelSize() * (alignmentConfig.getProcessGroupHeight() + alignmentConfig.getProcessGroupPaddingTopBottom()));
            processGroupRenderer.storePosition(xValue, yValue);

            ProcessGroupDTO positionProcessGroup = new ProcessGroupDTO();
            positionProcessGroup.setId(layoutOrder.getProcessGroupDTO().getId());
            PositionDTO lastPosition = processGroupRenderer.getLastPosition();
            PositionDTO newPosition = processGroupRenderer.getNextPosition(lastPosition);
            positionProcessGroup.setPosition(newPosition);
            yValue = newPosition.getY();

            niFiRestClient.processGroups().update(positionProcessGroup);

            log.debug("Aligned ProcessGroup {} at {},{}", layoutOrder.getProcessGroupDTO().getName(), positionProcessGroup.getPosition().getX(), positionProcessGroup.getPosition().getY());
            level = layoutOrder.getLevel();
            renderedProcessGroups.add(layoutOrder.getProcessGroupDTO().getId());

            for (LayoutOrder layoutOrder1 : layoutOrder.getNext()) {
                if (!renderedProcessGroups.contains(layoutOrder1.getProcessGroupDTO().getId())) {
                    renderLayout(layoutOrder1);
                }
            }


        }

        public void render(Double start) {
            this.yValue = start;
            this.xValue = alignmentConfig.getCenterX() - (alignmentConfig.getProcessGroupWidth() / 2);
            this.level = -1;
            renderLayout(this.layoutOrder);
        }

        public Double getyValue() {
            return yValue;
        }
    }


    public class ProcessGroupToProcessGroup extends LayoutGroup {

        private Set<ProcessGroupDTO> destinations = new HashSet<>();


        public ProcessGroupToProcessGroup(Set<String> destinations) {
            this.destinations = destinations.stream().map(groupId -> processGroupDTOMap.get(groupId)).collect(Collectors.toSet());
        }


        public Set<ProcessGroupDTO> getSources() {
            return super.getProcessGroupDTOs();
        }

        @Override
        public Integer calculateHeight() {
            return alignmentConfig.getProcessGroupHeight() + alignmentConfig.getProcessGroupPaddingTopBottom();
        }

        public Set<ProcessGroupDTO> getDestinations() {
            return destinations;
        }
    }


    /**
     * Layout Group where an Input Port is connected to a ProcessGroup
     */
    public class InputPortToProcessGroup extends ProcessGroupToOutputPort {

        public InputPortToProcessGroup(Set<PortDTO> inputPorts) {
            super(inputPorts);
        }
    }

    /**
     * Group where a ProcessGroup is connected to an OutputPort
     */
    public class ProcessGroupToOutputPort extends ProcessGroupToPort {

        public ProcessGroupToOutputPort(Set<PortDTO> outputPorts) {
            super(outputPorts);
        }
    }

    /**
     * LayoutGroup where a Port is connected to a Process Group
     */
    public abstract class ProcessGroupToPort extends LayoutGroup {

        private Map<String, PortDTO> ports = new HashMap<>();


        public ProcessGroupToPort(Set<PortDTO> ports) {
            if (ports != null) {
                ports.stream().forEach(portDTO -> {
                    this.ports.put(portDTO.getId(), portDTO);
                });
            }
        }


        public Integer calculateHeight() {
            Integer portCount = ports.size();
            return (alignmentConfig.getPortHeight() * portCount) + alignmentConfig.getProcessGroupHeight() + (alignmentConfig.getProcessGroupPaddingTopBottom() * portCount);
        }

        public Double getMiddleY() {
            return super.getMiddleY();
        }


        public Map<String, PortDTO> getPorts() {
            return ports;
        }


    }


}
