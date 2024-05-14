package com.sebastientr.workflow.service.impl;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.domain.entity.TaskInstanceEntity;
import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.domain.repository.FlowRepository;
import com.sebastientr.workflow.domain.repository.FlowTaskRepository;
import com.sebastientr.workflow.domain.repository.ProcessRepository;
import com.sebastientr.workflow.domain.repository.TaskInstanceRepository;
import com.sebastientr.workflow.domain.repository.TaskRepository;
import com.sebastientr.workflow.dto.constant.WorkflowEngineConstant;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;
import com.sebastientr.workflow.dto.enumeration.TaskStatus;
import com.sebastientr.workflow.exception.WorkflowEngineRuntimeException;
import com.sebastientr.workflow.process.WorkflowEngineProcessor;
import com.sebastientr.workflow.queuing.producer.WorkflowEngineEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WorkflowEngineServiceTest {
    @Autowired
    private FlowTaskRepository flowTaskRepository;
    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private WorkflowEngineProcessor workflowEngineProcessor;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private TaskInstanceRepository taskInstanceRepository;

    @SpyBean(reset = MockReset.BEFORE)
    WorkflowEngineEventPublisher workflowEngineEventPublisher;

    @Test
    void testLazy() {
        ContextEntity context = new ContextEntity();

        ProcessEntity process = workflowEngineService.start("test-flow", context);
        sleep(1);

        process = workflowEngineService.get(process.getId().toString());

        Assertions.assertEquals(3, process.getTaskInstances().size());
        Assertions.assertNotNull(process.getContext().getProcess());
    }

    @Test
    void testStartNewProcess() {
        ContextEntity context = new ContextEntity();

        ProcessEntity process = workflowEngineService.start("test-flow", context);
        Assertions.assertEquals(ProcessStatus.INIT, process.getStatus());
        sleep(1);

        verify(workflowEngineEventPublisher, times(1)).publishStartProcessEvent(any(FlowEntity.class), any(ProcessEntity.class));
        verify(workflowEngineEventPublisher, times(3)).publishStartTaskEvent(any(FlowTaskEntity.class), any(ProcessEntity.class));
        verify(workflowEngineEventPublisher, times(1)).publishEndProcessEvent(any(ProcessEntity.class));
        verify(workflowEngineEventPublisher, times(3)).publishEndTaskEvent(any(FlowTaskEntity.class), any(ProcessEntity.class), any(TaskInstanceEntity.class));

        process = workflowEngineService.get(process.getId().toString());

        Assertions.assertEquals(ProcessStatus.SUCCESS, process.getStatus());
        Assertions.assertTrue(WorkflowEngineConstant.CLOSED_PROCESS_STATUS.contains(process.getStatus()));
        Assertions.assertEquals(3, process.getTaskTotalCount());
        Assertions.assertEquals(3, process.getTaskSuccessCount());
    }

    @Test
    void testGetProcess() {
        ContextEntity context = new ContextEntity();

        // Test the start method
        ProcessEntity process = workflowEngineService.start("test-flow", context);
        ProcessEntity processRetrieved = workflowEngineService.get(process.getId().toString());

        Assertions.assertNotNull(processRetrieved);
        Assertions.assertEquals(process.getId().toString(), processRetrieved.getId().toString());
    }

    @Test
    void testGetProcessList() {
        ContextEntity contextA = new ContextEntity();
        ContextEntity contextB = new ContextEntity();

        ProcessEntity processA = workflowEngineService.start("test-flow", contextA);
        ProcessEntity processB = workflowEngineService.start("test-flow", contextB);

        List<ProcessEntity> processEntities = List.of(processA, processB);

        List<String> processEntitiesIds = List.of(processA.getId().toString(), processB.getId().toString());
        List<ProcessEntity> processRetrieved = workflowEngineService.get(processEntitiesIds);

        Assertions.assertNotNull(processRetrieved);
        Assertions.assertEquals(processEntities.stream().map(ProcessEntity::getId).sorted().toList(), processRetrieved.stream().map(ProcessEntity::getId).sorted().toList());
    }

    @Test
    void testSetInProgressTaskToError() {
        ContextEntity context = new ContextEntity();

        // Test the start method
        ProcessEntity process = workflowEngineService.start("test-flow", context);
        sleep(1);

        process = workflowEngineService.get(process.getId().toString());
        process.setStatus(ProcessStatus.IN_PROGRESS);
        process.getTaskInstances().get(process.getTaskInstances().size() - 1).setStatus(TaskStatus.IN_PROGRESS);

        processRepository.save(process);
        taskInstanceRepository.save(process.getTaskInstances().get(process.getTaskInstances().size() - 1));

        workflowEngineProcessor.endExistingTasksOnStartup();

        ProcessEntity processRetrieved = workflowEngineService.get(process.getId().toString());

        Assertions.assertNotNull(processRetrieved);
        Assertions.assertEquals(ProcessStatus.WARNING, processRetrieved.getStatus());
        Assertions.assertEquals(TaskStatus.ERROR, processRetrieved.getTaskInstances().get(process.getTaskInstances().size() - 1).getStatus());
    }

    @Test
    void testRetryProcess() {
        ContextEntity context = new ContextEntity();

        // Start a process with a task that will fail
        var failingTask = taskRepository.findById(2L).orElseThrow();
        failingTask.setName("FailingTestTask");
        taskRepository.save(failingTask);
        ProcessEntity process = workflowEngineService.start("test-flow", context);

        Assertions.assertEquals(ProcessStatus.INIT, process.getStatus());

        sleep(1);

        process = workflowEngineService.get(process.getId().toString());

        // Then retry the same process with the failed task, but this time with a new task that does not fail
        failingTask.setName("SecondTestTask");
        taskRepository.save(failingTask);
        process.getTaskInstances().get(1).setTaskName("SecondTestTask");
        processRepository.save(process);
        taskInstanceRepository.save(process.getTaskInstances().get(1));

        Assertions.assertEquals(ProcessStatus.WARNING, process.getStatus());
        Assertions.assertEquals(3, process.getTaskTotalCount());
        Assertions.assertEquals(2, process.getTaskSuccessCount());
        Assertions.assertEquals(1, process.getTaskErrorCount());

        Assertions.assertNotNull(workflowEngineService.retry(process.getId().toString(), process.getTaskInstances().get(1).getId().toString()));
        sleep(1);
        ProcessEntity processRetrieved = workflowEngineService.get(process.getId().toString());

        Assertions.assertEquals(ProcessStatus.SUCCESS, workflowEngineService.get(processRetrieved.getId().toString()).getStatus());
        Assertions.assertEquals(3, processRetrieved.getTaskTotalCount());
        Assertions.assertEquals(3, processRetrieved.getTaskSuccessCount());
        Assertions.assertEquals(0, processRetrieved.getTaskErrorCount());
    }

    @Test
    void testRetryNotAllowedToFailProcess() {
        ContextEntity context = new ContextEntity();

        // Start a process with a task that will fail
        var flow = flowRepository.findByNameOrderByTaskOrder("test-flow").orElseThrow();
        var failingFlowTaskTask = flow.getFlowTask().get(1);
        var failingTask = failingFlowTaskTask.getTask();
        failingTask.setName("FailingTestTask");
        failingFlowTaskTask.setAllowToFail(false);
        flowTaskRepository.save(failingFlowTaskTask);
        taskRepository.save(failingTask);
        ProcessEntity process = workflowEngineService.start("test-flow", context);

        Assertions.assertEquals(ProcessStatus.INIT, process.getStatus());
        sleep(1);
        process = workflowEngineService.get(process.getId().toString());

        // Then retry the same process with the failed task, but this time with a new task that does not fail
        failingTask.setName("SecondTestTask");
        taskRepository.save(failingTask);
        process.getTaskInstances().get(1).setTaskName("SecondTestTask");
        processRepository.save(process);
        taskInstanceRepository.save(process.getTaskInstances().get(1));

        Assertions.assertEquals(ProcessStatus.ERROR, process.getStatus());

        ProcessEntity finalProcess = process;
        WorkflowEngineRuntimeException exception = Assertions.assertThrows(WorkflowEngineRuntimeException.class, () ->
                workflowEngineService.retry(finalProcess.getId().toString(), finalProcess.getTaskInstances().get(1).getId().toString()));

        Assertions.assertEquals("A task tagged as \"allowToFail: false\" cannot be retried", exception.getMessage());

        failingFlowTaskTask.setAllowToFail(true);
        flowTaskRepository.save(failingFlowTaskTask);
        Assertions.assertEquals(ProcessStatus.ERROR, workflowEngineService.get(process.getId().toString()).getStatus());
    }

    @Test
    void testRetryFailOnSuccessfulTask() {
        ContextEntity context = new ContextEntity();

        // Test the start method
        ProcessEntity process = workflowEngineService.start("test-flow", context);
        sleep(1);
        process = workflowEngineService.get(process.getId().toString());

        ProcessEntity finalProcess = process;
        WorkflowEngineRuntimeException exception = Assertions.assertThrows(WorkflowEngineRuntimeException.class, () ->
                workflowEngineService.retry(finalProcess.getId().toString(), finalProcess.getTaskInstances().get(1).getId().toString()));

        Assertions.assertEquals("The task has status %s, you can only retry an %s task".formatted(process.getTaskInstances().get(0).getStatus(), TaskStatus.ERROR), exception.getMessage());
    }

    @Test
    void testWithDisabledTask() {
        ContextEntity context = new ContextEntity();

        // Start a process with a task that will fail
        var flow = flowRepository.findByNameOrderByTaskOrder("test-flow").orElseThrow();
        flow.getFlowTask().get(1).setEnabled(false);
        flowTaskRepository.save(flow.getFlowTask().get(1));
        ProcessEntity process = workflowEngineService.start("test-flow", context);

        sleep(1);

        process = workflowEngineService.get(process.getId().toString());

        Assertions.assertEquals(ProcessStatus.SUCCESS, process.getStatus());
        Assertions.assertEquals(2, process.getTaskTotalCount());
        Assertions.assertEquals(2, process.getTaskSuccessCount());
    }

    @Test
    void testWithFirstDisabledTask() {
        ContextEntity context = new ContextEntity();

        // Start a process with a task that will fail
        var flow = flowRepository.findByNameOrderByTaskOrder("test-flow").orElseThrow();
        flow.getFlowTask().get(0).setEnabled(false);
        flowTaskRepository.save(flow.getFlowTask().get(0));
        ProcessEntity process = workflowEngineService.start("test-flow", context);
        Assertions.assertEquals(ProcessStatus.INIT, process.getStatus());
        sleep(1);
        process = workflowEngineService.get(process.getId().toString());

        Assertions.assertEquals(ProcessStatus.SUCCESS, process.getStatus());
        Assertions.assertEquals(2, process.getTaskTotalCount());
        Assertions.assertEquals(2, process.getTaskSuccessCount());
    }

    @Test
    void testWithAllDisabledTask() {
        ContextEntity context = new ContextEntity();

        // Start a process with a task that will fail
        var flow = flowRepository.findByNameOrderByTaskOrder("test-flow").orElseThrow();
        flow.getFlowTask().get(0).setEnabled(false);
        flow.getFlowTask().get(1).setEnabled(false);
        flow.getFlowTask().get(2).setEnabled(false);
        flowTaskRepository.saveAll( flow.getFlowTask());

        WorkflowEngineRuntimeException exception = Assertions.assertThrows(WorkflowEngineRuntimeException.class, () -> workflowEngineService.start("test-flow", context));

        Assertions.assertEquals("No enable flow task was found on flow test-flow", exception.getMessage());
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