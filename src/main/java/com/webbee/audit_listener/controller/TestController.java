package com.webbee.audit_listener.controller;

import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.document.AuditRequest;
import com.webbee.audit_listener.repository.AuditMethodRepository;
import com.webbee.audit_listener.repository.AuditRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final AuditRequestRepository auditRequestRepository;
    private final AuditMethodRepository auditMethodRepository;

    @GetMapping("/all")
    public ResponseEntity<Iterable<AuditMethod>> findAll() {
        return ResponseEntity.ok(auditMethodRepository.findAll());
    }

    @GetMapping("/all/req")
    public ResponseEntity<Iterable<AuditRequest>> find() {
        return ResponseEntity.ok(auditRequestRepository.findAll());
    }

}
