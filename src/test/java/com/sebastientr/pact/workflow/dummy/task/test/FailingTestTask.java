package com.sebastientr.workflow.dummy.task.test;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import com.sebastientr.workflow.dummy.service.IDummyService;
import com.sebastientr.workflow.dummy.task.AbstractTestClass;
import com.sebastientr.workflow.exception.WorkflowEngineRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FailingTestTask extends AbstractTestClass {
    public FailingTestTask(IDummyService dummyService) {
        super(dummyService);
    }

    @Override
    public void execute(ContextEntity context) {
        log.info("Executing {}", this.getClass().getName());
        throw new WorkflowEngineRuntimeException("error");
    }
}
