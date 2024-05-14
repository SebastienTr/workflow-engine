package com.sebastientr.workflow.process;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.domain.entity.core.TaskEntity;
import com.sebastientr.workflow.domain.repository.FlowRepository;
import com.sebastientr.workflow.domain.repository.FlowTaskRepository;
import com.sebastientr.workflow.domain.repository.TaskRepository;
import com.sebastientr.workflow.exception.WorkflowEngineConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConfigurationLoaderTest {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    FlowRepository flowRepository;
    @Autowired
    FlowTaskRepository flowTaskRepository;

    @Autowired
    ConfigurationLoader configurationLoader;

    @Test
    void testWithUnknownTask() {
        TaskEntity task = new TaskEntity();
        FlowEntity flow = new FlowEntity();
        flow.setName("wrong-flow");

        task.setName("WrongTask");
        task.setDescription("fjoifzfez");

        taskRepository.save(task);
        flowRepository.save(flow);

        FlowTaskEntity flowTask = new FlowTaskEntity();
        flowTask.setId(100L);
        flowTask.setTaskOrder(1);
        flowTask.setTask(task);
        flowTask.setFlow(flow);
        flowTask.setAllowToFail(true);

        flowTaskRepository.save(flowTask);

        WorkflowEngineConfigurationException exception = Assertions.assertThrows(WorkflowEngineConfigurationException.class,() -> configurationLoader.validateFlows());

        Assertions.assertEquals("Loaded task class WrongTask is invalid", exception.getMessage());
        Assertions.assertNotNull(exception.getPrevious());
    }

    @Test
    void testWithWrongSize() {
        TaskEntity task = new TaskEntity();
        FlowEntity flow = new FlowEntity();
        flow.setName("wrong-flow");

        task.setName("WrongTask");
        task.setDescription("fjoifzfez");

        taskRepository.save(task);
        flowRepository.save(flow);

        FlowTaskEntity flowTask = new FlowTaskEntity();
        flowTask.setId(100L);
        flowTask.setTaskOrder(42);
        flowTask.setTask(task);
        flowTask.setFlow(flow);
        flowTask.setAllowToFail(true);

        flowTaskRepository.save(flowTask);

        WorkflowEngineConfigurationException exception = Assertions.assertThrows(WorkflowEngineConfigurationException.class,() -> configurationLoader.validateFlows());

        Assertions.assertEquals("The max value (42) of flowTask order does not match with the count of flowTasks (1) for flow wrong-flow", exception.getMessage());
    }

    @Test
    void testWithWrongOrder() {
        TaskEntity task1 = new TaskEntity();
        TaskEntity task2 = new TaskEntity();
        FlowEntity flow = new FlowEntity();

        flow.setName("wrong-flow");
        task1.setName("WrongTask");
        task1.setDescription("fjoifzfez");
        task2.setName("WrongTask2");
        task2.setDescription("fjoifzfez");

        taskRepository.save(task1);
        taskRepository.save(task2);
        flowRepository.save(flow);

        FlowTaskEntity flowTask1 = new FlowTaskEntity();
        flowTask1.setId(100L);
        flowTask1.setTaskOrder(2);
        flowTask1.setTask(task1);
        flowTask1.setFlow(flow);
        flowTask1.setAllowToFail(true);

        FlowTaskEntity flowTask2 = new FlowTaskEntity();
        flowTask2.setId(100L);
        flowTask2.setTaskOrder(2);
        flowTask2.setTask(task2);
        flowTask2.setFlow(flow);
        flowTask2.setAllowToFail(true);

        flowTaskRepository.save(flowTask1);
        flowTaskRepository.save(flowTask2);

        WorkflowEngineConfigurationException exception = Assertions.assertThrows(WorkflowEngineConfigurationException.class,() -> configurationLoader.validateFlows());

        Assertions.assertEquals("Could not find flowTask with order 1 for flow wrong-flow", exception.getMessage());
    }

    @Test
    void testWithEmptyFlow() {
        FlowEntity flow = new FlowEntity();
        flow.setName("wrong-flow");
        flowRepository.save(flow);

        WorkflowEngineConfigurationException exception = Assertions.assertThrows(WorkflowEngineConfigurationException.class, () -> configurationLoader.validateFlows());

        Assertions.assertEquals("Flow [wrong-flow] has no flowTask(s) associated", exception.getMessage());
    }
}