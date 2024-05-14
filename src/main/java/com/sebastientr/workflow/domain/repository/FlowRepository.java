package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlowRepository extends JpaRepository<FlowEntity, Long> {
    @Query("SELECT f FROM FlowEntity f JOIN FETCH f.flowTask ft WHERE f.name = :flowName ORDER BY ft.taskOrder")
    Optional<FlowEntity> findByNameOrderByTaskOrder(@Param("flowName") String flowName);
}
