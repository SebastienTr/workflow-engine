package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.TaskInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskInstanceRepository extends JpaRepository<TaskInstanceEntity, UUID> {
}
