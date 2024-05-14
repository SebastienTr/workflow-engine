package com.sebastientr.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class EditorAuditableEntity extends AuditableEntity {
    @Column(nullable = false, updatable = false)
    @CreatedBy
    protected String createdBy;

    @LastModifiedBy
    protected String modifiedBy;
}
