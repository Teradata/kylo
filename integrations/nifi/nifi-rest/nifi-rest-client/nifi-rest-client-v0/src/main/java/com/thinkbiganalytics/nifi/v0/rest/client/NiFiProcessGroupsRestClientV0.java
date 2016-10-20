package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiComponentState;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.FlowSnippetEntity;
import org.apache.nifi.web.api.entity.InputPortEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.OutputPortEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupsEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Form;

/**
 * Implements a {@link NiFiProcessGroupsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiProcessGroupsRestClientV0 extends AbstractNiFiProcessGroupsRestClient {

    /**
     * Base path for process group requests
     */
    private static final String BASE_PATH = "/controller/process-groups";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiProcessGroupsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiProcessGroupsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public ProcessGroupDTO create(@Nonnull final String parentGroupId, @Nonnull final String name) {
        ProcessGroupEntity entity = new ProcessGroupEntity();
        ProcessGroupDTO group = new ProcessGroupDTO();
        group.setName(name);

        try {
            entity.setProcessGroup(group);
            ProcessGroupEntity
                    returnedGroup =
                    client.post(BASE_PATH + "/" + parentGroupId + "/process-group-references", entity, ProcessGroupEntity.class);
            return returnedGroup.getProcessGroup();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ConnectionDTO createConnection(@Nonnull final String processGroupId, @Nonnull final ConnectableDTO source, @Nonnull final ConnectableDTO dest) {
        try {
            ConnectionDTO connectionDTO = new ConnectionDTO();
            connectionDTO.setSource(source);
            connectionDTO.setDestination(dest);
            connectionDTO.setName(source.getName() + " - " + dest.getName());
            ConnectionEntity connectionEntity = new ConnectionEntity();
            connectionEntity.setConnection(connectionDTO);

            return client.post(BASE_PATH + "/" + processGroupId + "/connections", connectionEntity, ConnectionEntity.class).getConnection();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ControllerServiceDTO createControllerService(@Nonnull final String processGroupId, @Nonnull final ControllerServiceDTO controllerService) {
        throw new UnsupportedOperationException("Scoped controller services not supported in NiFi versions before 1.0.");
    }

    @Nonnull
    @Override
    public PortDTO createInputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort) {
        try {
            final InputPortEntity entity = new InputPortEntity();
            entity.setInputPort(inputPort);

            return client.post(BASE_PATH + "/" + processGroupId + "/input-ports", entity, InputPortEntity.class).getInputPort();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public PortDTO createOutputPort(@Nonnull String processGroupId, @Nonnull PortDTO outputPort) {
        try {
            final OutputPortEntity entity = new OutputPortEntity();
            entity.setOutputPort(outputPort);

            return client.post(BASE_PATH + "/" + processGroupId + "/output-ports", entity, OutputPortEntity.class).getOutputPort();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<ProcessGroupDTO> findAll(@Nonnull final String parentGroupId) {
        try {
            return client.get(BASE_PATH + "/" + parentGroupId + "/process-group-references", null, ProcessGroupsEntity.class).getProcessGroups();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Optional<ProcessGroupDTO> findById(@Nonnull final String processGroupId, final boolean recursive, final boolean verbose) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("recursive", recursive);
            params.put("verbose", verbose);
            return Optional.of(client.get(BASE_PATH + "/" + processGroupId, params, ProcessGroupEntity.class).getProcessGroup());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Set<ConnectionDTO> getConnections(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + "/" + processGroupId + "/connections", null, ConnectionsEntity.class).getConnections();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<ControllerServiceDTO> getControllerServices(@Nonnull final String processGroupId) {
        return Collections.emptySet();  // not supported in NiFi versions before 1.0
    }

    @Nonnull
    @Override
    public Set<PortDTO> getInputPorts(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + "/" + processGroupId + "/input-ports/", null, InputPortsEntity.class).getInputPorts();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<PortDTO> getOutputPorts(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + "/" + processGroupId + "/output-ports", null, OutputPortsEntity.class).getOutputPorts();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public FlowSnippetDTO instantiateTemplate(@Nonnull final String processGroupId, @Nonnull final String templateId) {
        try {
            String originX = "10";
            String originY = "10";
            Form form = new Form();
            form.param("templateId", templateId);
            form.param("originX", originX);
            form.param("originY", originY);

            return client.postForm(BASE_PATH + "/" + processGroupId + "/template-instance", form, FlowSnippetEntity.class).getContents();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Unable to create Template instance for templateId: " + templateId + " under Process group " + processGroupId
                                                     + ".  Unable find the processGroup or template");
        } catch (ClientErrorException e) {
            final String msg = e.getResponse().readEntity(String.class);
            throw new NifiComponentNotFoundException("Unable to create Template instance for templateId: " + templateId + " under Process group " + processGroupId + ". " + msg);
        }
    }

    @Override
    public void schedule(@Nonnull final String processGroupId, @Nonnull final String parentGroupId, @Nonnull final NiFiComponentState state) {
        ProcessGroupEntity entity = new ProcessGroupEntity();
        entity.setProcessGroup(findById(processGroupId, false, false).orElseThrow(() -> new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, null)));
        entity.getProcessGroup().setRunning(state.equals(NiFiComponentState.RUNNING));

        try {
            client.put(BASE_PATH + "/" + parentGroupId + "/process-group-references/" + processGroupId, entity, ProcessGroupEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ProcessGroupDTO update(@Nonnull final ProcessGroupDTO processGroup) {
        try {
            final ProcessGroupEntity entity = new ProcessGroupEntity();
            entity.setProcessGroup(processGroup);

            return client.put(BASE_PATH + "/" + processGroup.getId(), processGroup, ProcessGroupEntity.class).getProcessGroup();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroup.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    protected Optional<ProcessGroupDTO> doDelete(@Nonnull final ProcessGroupDTO processGroup) {
        try {
            final ProcessGroupEntity entity = client.delete(BASE_PATH + "/" + processGroup.getParentGroupId() + "/process-group-references/" + processGroup.getId(), new HashMap<>(),
                                                            ProcessGroupEntity.class);
            return Optional.of(entity.getProcessGroup());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
