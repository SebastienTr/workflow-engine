package com.sebastientr.workflow.task;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class TaskDelegate {
    public abstract void execute(ContextEntity context);
}
