package com.sebastientr.workflow.dto.event;

import com.sebastientr.workflow.domain.entity.TaskInstanceEntity;
import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class EndTaskDTO {
    private FlowTaskEntity flowTask;
    private UUID processId;
    private TaskInstanceEntity taskInstance;
}
