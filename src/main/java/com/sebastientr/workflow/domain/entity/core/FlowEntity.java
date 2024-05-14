package com.sebastientr.workflow.domain.entity.core;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "config_workflow_flow")
public class FlowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "flow", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<FlowTaskEntity> flowTask;

    public int getSize() {
        List<FlowTaskEntity> tasks = getFlowTask();

        if (tasks != null) {
            return (int) tasks.stream().filter(FlowTaskEntity::getEnabled).count();
        }

        return 0;
    }
}
