package com.webbee.audit_listener.service.impl;

import com.webbee.audit_listener.model.MethodLog;
import com.webbee.audit_listener.repository.MethodLogRepository;
import com.webbee.audit_listener.service.MethodLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MethodLogServiceImpl implements MethodLogService {

    private final MethodLogRepository methodLogRepository;

    @Override
    @Transactional
    public void saveMethodLog(MethodLog methodLog) {
        methodLogRepository.save(methodLog);
    }

}
