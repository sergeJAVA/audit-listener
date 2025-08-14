package com.webbee.audit_listener.repository;

import com.webbee.audit_listener.model.HttpLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HttpLogRepository extends JpaRepository<HttpLog, Long> {
}
