package com.sebastientr.workflow.dto.event;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StartProcessDTO {
    private FlowEntity flow;
    private UUID processId;
}
