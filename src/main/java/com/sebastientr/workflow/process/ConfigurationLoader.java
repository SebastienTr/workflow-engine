package com.sebastientr.workflow.process;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import com.sebastientr.workflow.domain.repository.FlowRepository;
import com.sebastientr.workflow.exception.WorkflowEngineConfigurationException;
import com.sebastientr.workflow.helper.TaskClassLoader;
import com.sebastientr.workflow.task.TaskDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ConfigurationLoader {
    private final FlowRepository flowRepository;
    private final TaskClassLoader taskClassLoader;

    public ConfigurationLoader(FlowRepository flowRepository, TaskClassLoader taskClassLoader) {
        this.flowRepository = flowRepository;
        this.taskClassLoader = taskClassLoader;
    }

    /**
     * Validate integrity of configured flows
     *
     * @throws WorkflowEngineConfigurationException if flows are not valid
     */
    public void validateFlows() throws WorkflowEngineConfigurationException {
        var flows = flowRepository.findAll();

        for (FlowEntity flow: flows) {
            log.info("Validating flow {}", flow.getName());

            if (flow.getFlowTask() == null || flow.getFlowTask().isEmpty()) {
                throw new WorkflowEngineConfigurationException("Flow [%s] has no flowTask(s) associated".formatted(flow.getName()));
            }

            validateFlowTaskOrder(flow);

            for (FlowTaskEntity flowTask: flow.getFlowTask()) {
                validateFlowTask(flowTask);
            }
        }
    }

    /**
     * Validate integrity of configured flows task orders
     *
     * @throws WorkflowEngineConfigurationException if flows task orders are not valid
     */
    private static void validateFlowTaskOrder(FlowEntity flow) throws WorkflowEngineConfigurationException {
        List<Integer> ordersList = flow.getFlowTask().stream()
                .map(FlowTaskEntity::getTaskOrder)
                .toList();

        Integer max = Collections.max(ordersList);

        if (max != ordersList.size()) {
            throw new WorkflowEngineConfigurationException("The max value (%d) of flowTask order does not match with the count of flowTasks (%d) for flow %s"
                    .formatted(max, ordersList.size(), flow.getName()));
        }

        for (int i = 1; i <= ordersList.size(); i++) {
            if (!ordersList.contains(i)) {
                throw new WorkflowEngineConfigurationException("Could not find flowTask with order %d for flow %s".formatted(i, flow.getName()));
            }
        }
    }

    /**
     * Validate the integrity of tasks
     * @param flowTask flow task
     *
     * @throws WorkflowEngineConfigurationException if a task could not be loaded
     */
    private void validateFlowTask(FlowTaskEntity flowTask) throws WorkflowEngineConfigurationException {
        try {
            TaskDelegate loadedClass = taskClassLoader.getClass(flowTask.getTask().getName());
            assert loadedClass != null;
        } catch (Exception e) {
            throw new WorkflowEngineConfigurationException("Loaded task class %s is invalid".formatted(flowTask.getTask().getName()), e);
        }
    }
}
