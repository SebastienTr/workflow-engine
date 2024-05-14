package com.sebastientr.workflow.controller;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import com.sebastientr.workflow.service.impl.WorkflowEngineService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ConditionalOnExpression("${workflow-engine.enable-controller:false}")
@RequestMapping("/workflow-engine")
public class WorkflowEngineController {
    private final WorkflowEngineService workflowEngineService;

    public WorkflowEngineController(WorkflowEngineService workflowEngineService) {
        this.workflowEngineService = workflowEngineService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FlowEntity> getFlows() {
        return workflowEngineService.getFlows();
    }

    @GetMapping("/{flowName}")
    @ResponseStatus(HttpStatus.OK)
    public FlowEntity getFlow(@PathVariable("flowName") String flowName) {
        return workflowEngineService.getFlow(flowName);
    }
}
