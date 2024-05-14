package com.sebastientr.workflow.domain.entity;

import com.sebastientr.workflow.domain.EditorAuditableEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
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
@Table(name = "workflow_context")
@Inheritance(strategy = InheritanceType.JOINED)
public class ContextEntity extends EditorAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonBackReference
    @OneToOne(mappedBy = "context")
    private ProcessEntity process;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContextEntity that)) return false;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
