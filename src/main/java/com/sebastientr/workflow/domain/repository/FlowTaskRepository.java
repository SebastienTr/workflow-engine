package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.core.FlowTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowTaskRepository extends JpaRepository<FlowTaskEntity, Long> {
}
