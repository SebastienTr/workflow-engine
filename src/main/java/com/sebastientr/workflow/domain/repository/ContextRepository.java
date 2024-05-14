package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.ContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContextRepository extends JpaRepository<ContextEntity, UUID> {
}
