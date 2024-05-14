package com.sebastientr.workflow.queuing;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkflowEvent<T> extends ApplicationEvent {
    private final transient T what;
    protected final EventType type;

    public WorkflowEvent(T what, EventType type) {
        super(what);
        this.what = what;
        this.type = type;
    }

    public enum EventType {
        START_TASK, END_TASK, START_PROCESS, END_PROCESS
    }
}