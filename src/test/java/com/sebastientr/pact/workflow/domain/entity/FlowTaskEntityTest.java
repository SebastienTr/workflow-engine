package com.sebastientr.workflow.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.domain.entity.core.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class FlowTaskEntityTest {

    @InjectMocks
    private FlowEntity flowEntity;

    @InjectMocks
    private FlowTaskEntity flowTaskEntity;

    @InjectMocks
    private TaskEntity taskEntity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFlowEntity() {
        // Create FlowEntity
        flowEntity.setId(1L);
        flowEntity.setName("TestFlow");

        // Create FlowTaskEntity
        FlowTaskEntity flowTask = new FlowTaskEntity();
        flowTask.setId(1L);
        flowTask.setAllowToFail(false);
        flowTask.setTaskOrder(1);

        List<FlowTaskEntity> flowTasks = new ArrayList<>();
        flowTasks.add(flowTask);
        flowEntity.setFlowTask(flowTasks);

        // Test FlowEntity
        assertEquals(1L, flowEntity.getId());
        assertEquals("TestFlow", flowEntity.getName());
        assertEquals(1, flowEntity.getFlowTask().size());
    }

    @Test
    void testFlowTaskEntity() {
        // Create TaskEntity
        taskEntity.setId(1L);
        taskEntity.setName("TestTask");
        taskEntity.setDescription("Test Description");

        // Create FlowEntity
        flowEntity.setId(1L);
        flowEntity.setName("TestFlow");

        // Create FlowTaskEntity
        flowTaskEntity.setId(1L);
        flowTaskEntity.setAllowToFail(false);
        flowTaskEntity.setTaskOrder(1);
        flowTaskEntity.setFlow(flowEntity);
        flowTaskEntity.setTask(taskEntity);

        // Test FlowTaskEntity
        assertEquals(1L, flowTaskEntity.getId());
        assertFalse(flowTaskEntity.getAllowToFail());
        assertEquals(1, flowTaskEntity.getTaskOrder());
        assertNotNull(flowTaskEntity.getFlow());
        assertNotNull(flowTaskEntity.getTask());
    }

    @Test
    void testTaskEntity() {
        // Create TaskEntity
        taskEntity.setId(1L);
        taskEntity.setName("TestTask");
        taskEntity.setDescription("Test Description");

        // Test TaskEntity
        assertEquals(1L, taskEntity.getId());
        assertEquals("TestTask", taskEntity.getName());
        assertEquals("Test Description", taskEntity.getDescription());
    }
}
