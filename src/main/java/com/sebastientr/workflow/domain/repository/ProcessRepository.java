package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.ProcessEntity;
import com.sebastientr.workflow.dto.enumeration.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessRepository extends JpaRepository<ProcessEntity, UUID> {
    @Query("SELECT process FROM ProcessEntity process LEFT JOIN FETCH process.taskInstances taskInstances LEFT JOIN FETCH process.context context WHERE process.id = :uuid ORDER BY taskInstances.createdAt")
    Optional<ProcessEntity> findByIdOrderByTaskInstancesCreatedAt(@Param("uuid") UUID uuid);

    @Query("SELECT process FROM ProcessEntity process LEFT JOIN FETCH process.taskInstances taskInstances LEFT JOIN FETCH process.context context WHERE process.status = :status ORDER BY taskInstances.createdAt")
    List<ProcessEntity> findAllByStatus(@Param("status") ProcessStatus status);

    @Query("SELECT process FROM ProcessEntity process LEFT JOIN FETCH process.taskInstances taskInstances LEFT JOIN FETCH process.context context WHERE process.id IN :uuids ORDER BY taskInstances.createdAt")
    List<ProcessEntity> findByIdInOrderByTaskInstancesCreatedAt(@Param("uuids") List<UUID> uuids);
}
