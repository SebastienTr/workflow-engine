package com.sebastientr.workflow.exception;

import lombok.Getter;

@Getter
public class WorkflowEngineConfigurationException extends WorkflowEngineException {
    public WorkflowEngineConfigurationException(String message) {
        super(message);
    }

    public WorkflowEngineConfigurationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
