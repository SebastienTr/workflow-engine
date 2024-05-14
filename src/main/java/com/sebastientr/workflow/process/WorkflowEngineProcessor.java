package com.sebastientr.workflow.process;

import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.domain.entity.TaskInstanceEntity;
import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.domain.repository.ContextRepository;
import com.sebastientr.workflow.domain.repository.FlowRepository;
import com.sebastientr.workflow.domain.repository.ProcessRepository;
import com.sebastientr.workflow.domain.repository.TaskInstanceRepository;
import com.sebastientr.workflow.dto.constant.WorkflowEngineConstant;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;
import com.sebastientr.workflow.dto.enumeration.TaskStatus;
import com.sebastientr.workflow.exception.WorkflowEngineRuntimeException;
import com.sebastientr.workflow.helper.TaskClassLoader;
import com.sebastientr.workflow.queuing.producer.WorkflowEngineEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class WorkflowEngineProcessor {
    private final ContextRepository contextRepository;
    private final WorkflowEngineEventPublisher workflowEngineEventPublisher;
    private final TaskInstanceRepository taskInstanceRepository;
    private final ProcessRepository processRepository;
    private final FlowRepository flowRepository;
    private final TransactionExecutor transactionExecutor;
    private final TaskClassLoader taskClassLoader;

    public WorkflowEngineProcessor(WorkflowEngineEventPublisher workflowEngineEventPublisher, TaskInstanceRepository taskInstanceRepository, ProcessRepository processRepository, FlowRepository flowRepository, TransactionExecutor transactionExecutor, TaskClassLoader taskClassLoader,
                                   ContextRepository contextRepository) {
        this.workflowEngineEventPublisher = workflowEngineEventPublisher;
        this.taskInstanceRepository = taskInstanceRepository;
        this.processRepository = processRepository;
        this.flowRepository = flowRepository;
        this.transactionExecutor = transactionExecutor;
        this.taskClassLoader = taskClassLoader;
        this.contextRepository = contextRepository;
    }

    /**
     * Execute the provided flowTask on a process
     * Update the process task instance and process
     * After the task is done, if SUCCESS or WARNING (when allow to fail) we continue the flow
     * If the given task was a retry, we do not continue the flow
     *
     * @param flowTask flow task that describes the current step
     * @param process current process to execute
     */
    public void execute(FlowTaskEntity flowTask, ProcessEntity process) {
        log.info("Starting task {}", flowTask.getTask().getName());

        AtomicReference<TaskInstanceEntity> taskInstance = new AtomicReference<>();
        Optional<TaskInstanceEntity> existingTaskInstance = getExistingTaskInstance(process, flowTask.getTask().getName());

        // Do run the task instance inside a transaction executor
        transactionExecutor.withoutReturn(() ->
                runTaskInstance(flowTask, process, taskInstance, existingTaskInstance)
        );

        workflowEngineEventPublisher.publishEndTaskEvent(flowTask, process, taskInstance.get());

        if (existingTaskInstance.isEmpty()) {
            next(flowTask, process, taskInstance.get().getStatus());
        }
    }

    private void runTaskInstance(FlowTaskEntity flowTask, ProcessEntity process, AtomicReference<TaskInstanceEntity> taskInstance, Optional<TaskInstanceEntity> existingTaskInstance) {
        taskInstance.set(newTaskInstance(flowTask, process));
        LoggerContext.set(WorkflowEngineConstant.LOGGER_PROCESS_ID_KEY, process.getId().toString());

        try {
            taskClassLoader.getClass(flowTask.getTask().getName()).execute(process.getContext());
            updateTaskInstanceStatus(taskInstance.get(), TaskStatus.SUCCESS, null);
        } catch (Exception | Error /* We do catch "Error" here as well */ e) {
            log.error("Task [{}] failed with error message : [{}]", flowTask.getTask().getName(), e.getMessage());
            updateTaskInstanceStatus(taskInstance.get(), TaskStatus.ERROR, e);
            updateProcessStatusOnFail(process, flowTask.getAllowToFail());
        } finally {
            contextRepository.save(process.getContext());
        }

        if (existingTaskInstance.isPresent()) {
            endRetried(existingTaskInstance.get(), taskInstance.get(), process);
        } else {
            updateCounters(process, taskInstance.get().getStatus());
        }

        processRepository.save(process);
    }

    /**
     * Update the process counters according to the task status
     *
     * @param process process entity
     * @param status task status
     */
    private void updateCounters(ProcessEntity process, TaskStatus status) {
        if (TaskStatus.SUCCESS.equals(status)) {
            process.setTaskSuccessCount(process.getTaskSuccessCount() + 1);
        } else if (TaskStatus.ERROR.equals(status)) {
            process.setTaskErrorCount(process.getTaskErrorCount() + 1);
        }
    }

    /**
     * End a retried task by saving its previous state with new status and logging the retry
     *
     * @param existingTaskInstance existing task instance
     * @param taskInstance         new task instance
     * @param process              current process
     */
    private void endRetried(TaskInstanceEntity existingTaskInstance, TaskInstanceEntity taskInstance, ProcessEntity process) {
        taskInstanceRepository.save(existingTaskInstance);

        // If the process have all task instance in SUCCESS (or RETRIED), the process status is SUCCESS,
        // otherwise it is WARNING since we cannot retry an ERROR process.
        process.setStatus(ProcessStatus.IN_PROGRESS.equals(process.getStatus()) && isEverythingSuccess(process) ?
                ProcessStatus.SUCCESS : ProcessStatus.WARNING);

        // Update task success/error counter.
        // If the retried task is success, we decrement the error counter and increment the success counter
        // Otherwise, the task is still in ERROR, so we do not update anything
        if (TaskStatus.SUCCESS.equals(taskInstance.getStatus())) {
            process.setTaskSuccessCount(process.getTaskSuccessCount() + 1);
            process.setTaskErrorCount(process.getTaskErrorCount() - 1);
        }

        log.info("Task instance {} with id {} retried with new id {}. Its final status is now {}",
                existingTaskInstance.getTaskName(),
                existingTaskInstance.getId(),
                taskInstance.getId(),
                taskInstance.getStatus());
    }

    /**
     * Check if every task are done in status SUCCESS or RETRIED
     *
     * @param process process containing tasks to count
     * @return true if everything is in success
     */
    private static boolean isEverythingSuccess(ProcessEntity process) {
        return process.getTaskInstances().stream()
                .allMatch(ti -> TaskStatus.SUCCESS.equals(ti.getStatus()) || TaskStatus.RETRIED.equals(ti.getStatus()));
    }

    /**
     * If a task instance was already present with status ERROR, this existing one is set to status RETRIED and the new one is executed
     * then we do not continue the flow because it is a retry
     *
     * @param process current process
     * @param taskName task name
     * @return an existing task instance to retry
     */
    private static Optional<TaskInstanceEntity> getExistingTaskInstance(ProcessEntity process, String taskName) {
        return process.getTaskInstances().stream()
                .filter(ti -> ti.getTaskName().equals(taskName))
                .filter(ti -> ti.getStatus().equals(TaskStatus.ERROR))
                .findFirst()
                .map(ti -> {
                    log.info("Retrying task {}", ti.getId());
                    ti.setStatus(TaskStatus.RETRIED);
                    return ti;
                });
    }

    /**
     * Update the task instance status
     *
     * @param taskInstance task instance to update
     * @param taskStatus new status
     */
    private void updateTaskInstanceStatus(TaskInstanceEntity taskInstance, TaskStatus taskStatus, Throwable throwable) {
        taskInstance.setStatus(taskStatus);

        if (throwable != null) {
            throwable.printStackTrace();
            taskInstance.setError(throwable.getMessage());
        }

        taskInstanceRepository.save(taskInstance);
    }

    /**
     * End the task
     * If the task is SUCCESS or allowedToFail
     *      If the flow is finished, we end the process
     *      If the flow is not finished, we publish an event to start the next task
     * otherwise we finish the process
     *
     * @param flowTask current flow tas
     * @param process  process to continue
     * @param status   last task status
     */
    private void next(FlowTaskEntity flowTask, ProcessEntity process, TaskStatus status) {
        if (TaskStatus.SUCCESS.equals(status) || Boolean.TRUE.equals(flowTask.getAllowToFail())) {
            getNextTask(flowTask).ifPresentOrElse(
                    // If a next task is present in the flow, we publish a start event to execute it
                    ft -> workflowEngineEventPublisher.publishStartTaskEvent(ft, process),
                    // If no next task are found in the flow, we end the process
                    () -> endProcess(process)
            );
        } else {
            endProcess(process);
        }
    }

    /**
     * Update the process status when a task failed if the process is considered as "open"
     * The process is considered as "open" if INIT, IN_PROGRESS or WARNING
     *
     * @param process process to update
     * @param allowToFail boolean, if true the process will be set to WARNING, otherwise ERROR
     */
    private void updateProcessStatusOnFail(ProcessEntity process, boolean allowToFail) {
        if (WorkflowEngineConstant.OPEN_PROCESS_STATUS.contains(process.getStatus())) {
            process.setStatus(allowToFail ? ProcessStatus.WARNING : ProcessStatus.ERROR);
            processRepository.save(process);
        }
    }

    /**
     * Create a new task instance and update the process status to IN_PROGRESS if was init
     *
     * @param flowTask current flow task
     * @param process  process tu update
     * @return created task instance entity
     */
    private TaskInstanceEntity newTaskInstance(FlowTaskEntity flowTask, ProcessEntity process) {
        TaskInstanceEntity taskInstance = new TaskInstanceEntity(process, flowTask.getTask(), TaskStatus.IN_PROGRESS);
        process.getTaskInstances().add(taskInstanceRepository.save(taskInstance));

        if (ProcessStatus.INIT.equals(process.getStatus())) {
            process.setStatus(ProcessStatus.IN_PROGRESS);
        }

        processRepository.save(process);

        return taskInstance;
    }

    /**
     * End the process. If the process was indeed IN_PROGRESS, the final status shall be SUCCESS
     * If we reach there without IN_PROGRESS status, it means an error has occurred before, the final status shall remain ERROR or WARNING
     *
     * @param process process to update
     */
    private void endProcess(ProcessEntity process) {
        process.setStatus(ProcessStatus.IN_PROGRESS.equals(process.getStatus()) ? ProcessStatus.SUCCESS : process.getStatus());
        processRepository.save(process);
        workflowEngineEventPublisher.publishEndProcessEvent(process);
        log.info("== DONE WITH STATUS : [%s] ==".formatted(process.getStatus()));
    }

    /**
     * Get the next task according to the order
     *
     * @param flowTask current flow task
     * @return an optional next task defined by the flow
     */
    private static Optional<FlowTaskEntity> getNextTask(FlowTaskEntity flowTask) {
        Optional<FlowTaskEntity> next =  flowTask.getFlow().getFlowTask().stream()
                .filter(tf -> tf.getTaskOrder().equals(flowTask.getTaskOrder() + 1))
                .findFirst();

        if (next.isPresent() && next.get().getEnabled().equals(Boolean.FALSE))
            return getNextTask(next.get());

        return next;
    }

    /**
     * On startup, update IN_PROGRESS process and taskInstance status to ERROR and/or WARNING
     * If the IN_PROGRESS task set to error is allowed to fail, we set the process status to WARNING
     */
    public void endExistingTasksOnStartup() {
        List<ProcessEntity> inProgressProcesses = processRepository.findAllByStatus(ProcessStatus.IN_PROGRESS);

        if (!inProgressProcesses.isEmpty()) {
            log.info("Found %d process IN_PROGRESS".formatted(inProgressProcesses.size()));

            for (ProcessEntity process: inProgressProcesses) {
                FlowEntity flow = flowRepository.findByNameOrderByTaskOrder(process.getFlowName())
                        .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not load IN_PROGRESS process %s for flow %s".formatted(process.getId(), process.getFlowName())));

                process.setStatus(ProcessStatus.ERROR);

                process.getTaskInstances().stream()
                        .filter(taskInstance -> TaskStatus.IN_PROGRESS.equals(taskInstance.getStatus()))
                        .findFirst()
                        .ifPresent(taskInstance -> updateStatus(process, taskInstance, flow));

                process.setTaskSuccessCount(process.getTaskSuccessCount() + 1);
            }

            processRepository.saveAll(inProgressProcesses);
        }
    }

    /**
     * Update taskInstance and process status
     *
     * @param process process to update status
     * @param taskInstance task instance to update status
     * @param flow flow to check if status shall be set to WARNING instead of ERROR
     */
    private void updateStatus(ProcessEntity process, TaskInstanceEntity taskInstance, FlowEntity flow) {
        taskInstance.setStatus(TaskStatus.ERROR);

        FlowTaskEntity flowTask = flow.getFlowTask().stream()
                .filter(ft -> ft.getTask().getName().equals(taskInstance.getTaskName()))
                .findFirst()
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not load IN_PROGRESS task %s".formatted(taskInstance.getTaskName())));

        if (Boolean.TRUE.equals(flowTask.getAllowToFail()))
            process.setStatus(ProcessStatus.WARNING);

        taskInstanceRepository.save(taskInstance);
    }
}
