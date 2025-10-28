package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AuditLog, Long> {}
