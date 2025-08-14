package com.webbee.audit_listener.repository;

import com.webbee.audit_listener.model.MethodLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MethodLogRepository extends JpaRepository<MethodLog, Long> {
}
