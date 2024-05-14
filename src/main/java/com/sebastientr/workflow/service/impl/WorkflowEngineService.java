package com.sebastientr.workflow.service.impl;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.domain.entity.TaskInstanceEntity;
import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.domain.repository.ContextRepository;
import com.sebastientr.workflow.domain.repository.FlowRepository;
import com.sebastientr.workflow.domain.repository.ProcessRepository;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;
import com.sebastientr.workflow.dto.enumeration.TaskStatus;
import com.sebastientr.workflow.exception.WorkflowEngineConfigurationException;
import com.sebastientr.workflow.exception.WorkflowEngineRuntimeException;
import com.sebastientr.workflow.process.ConfigurationLoader;
import com.sebastientr.workflow.process.TransactionExecutor;
import com.sebastientr.workflow.process.WorkflowEngineProcessor;
import com.sebastientr.workflow.queuing.producer.WorkflowEngineEventPublisher;
import com.sebastientr.workflow.service.IWorkflowEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WorkflowEngineService implements IWorkflowEngineService {
    private final WorkflowEngineEventPublisher workflowEngineEventPublisher;
    private final WorkflowEngineProcessor workflowEngineProcessor;
    private final ConfigurationLoader configurationLoader;
    private final ContextRepository contextRepository;
    private final ProcessRepository processRepository;
    private final FlowRepository flowRepository;

    private final TransactionExecutor transactionExecutor;

    @Value("${workflow-engine.validate-flows-on-startup:true}")
    private Boolean validateFlowsOnStartup;

    @Value("${workflow-engine.clean-process-on-startup:false}")
    private Boolean cleanProcessOnStartup;

    @Value("${workflow-engine.continue-process-on-startup:false}")
    private Boolean continueProcessOnStartup;

    public WorkflowEngineService(WorkflowEngineEventPublisher workflowEngineEventPublisher, WorkflowEngineProcessor workflowEngineProcessor, ConfigurationLoader configurationLoader, ContextRepository contextRepository, ProcessRepository processRepository, FlowRepository flowRepository, TransactionExecutor transactionExecutor) {
        this.workflowEngineEventPublisher = workflowEngineEventPublisher;
        this.workflowEngineProcessor = workflowEngineProcessor;
        this.configurationLoader = configurationLoader;
        this.contextRepository = contextRepository;
        this.processRepository = processRepository;
        this.flowRepository = flowRepository;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public ProcessEntity start(String flowName, ContextEntity context) {
        FlowEntity flow = flowRepository.findByNameOrderByTaskOrder(flowName)
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not find flow %s".formatted(flowName)));

        ProcessEntity process = transactionExecutor.withReturn(() -> {
            ProcessEntity newProcess = processRepository.save(new ProcessEntity(flow.getName(), context, flow.getSize()));

            log.info("Starting flow [{}] with processId : [{}]", flow.getName(), newProcess.getId());

            context.setProcess(newProcess);
            contextRepository.save(context);
            return newProcess;
        });

        workflowEngineEventPublisher.publishStartProcessEvent(flow, process);
        workflowEngineEventPublisher.publishStartTaskEvent(getFirstFlowTask(flow), process);

        return process;
    }

    private static FlowTaskEntity getFirstFlowTask(FlowEntity flow) {
        for (FlowTaskEntity flowTask : flow.getFlowTask()) {
            if (Boolean.TRUE.equals(flowTask.getEnabled()))
                return flowTask;
        }

        throw new WorkflowEngineRuntimeException("No enable flow task was found on flow %s".formatted(flow.getName()));
    }

    @Override
    public ProcessEntity get(String processId) {
        return processRepository.findByIdOrderByTaskInstancesCreatedAt(UUID.fromString(processId))
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not find process with id %s".formatted(processId)));
    }

    @Override
    public ProcessEntity updateStatus(String processId, ProcessStatus processStatus) {
        UUID uuidProcessId = UUID.fromString(processId);
        ProcessEntity processEntity = processRepository.findByIdOrderByTaskInstancesCreatedAt(uuidProcessId)
                .orElseThrow(() -> new WorkflowEngineRuntimeException(
                        String.format("Could not find process with id %s", processId)));
        processEntity.setStatus(processStatus);
        return processRepository.save(processEntity);
    }

    @Override
    public List<ProcessEntity> get(List<String> processIds) {
        return processRepository.findByIdInOrderByTaskInstancesCreatedAt(processIds.stream().map(UUID::fromString).toList());
    }

    @Override
    public ProcessEntity retry(String processId, String taskId) {
        ProcessEntity process = get(processId);

        FlowEntity flow = flowRepository.findByNameOrderByTaskOrder(process.getFlowName())
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not find flow %s".formatted(process.getFlowName())));

        TaskInstanceEntity taskInstance = process.getTaskInstances().stream()
                .filter(instance -> UUID.fromString(taskId).equals(instance.getId()))
                .findFirst()
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not find task instance with id [%s] on process [%s]".formatted(taskId, processId)));

        FlowTaskEntity flowTask = flow.getFlowTask().stream()
                .filter(ft -> taskInstance.getTaskName().equals(ft.getTask().getName()))
                .findFirst()
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not find task [%s] on flow [%s]".formatted(taskInstance.getTaskName(), flow.getName())));

        if (Boolean.FALSE.equals(flowTask.getAllowToFail())) {
            throw new WorkflowEngineRuntimeException("A task tagged as \"allowToFail: false\" cannot be retried");
        }

        if (!TaskStatus.ERROR.equals(taskInstance.getStatus())) {
            throw new WorkflowEngineRuntimeException("The task has status %s, you can only retry an %s task".formatted(taskInstance.getStatus(), TaskStatus.ERROR));
        }

        log.info("Retrying task {} on process {}", taskInstance.getTaskName(), processId);

        process.setStatus(ProcessStatus.IN_PROGRESS);

        processRepository.save(process);

        workflowEngineEventPublisher.publishStartTaskEvent(flowTask, process);

        return process;
    }

    public List<FlowEntity> getFlows() {
        return flowRepository.findAll();
    }

    @Override
    public FlowEntity getFlow(String flowName) {
        return flowRepository.findByNameOrderByTaskOrder(flowName)
                .orElseThrow(() -> new WorkflowEngineRuntimeException("Could not find flow with name %s".formatted(flowName)));
    }

    /**
     * When the application is ready, we start a flow validation process to ensure
     * the integrity of the configuration.
     *
     * @throws WorkflowEngineConfigurationException if the configuration is not valid
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startUp() throws WorkflowEngineConfigurationException {
        if (Boolean.TRUE.equals(validateFlowsOnStartup)) {
            log.info("Loading workflow configurations ...");
            configurationLoader.validateFlows();
            log.info("Workflow configurations validated.");
        }

        if (Boolean.TRUE.equals(cleanProcessOnStartup)) {
            log.info("Check existing IN_PROGRESS processes and tasks ...");
            workflowEngineProcessor.endExistingTasksOnStartup();
            log.info("Checked existing IN_PROGRESS processes and tasks.");
        }

        if (Boolean.TRUE.equals(continueProcessOnStartup)) {
            log.info("TODO : Continue processes on startup");
        }
    }
}
