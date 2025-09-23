package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {}
