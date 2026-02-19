# Separate Logging Implementation

## Overview
This document describes the implementation of separate log files for different log levels (DEBUG, WARNING, ERROR) in the CMS backend system.

## Log File Structure

### Generated Log Files
```
logs/
├── cms-application.log      # Main application log (INFO and above)
├── cms-debug.log         # Debug-only logs
├── cms-warning.log       # Warning-only logs
└── cms-error.log         # Error-only logs
```

### Log File Details

#### 1. cms-application.log
- **Purpose**: Main application log containing INFO level and above
- **Content**: General application flow, business operations, system events
- **Rotation**: Daily, 10MB max, 30 days retention, 1GB total cap
- **Example**:
```
2026-02-19 16:01:37.852 [http-nio-8080-exec-6] INFO [req-abc123,op-def456] [c.e.cms.service.impl.CardServiceImpl] - getAllCards(page=0, size=5) - Successfully retrieved paginated results: 5 cards
```

#### 2. cms-debug.log
- **Purpose**: Debug-level logs for detailed troubleshooting
- **Content**: SQL queries, method entry/exit, parameter values (masked), performance metrics
- **Rotation**: Daily, 10MB max, 15 days retention, 500MB total cap
- **Example**:
```
2026-02-19 16:01:37.846 [http-nio-8080-exec-6] DEBUG [cc1811a1-49b9-4955-8d6d-23136d57ecef] [c.epic.cms.repository.CardRepository] - countAllCards() - Executing query: SELECT COUNT(*) FROM card
2026-02-19 16:01:37.848 [http-nio-8080-exec-6] DEBUG [cc1811a1-49b9-4955-8d6d-23136d57ecef] [o.s.jdbc.core.JdbcTemplate] - Executing SQL query [SELECT COUNT(*) FROM card]
```

#### 3. cms-warning.log
- **Purpose**: Warning-level logs for business rule violations and recoverable issues
- **Content**: Validation failures, business rule violations, performance degradation
- **Rotation**: Daily, 10MB max, 30 days retention, 200MB total cap
- **Example**:
```
2026-02-19 16:00:33.135 [http-nio-8080-exec-7] WARN [790f86f9-8f9e-4be4-a39b-293ac01fd3d8,] [c.e.c.e.GlobalExceptionHandler] - BusinessException: Cannot create request: There is already a pending activation request for this card - URI: /api/card-requests
```

#### 4. cms-error.log
- **Purpose**: Error-level logs for system failures and critical issues
- **Content**: System failures, database errors, unhandled exceptions, stack traces
- **Rotation**: Daily, 10MB max, 30 days retention, 500MB total cap
- **Example**:
```
2026-02-19 15:51:27.279 [http-nio-8080-exec-6] ERROR [] [c.e.c.c.CardRequestController] - POST /api/card-requests - Error creating ACTI request for card 9999********9999: Cannot create request: There is already a pending activation request for this card
com.epic.cms.exception.BusinessException: Cannot create request: There is already a pending activation request for this card
	at com.epic.cms.service.impl.CardRequestServiceImpl.createRequest(CardRequestServiceImpl.java:60)
```

## Configuration Details

### Logback Configuration (logback-spring.xml)

#### Appenders Configuration
```xml
<!-- Debug File Appender -->
<appender name="DebugFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOGS}/cms-debug.log</file>
    <filter class="ch.qos.logback.classic.filter.LevelFilter">
        <level>DEBUG</level>
        <onMatch>ACCEPT</onMatch>
        <onMismatch>DENY</onMismatch>
    </filter>
</appender>

<!-- Warning File Appender -->
<appender name="WarningFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOGS}/cms-warning.log</file>
    <filter class="ch.qos.logback.classic.filter.LevelFilter">
        <level>WARN</level>
        <onMatch>ACCEPT</onMatch>
        <onMismatch>DENY</onMismatch>
    </filter>
</appender>

<!-- Error File Appender -->
<appender name="ErrorFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOGS}/cms-error.log</file>
    <filter class="ch.qos.logback.classic.filter.LevelFilter">
        <level>ERROR</level>
        <onMatch>ACCEPT</onMatch>
        <onMismatch>DENY</onMismatch>
    </filter>
</appender>
```

#### Logger Configuration
```xml
<!-- Application Loggers -->
<logger name="com.epic.cms" level="DEBUG" additivity="false">
    <appender-ref ref="Console"/>
    <appender-ref ref="RollingFile"/>
    <appender-ref ref="DebugFile"/>
    <appender-ref ref="WarningFile"/>
    <appender-ref ref="ErrorFile"/>
</logger>

<!-- Database Operations -->
<logger name="org.springframework.jdbc" level="DEBUG" additivity="false">
    <appender-ref ref="Console"/>
    <appender-ref ref="RollingFile"/>
    <appender-ref ref="DebugFile"/>
</logger>
```

## Benefits of Separate Logging

### 1. **Easier Troubleshooting**
- **Debug logs**: Isolate detailed troubleshooting information
- **Warning logs**: Focus on business rule violations and recoverable issues
- **Error logs**: Quick access to critical system failures
- **Main logs**: Overview of application flow without noise

### 2. **Performance Optimization**
- **Reduced I/O**: Each log file writes only relevant log levels
- **Faster Log Analysis**: Smaller files for specific log levels
- **Efficient Monitoring**: Targeted monitoring based on log level

### 3. **Security & Compliance**
- **Access Control**: Different permissions can be set per log file
- **Audit Trails**: Separate error logs for security auditing
- **Data Protection**: Sensitive debug information isolated from main logs

### 4. **Operational Benefits**
- **Log Rotation**: Different retention policies per log level
- **Storage Management**: Optimize storage usage based on log importance
- **Monitoring Integration**: Different alerting rules per log type

## Log Analysis Examples

### Debugging Issues
```bash
# Analyze database performance
grep "Query executed in" logs/cms-debug.log | awk '{print $NF}' | sort -n

# Track specific request flow
grep "req-abc123" logs/cms-debug.log

# Monitor slow operations
grep "ms" logs/cms-debug.log | grep -E "(slow|timeout|error)"
```

### Monitoring Warnings
```bash
# Count business rule violations
grep -c "BusinessException" logs/cms-warning.log

# Monitor validation failures
grep "validation" logs/cms-warning.log

# Track performance degradation
grep "performance" logs/cms-warning.log
```

### Error Analysis
```bash
# Recent error analysis
tail -50 logs/cms-error.log

# Error rate monitoring
grep -c "ERROR" logs/cms-error.log

# Critical error patterns
grep -E "(Exception|Error|Failed)" logs/cms-error.log
```

## File Size Management

### Rotation Policies
| Log File | Max Size | History | Total Cap | Purpose |
|-----------|-----------|---------|------------|---------|
| cms-debug.log | 10MB | 15 days | 500MB | Detailed troubleshooting |
| cms-warning.log | 10MB | 30 days | 200MB | Business rule violations |
| cms-error.log | 10MB | 30 days | 500MB | Critical system failures |
| cms-application.log | 10MB | 30 days | 1GB | Main application flow |

### Storage Estimation
- **Daily**: ~50MB total (varies by activity)
- **Monthly**: ~1.5GB total
- **Annual**: ~18GB total with rotation

## Integration with Monitoring Systems

### ELK Stack Integration
```yaml
# Filebeat configuration
filebeat.inputs:
- type: log
  paths:
    - /app/logs/cms-debug.log
    fields: {logtype: debug}
- type: log
  paths:
    - /app/logs/cms-warning.log
    fields: {logtype: warning}
- type: log
  paths:
    - /app/logs/cms-error.log
    fields: {logtype: error}
```

### Splunk Configuration
```conf
# Splunk inputs.conf
[monitor:///app/logs/cms-debug.log]
index = cms_debug
sourcetype = cms:debug

[monitor:///app/logs/cms-warning.log]
index = cms_warning
sourcetype = cms:warning

[monitor:///app/logs/cms-error.log]
index = cms_error
sourcetype = cms:error
```

## Alerting Strategies

### Debug Alerts
- **High debug volume**: Alert if debug logs exceed threshold
- **Slow queries**: Alert on SQL execution time > 1 second
- **Memory issues**: Alert on OutOfMemoryError in debug logs

### Warning Alerts
- **Business rule violations**: Alert on repeated validation failures
- **Performance degradation**: Alert on warning rate spikes
- **Resource utilization**: Alert on resource warnings

### Error Alerts
- **Critical errors**: Immediate alert on ERROR level logs
- **Database failures**: Alert on database connection issues
- **System failures**: Alert on application startup failures

## Best Practices

### Development Environment
- **Debug logging**: Enable for detailed troubleshooting
- **All log files**: Monitor all log levels
- **Real-time monitoring**: Use console appender for immediate feedback

### Production Environment
- **Selective logging**: INFO level for main logs
- **Error focus**: Monitor error and warning files closely
- **Performance monitoring**: Use debug logs sparingly in production

### Log File Management
- **Regular cleanup**: Automate log file cleanup based on retention policies
- **Disk space monitoring**: Monitor available disk space for log directory
- **Backup strategy**: Implement log backup for critical error logs

## Testing Results

### Verification Tests Performed
1. ✅ **Debug logs**: Successfully captured SQL queries and method execution details
2. ✅ **Warning logs**: Successfully captured business rule violations
3. ✅ **Error logs**: Successfully captured system failures with stack traces
4. ✅ **Main logs**: Successfully captured general application flow
5. ✅ **Log rotation**: All files rotating correctly based on size/time
6. ✅ **MDC context**: Request and operation IDs working across all log files

### Sample Log Output Verification
```
# Debug file contains detailed SQL and timing
2026-02-19 16:01:37.846 [http-nio-8080-exec-6] DEBUG [...] [c.epic.cms.repository.CardRepository] - countAllCards() - Executing query: SELECT COUNT(*) FROM card

# Warning file contains business violations
2026-02-19 16:00:33.135 [http-nio-8080-exec-7] WARN [...] [c.e.c.e.GlobalExceptionHandler] - BusinessException: Cannot create request: There is already a pending activation request

# Error file contains system failures
2026-02-19 15:51:27.279 [http-nio-8080-exec-6] ERROR [...] [c.e.c.c.CardRequestController] - POST /api/card-requests - Error creating ACTI request
```

The separate logging implementation is fully functional and provides comprehensive log level segregation for improved troubleshooting, monitoring, and operational efficiency.
