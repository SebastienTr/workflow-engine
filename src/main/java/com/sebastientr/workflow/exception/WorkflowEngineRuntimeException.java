package com.sebastientr.workflow.exception;

import lombok.Getter;

@Getter
public class WorkflowEngineRuntimeException extends RuntimeException {
    private final String message;

    public WorkflowEngineRuntimeException(String message) {
        this.message = message;
    }
}
