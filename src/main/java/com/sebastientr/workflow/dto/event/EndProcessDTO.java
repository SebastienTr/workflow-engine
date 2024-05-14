package com.sebastientr.workflow.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class EndProcessDTO {
    private UUID processId;
}
