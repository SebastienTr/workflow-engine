package com.sebastientr.workflow.exception;

import lombok.Getter;

@Getter
public class WorkflowEngineException extends Exception {
    private final String message;
    private final Throwable previous;

    public WorkflowEngineException(String message) {
        this.message = message;
        this.previous = null;
    }

    public WorkflowEngineException(String message, Throwable throwable) {
        this.message = message;
        this.previous = throwable;
    }
}
