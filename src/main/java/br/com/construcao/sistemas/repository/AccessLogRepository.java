package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByDataCriacaoBetweenOrderByDataCriacaoDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "a.dataCriacao BETWEEN :startDate AND :endDate " +
            "AND (:userEmail IS NULL OR a.userEmail = :userEmail) " +
            "AND (:method IS NULL OR a.method = :method) " +
            "AND (:statusCode IS NULL OR a.statusCode = :statusCode) " +
            "ORDER BY a.dataCriacao DESC")
    List<AuditLog> findByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userEmail") String userEmail,
            @Param("method") String method,
            @Param("statusCode") Integer statusCode
    );

}
