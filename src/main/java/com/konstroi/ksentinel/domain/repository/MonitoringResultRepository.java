package com.konstroi.ksentinel.domain.repository;

import com.konstroi.ksentinel.domain.model.MonitoringResult;
import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, Long> {

    Page<MonitoringResult> findByApiConfigIdOrderByCheckedAtDesc(Long apiConfigId, Pageable pageable);

    List<MonitoringResult> findTop10ByApiConfigIdOrderByCheckedAtDesc(Long apiConfigId);

    @Query("SELECT COUNT(r) FROM MonitoringResult r WHERE r.apiConfig.id = :apiConfigId AND r.checkedAt >= :since")
    long countByApiConfigIdSince(Long apiConfigId, LocalDateTime since);

    @Query("SELECT COUNT(r) FROM MonitoringResult r WHERE r.apiConfig.id = :apiConfigId AND r.status = :status AND r.checkedAt >= :since")
    long countByApiConfigIdAndStatusSince(Long apiConfigId, MonitoringStatus status, LocalDateTime since);

    @Query("SELECT AVG(r.latencyMs) FROM MonitoringResult r WHERE r.apiConfig.id = :apiConfigId AND r.checkedAt >= :since AND r.latencyMs IS NOT NULL")
    Double avgLatencyByApiConfigIdSince(Long apiConfigId, LocalDateTime since);

    void deleteByCheckedAtBefore(LocalDateTime before);
}
