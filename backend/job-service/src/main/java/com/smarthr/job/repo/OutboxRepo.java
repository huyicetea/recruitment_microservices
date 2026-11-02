package com.smarthr.job.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smarthr.job.model.entity.Outbox;

@Repository
public interface OutboxRepo extends JpaRepository<Outbox, UUID> {

    List<Outbox> findByProcessedFalseOrderByCreatedAtAsc();
}