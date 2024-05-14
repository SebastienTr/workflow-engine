package com.sebastientr.workflow.dto.constant;

import com.sebastientr.workflow.dto.enumeration.ProcessStatus;

import java.util.List;

public class WorkflowEngineConstant {
    private WorkflowEngineConstant() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SYSTEM_USER = "System";
    public static final String LOGGER_PROCESS_ID_KEY = "processId";

    public static final List<ProcessStatus> OPEN_PROCESS_STATUS = List.of(
            ProcessStatus.INIT,
            ProcessStatus.IN_PROGRESS,
            ProcessStatus.WARNING
    );

    public static final List<ProcessStatus> CLOSED_PROCESS_STATUS = List.of(
            ProcessStatus.SUCCESS,
            ProcessStatus.WARNING
    );
}
