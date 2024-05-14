package com.sebastientr.workflow.service;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;

import java.util.List;

public interface IWorkflowEngineService {
    /**
     * Start a new process by giving a flow name and an initial context
     * @param flowName flow name
     * @param context initial context
     * @return newly created process entity
     */
    ProcessEntity start(String flowName, ContextEntity context);

    /**
     * Get the process by providing its id
     * @param processId process uuid
     * @return process entity
     */
    ProcessEntity get(String processId);


    /**
     * Update Status of processId
     * @param processId
     * @param processStatus
     * @return
     */
    ProcessEntity updateStatus(String processId, ProcessStatus processStatus);


    /**
     * Get the processes by providing their ids
     * @param processId process uuid list
     * @return process entities
     */
    List<ProcessEntity> get(List<String> processId);

    /**
     * Retry a specific task that has failed and is allow to fail (so to be retried) by providing process id and task id
     * @param processId process id on which retry a task
     * @param taskId task id to retry
     * @return process entity
     */
    ProcessEntity retry(String processId, String taskId);

    /**
     * Get all flows definition
     * @return a list of flows
     */
    List<FlowEntity> getFlows();

    /**
     * Get a flow definition by its name
     * @param flowName flow name
     * @return a flow entity
     */
    FlowEntity getFlow(String flowName);
}
