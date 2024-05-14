package com.sebastientr.workflow.queuing.consumer;

import com.sebastientr.workflow.domain.repository.ProcessRepository;
import com.sebastientr.workflow.dto.event.StartTaskDTO;
import com.sebastientr.workflow.exception.WorkflowEngineInvalidEventException;
import com.sebastientr.workflow.process.WorkflowEngineProcessor;
import com.sebastientr.workflow.queuing.WorkflowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowEngineEventConsumer {
    private final ProcessRepository processRepository;
    private final WorkflowEngineProcessor workflowEngineProcessor;

    public WorkflowEngineEventConsumer(WorkflowEngineProcessor workflowEngineProcessor,
                                       ProcessRepository processRepository) {
        this.workflowEngineProcessor = workflowEngineProcessor;
        this.processRepository = processRepository;
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).START_TASK}")
    public void handleStartTaskEvent(WorkflowEvent<StartTaskDTO> event) throws WorkflowEngineInvalidEventException {
        validateEvent(event);

        log.info("Received {} event", event.getType());

        var process = processRepository.findByIdOrderByTaskInstancesCreatedAt(event.getWhat().getProcessId()).orElseThrow();

        workflowEngineProcessor.execute(event.getWhat().getFlowTask(), process);
    }

    /**
     * Validate integrity of the start task event
     * @param event start task event
     * @throws WorkflowEngineInvalidEventException if the event is malformed
     */
    public static void validateEvent(WorkflowEvent<StartTaskDTO> event) throws WorkflowEngineInvalidEventException {
        if (event == null) {
            throw new WorkflowEngineInvalidEventException("Received null event");
        } else if (event.getType() == null) {
            throw new WorkflowEngineInvalidEventException("Received invalid event");
        } else if (event.getWhat().getFlowTask() == null) {
            throw new WorkflowEngineInvalidEventException("Received event with empty flowTask");
        } else if (event.getWhat().getProcessId() == null) {
            throw new WorkflowEngineInvalidEventException("Received event with empty process");
        }
    }
}
