package org.esup_portail.esup_stage.monitoring.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataSourcePoolMonitor {

    private static final Logger log = LoggerFactory.getLogger(DataSourcePoolMonitor.class);

    private final DataSource dataSource;
    private final boolean enabled;
    private final int warnActiveConnections;
    private final int warnWaitingThreads;
    private final int warnUsagePercent;
    private final AtomicBoolean hikariNotFoundLogged = new AtomicBoolean(false);

    public DataSourcePoolMonitor(
            DataSource dataSource,
            @Value("${monitoring.datasource.pool.enabled:true}") boolean enabled,
            @Value("${monitoring.datasource.pool.warn-active-connections:24}") int warnActiveConnections,
            @Value("${monitoring.datasource.pool.warn-waiting-threads:1}") int warnWaitingThreads,
            @Value("${monitoring.datasource.pool.warn-usage-percent:85}") int warnUsagePercent
    ) {
        this.dataSource = dataSource;
        this.enabled = enabled;
        this.warnActiveConnections = warnActiveConnections;
        this.warnWaitingThreads = warnWaitingThreads;
        this.warnUsagePercent = warnUsagePercent;
    }

    @Scheduled(
            initialDelayString = "${monitoring.datasource.pool.initial-delay-ms:15000}",
            fixedDelayString = "${monitoring.datasource.pool.fixed-delay-ms:30000}"
    )
    public void logPoolStatus() {
        if (!enabled) {
            return;
        }

        HikariDataSource hikariDataSource = resolveHikariDataSource();
        if (hikariDataSource == null) {
            if (hikariNotFoundLogged.compareAndSet(false, true)) {
                log.warn("Datasource pool monitor enabled but datasource is not HikariDataSource (type={})", dataSource.getClass().getName());
            }
            return;
        }

        HikariPoolMXBean pool = hikariDataSource.getHikariPoolMXBean();
        if (pool == null) {
            return;
        }

        int total = pool.getTotalConnections();
        int active = pool.getActiveConnections();
        int idle = pool.getIdleConnections();
        int waiting = pool.getThreadsAwaitingConnection();
        int usagePercent = total > 0 ? Math.round((active * 100.0f) / total) : 0;

        boolean warning = active >= warnActiveConnections
                || waiting >= warnWaitingThreads
                || usagePercent >= warnUsagePercent;

        if (warning) {
            log.warn("DB_POOL status=HIGH pool={} total={} active={} idle={} waiting={} usage={}%, thresholds(active>={}, waiting>={}, usage>={}%)",
                    hikariDataSource.getPoolName(), total, active, idle, waiting, usagePercent,
                    warnActiveConnections, warnWaitingThreads, warnUsagePercent);
        } else {
            log.debug("DB_POOL status=OK pool={} total={} active={} idle={} waiting={} usage={}%",
                    hikariDataSource.getPoolName(), total, active, idle, waiting, usagePercent);
        }
    }

    private HikariDataSource resolveHikariDataSource() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            return hikariDataSource;
        }

        try {
            return dataSource.unwrap(HikariDataSource.class);
        } catch (SQLException e) {
            return null;
        }
    }
}
