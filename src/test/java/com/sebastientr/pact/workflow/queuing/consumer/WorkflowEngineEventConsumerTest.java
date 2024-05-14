package com.sebastientr.workflow.queuing.consumer;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.dto.event.EndProcessDTO;
import com.sebastientr.workflow.dto.event.EndTaskDTO;
import com.sebastientr.workflow.dto.event.StartProcessDTO;
import com.sebastientr.workflow.dto.event.StartTaskDTO;
import com.sebastientr.workflow.dummy.queuing.DummyConsumer;
import com.sebastientr.workflow.exception.WorkflowEngineInvalidEventException;
import com.sebastientr.workflow.exception.WorkflowEngineRuntimeException;
import com.sebastientr.workflow.queuing.WorkflowEvent;
import com.sebastientr.workflow.service.impl.WorkflowEngineService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
class WorkflowEngineEventConsumerTest {
    @Autowired
    private WorkflowEngineService workflowEngineService;

    @SpyBean
    private DummyConsumer dummyConsumer;

    @Test
    void testNullEvent() {
        Assertions.assertThrows(WorkflowEngineInvalidEventException.class, () -> WorkflowEngineEventConsumer.validateEvent(null));
    }

    @Test
    void testNullEventType() {
        WorkflowEvent<StartTaskDTO> event = new WorkflowEvent<>(new StartTaskDTO(null, null), null);
        Assertions.assertThrows(WorkflowEngineInvalidEventException.class, () -> WorkflowEngineEventConsumer.validateEvent(event));
    }

    @Test
    void testNullEventWhat() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new WorkflowEvent<>(null, WorkflowEvent.EventType.START_PROCESS));
    }

    @Test
    void testNullEventFlowTask() {
        WorkflowEvent<StartTaskDTO> event = new WorkflowEvent<>(new StartTaskDTO(null, UUID.randomUUID()), WorkflowEvent.EventType.START_PROCESS);
        Assertions.assertThrows(WorkflowEngineInvalidEventException.class, () -> WorkflowEngineEventConsumer.validateEvent(event));
    }

    @Test
    void testNullEventProcess() {
        WorkflowEvent<StartTaskDTO> event = new WorkflowEvent<>(new StartTaskDTO(new FlowTaskEntity(), null), WorkflowEvent.EventType.START_PROCESS);
        Assertions.assertThrows(WorkflowEngineInvalidEventException.class, () -> WorkflowEngineEventConsumer.validateEvent(event));
    }

    @Test
    void testStartNewProcess() {
        // Create ArgumentCaptor for WorkflowEvent
        ArgumentCaptor<WorkflowEvent<StartProcessDTO>> startProcessEventCaptor = ArgumentCaptor.forClass(WorkflowEvent.class);
        ArgumentCaptor<WorkflowEvent<EndProcessDTO>> endProcessEventCaptor = ArgumentCaptor.forClass(WorkflowEvent.class);        // Create ArgumentCaptor for WorkflowEvent
        ArgumentCaptor<WorkflowEvent<StartTaskDTO>> startTaskEventCaptor = ArgumentCaptor.forClass(WorkflowEvent.class);
        ArgumentCaptor<WorkflowEvent<EndTaskDTO>> endTaskEventCaptor = ArgumentCaptor.forClass(WorkflowEvent.class);

        ContextEntity context = new ContextEntity();

        ProcessEntity process = workflowEngineService.start("test-flow", context);

        Assertions.assertNotNull(process);

        sleep(1);

        verify(dummyConsumer, times(1)).handleStartProcessEvent(startProcessEventCaptor.capture());
        verify(dummyConsumer, times(1)).handleEndProcessEvent(endProcessEventCaptor.capture());
        verify(dummyConsumer, times(3)).handleStartTaskEvent(startTaskEventCaptor.capture());
        verify(dummyConsumer, times(3)).handleEndTaskEvent(endTaskEventCaptor.capture());

        // Assert the content of the captured events
        WorkflowEvent<StartProcessDTO> capturedStartProcessEvent = startProcessEventCaptor.getValue();
        WorkflowEvent<EndProcessDTO> capturedEndProcessEvent = endProcessEventCaptor.getValue();
        WorkflowEvent<StartTaskDTO> capturedStartTaskEvent = startTaskEventCaptor.getValue();
        WorkflowEvent<EndTaskDTO> capturedEndTaskEvent = endTaskEventCaptor.getValue();

        Assertions.assertNotNull(capturedStartProcessEvent);
        Assertions.assertNotNull(capturedEndProcessEvent);
        Assertions.assertNotNull(capturedStartTaskEvent);
        Assertions.assertNotNull(capturedEndTaskEvent);

        Assertions.assertNotNull(capturedStartProcessEvent.getWhat());
        Assertions.assertNotNull(capturedEndProcessEvent.getWhat());
        Assertions.assertNotNull(capturedStartTaskEvent.getWhat());
        Assertions.assertNotNull(capturedEndTaskEvent.getWhat());

        Assertions.assertNotNull(capturedStartProcessEvent.getWhat().getProcessId());
        Assertions.assertNotNull(capturedEndProcessEvent.getWhat().getProcessId());
        Assertions.assertNotNull(capturedStartTaskEvent.getWhat().getProcessId());
        Assertions.assertNotNull(capturedEndTaskEvent.getWhat().getProcessId());

        Assertions.assertNotNull(capturedStartProcessEvent.getWhat().getFlow());

        Assertions.assertNotNull(capturedEndTaskEvent.getWhat().getProcessId());
        Assertions.assertNotNull(capturedEndTaskEvent.getWhat().getFlowTask());
        Assertions.assertNotNull(capturedEndTaskEvent.getWhat().getTaskInstance());
    }

    protected void sleep(long sec) {
        log.debug("Sleeping {} seconds", sec);
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkflowEngineRuntimeException("Interruption during sleep");
        }
    }
}