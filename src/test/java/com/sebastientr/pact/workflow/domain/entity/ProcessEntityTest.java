package com.sebastientr.workflow.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sebastientr.workflow.domain.entity.core.TaskEntity;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;
import com.sebastientr.workflow.dto.enumeration.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.UUID;

class ProcessEntityTest {
    @InjectMocks
    private ContextEntity contextEntity;

    @InjectMocks
    private ProcessEntity processEntity;

    @InjectMocks
    private TaskInstanceEntity taskInstanceEntity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testContextEntity() {
        // Create ContextEntity
        contextEntity.setId(UUID.randomUUID());
        contextEntity.setProcess(processEntity);

        contextEntity.setCreatedAt(LocalDateTime.now());
        contextEntity.setUpdatedAt(LocalDateTime.now());
        contextEntity.setCreatedBy("Someone");
        contextEntity.setModifiedBy("Somebody");

        // Test ContextEntity
        assertNotNull(contextEntity.getId());
        assertNotNull(contextEntity.getProcess());
        assertNotNull(contextEntity.getCreatedAt());
        assertNotNull(contextEntity.getUpdatedAt());
        assertNotNull(contextEntity.getCreatedBy());
        assertNotNull(contextEntity.getModifiedBy());
    }

    @Test
    void testProcessEntity() {
        // Create ContextEntity
        ContextEntity context = new ContextEntity();
        context.setId(UUID.randomUUID());

        // Create ProcessEntity
        processEntity.setId(UUID.randomUUID());
        processEntity.setFlowName("TestFlow");
        processEntity.setStatus(ProcessStatus.INIT);
        processEntity.setContext(context);
        processEntity.setTaskTotalCount(10);
        processEntity.setTaskSuccessCount(0);
        processEntity.setTaskErrorCount(0);

        // Test ProcessEntity
        assertNotNull(processEntity.getId());
        assertEquals("TestFlow", processEntity.getFlowName());
        assertEquals(ProcessStatus.INIT, processEntity.getStatus());
        assertNotNull(processEntity.getContext());
        assertEquals(10, processEntity.getTaskTotalCount());
        assertEquals(0, processEntity.getTaskSuccessCount());
        assertEquals(0, processEntity.getTaskErrorCount());
    }

    @Test
    void testTaskInstanceEntity() {
        // Create ProcessEntity
        ProcessEntity process = new ProcessEntity();
        process.setId(UUID.randomUUID());
        process.setFlowName("TestFlow");
        process.setStatus(ProcessStatus.INIT);
        ContextEntity context = new ContextEntity();
        context.setId(UUID.randomUUID());
        process.setContext(context);
        context.setProcess(process);

        // Create TaskInstanceEntity
        TaskEntity task = new TaskEntity();
        task.setId(1L);
        task.setName("TestTask");
        task.setDescription("Test Description");

        taskInstanceEntity.setId(UUID.randomUUID());
        taskInstanceEntity.setTaskName(task.getName());
        taskInstanceEntity.setStatus(TaskStatus.SUCCESS);
        taskInstanceEntity.setTaskDescription(task.getDescription());
        taskInstanceEntity.setError(null);

        // Test TaskInstanceEntity
        assertNotNull(taskInstanceEntity.getId());
        assertEquals("TestTask", taskInstanceEntity.getTaskName());
        assertEquals(TaskStatus.SUCCESS, taskInstanceEntity.getStatus());
        assertNull(taskInstanceEntity.getError());
        assertEquals("Test Description", taskInstanceEntity.getTaskDescription());
    }
}
