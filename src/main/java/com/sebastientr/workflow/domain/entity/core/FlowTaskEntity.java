package com.sebastientr.workflow.domain.entity.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "config_workflow_flow_task")
public class FlowTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flow_id", referencedColumnName = "id")
    @JsonBackReference
    private FlowEntity flow;

    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private TaskEntity task;

    @Column(nullable = false)
    private Boolean allowToFail;

    @Column(nullable = false)
    private Integer taskOrder;

    private Boolean enabled = true;
}
