package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.core.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Optional<TaskEntity> findByName(String taskName);
}
