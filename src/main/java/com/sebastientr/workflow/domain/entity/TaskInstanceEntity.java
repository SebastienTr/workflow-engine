package com.sebastientr.workflow.domain.entity;

import com.sebastientr.workflow.domain.EditorAuditableEntity;
import com.sebastientr.workflow.domain.entity.core.TaskEntity;
import com.sebastientr.workflow.dto.enumeration.TaskStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workflow_task_instance")
public class TaskInstanceEntity extends EditorAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String taskName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    @ManyToOne
    @JoinColumn(name="process_id", nullable = false)
    @JsonBackReference
    private ProcessEntity process;

    public TaskInstanceEntity(ProcessEntity process, TaskEntity task, TaskStatus taskStatus) {
        this.process = process;
        this.taskName = task.getName();
        this.status = taskStatus;
        this.taskDescription = task.getDescription();
    }
}
