package com.sebastientr.workflow.dummy.queuing;

import com.sebastientr.workflow.dto.event.EndProcessDTO;
import com.sebastientr.workflow.dto.event.EndTaskDTO;
import com.sebastientr.workflow.dto.event.StartProcessDTO;
import com.sebastientr.workflow.dto.event.StartTaskDTO;
import com.sebastientr.workflow.queuing.WorkflowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DummyConsumer {
    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).START_PROCESS}")
    public void handleStartProcessEvent(WorkflowEvent<StartProcessDTO> event) {
        log.info("Handle {} event", event.getType().name());
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).END_PROCESS}")
    public void handleEndProcessEvent(WorkflowEvent<EndProcessDTO> event) {
        log.info("Handle {} event", event.getType().name());
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).START_TASK}")
    public void handleStartTaskEvent(WorkflowEvent<StartTaskDTO> event) {
        log.info("Handle {} event", event.getType().name());
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).END_TASK}")
    public void handleEndTaskEvent(WorkflowEvent<EndTaskDTO> event) {
        log.info("Handle {} event", event.getType().name());
    }
}
