package com.sebastientr.workflow.helper;

import com.sebastientr.workflow.exception.WorkflowEngineConfigurationException;
import com.sebastientr.workflow.task.TaskDelegate;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;
import java.util.Optional;

@Component
public class TaskClassLoader {
    private final ApplicationContext applicationContext;
    private final Map<String, TaskDelegate> tasks = new LinkedCaseInsensitiveMap<>();

    public TaskClassLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void setTaskDelegateBeans() {
        tasks.putAll(this.applicationContext.getBeansOfType(TaskDelegate.class));
    }

    /**
     * Get the class from a class ,ame.
     * First we try to load the class as a bean, if the bean could not be found, we try to get the class with path
     *
     * @param taskName task class name
     * @return loaded class
     * @throws WorkflowEngineConfigurationException if the class could not be loaded
     */
    public TaskDelegate getClass(String taskName) throws WorkflowEngineConfigurationException {
        return Optional.ofNullable(this.tasks.get(taskName))
                .orElseThrow(() -> new WorkflowEngineConfigurationException("Could not load task [%s]".formatted(taskName)));
    }
}
