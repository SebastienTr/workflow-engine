package com.sebastientr.workflow.dummy.task;

import com.sebastientr.workflow.dummy.service.IDummyService;
import com.sebastientr.workflow.task.TaskDelegate;

public abstract class AbstractTestClass extends TaskDelegate {
    protected final IDummyService dummyService;
    public AbstractTestClass(IDummyService dummyService) {
        this.dummyService = dummyService;
    }
}
