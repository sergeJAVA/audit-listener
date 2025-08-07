package com.webbee.audit_listener.service.impl;

import com.webbee.audit_listener.model.HttpLog;
import com.webbee.audit_listener.repository.HttpLogRepository;
import com.webbee.audit_listener.service.HttpLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HttpLogServiceImpl implements HttpLogService {

    private final HttpLogRepository httpLogRepository;

    @Override
    @Transactional
    public void saveHttpLog(HttpLog httpLog) {
        httpLogRepository.save(httpLog);
    }

}
