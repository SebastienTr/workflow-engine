package com.sebastientr.workflow.domain.entity;

import com.sebastientr.workflow.domain.EditorAuditableEntity;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workflow_process")
public class ProcessEntity extends EditorAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String flowName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @Column(nullable = false)
    private Integer taskTotalCount;

    @Column(nullable = false)
    private Integer taskSuccessCount;

    @Column(nullable = false)
    private Integer taskErrorCount;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "process", orphanRemoval = true)
    @JsonManagedReference
    private List<TaskInstanceEntity> taskInstances = new ArrayList<>();

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "context_id", referencedColumnName = "id")
    @JsonManagedReference
    private ContextEntity context;

    public ProcessEntity(String flowName, ContextEntity contextEntity, int size) {
        this.flowName = flowName;
        this.status = ProcessStatus.INIT;
        this.context = contextEntity;

        // Init counters
        this.taskTotalCount = size;
        this.taskSuccessCount = 0;
        this.taskErrorCount = 0;
    }
}
