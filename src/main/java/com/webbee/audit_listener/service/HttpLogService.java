package com.webbee.audit_listener.service;

import com.webbee.audit_listener.model.HttpLog;

public interface HttpLogService {

    void saveHttpLog(HttpLog httpLog);

}
