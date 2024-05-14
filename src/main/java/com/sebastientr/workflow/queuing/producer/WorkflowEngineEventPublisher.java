package com.sebastientr.workflow.queuing.producer;

import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.domain.entity.TaskInstanceEntity;
import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.dto.event.EndProcessDTO;
import com.sebastientr.workflow.dto.event.EndTaskDTO;
import com.sebastientr.workflow.dto.event.StartProcessDTO;
import com.sebastientr.workflow.dto.event.StartTaskDTO;
import com.sebastientr.workflow.queuing.WorkflowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowEngineEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkflowEngineEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publish async event
     * @param what message payload
     * @param type message type
     */
    public <T> void publishEvent(final T what, WorkflowEvent.EventType type) {
        log.info("Publishing {} event", type);
        applicationEventPublisher.publishEvent(new WorkflowEvent<>(what, type));
    }

    public void publishStartTaskEvent(FlowTaskEntity flowTaskEntity, ProcessEntity process) {
        publishEvent(new StartTaskDTO(flowTaskEntity, process.getId()), WorkflowEvent.EventType.START_TASK);
    }

    public void publishEndTaskEvent(FlowTaskEntity flowTask, ProcessEntity process, TaskInstanceEntity taskInstance) {
        publishEvent(new EndTaskDTO(flowTask, process.getId(), taskInstance), WorkflowEvent.EventType.END_TASK);
    }

    public void publishStartProcessEvent(FlowEntity flow, ProcessEntity process) {
        publishEvent(new StartProcessDTO(flow, process.getId()), WorkflowEvent.EventType.START_PROCESS);
    }

    public void publishEndProcessEvent(ProcessEntity process) {
        publishEvent(new EndProcessDTO(process.getId()), WorkflowEvent.EventType.END_PROCESS);
    }
}