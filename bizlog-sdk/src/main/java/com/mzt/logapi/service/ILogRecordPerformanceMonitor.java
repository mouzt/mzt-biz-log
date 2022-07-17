package com.mzt.logapi.service;

import org.springframework.util.StopWatch;

public interface ILogRecordPerformanceMonitor {

     void print(StopWatch stopWatch);

     String MONITOR_NAME = "log-record-performance";
     String MONITOR_TASK_BEFORE_EXECUTE = "before-execute";
     String MONITOR_TASK_AFTER_EXECUTE = "after-execute";
}
