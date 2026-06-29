package org.esup_portail.esup_stage.config;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = StructureJpaRepository.class)
public class DatasourceConfiguration {

    @Autowired
    private AppliProperties appliProperties;

    @Autowired
    private Environment environment;

    @Bean
    public DataSource getDataSource() {
        AppliProperties.DatasourceProperties datasource = appliProperties.getDatasource();

        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(datasource.getDriver())
                .url(datasource.getUrl())
                .username(datasource.getUsername())
                .password(datasource.getPassword())
                .build();

        applyHikariConfiguration(dataSource);
        return dataSource;
    }

    private void applyHikariConfiguration(HikariDataSource dataSource) {
        Integer maximumPoolSize = environment.getProperty("appli.datasource.hikari.maximum-pool-size", Integer.class);
        if (maximumPoolSize != null) {
            dataSource.setMaximumPoolSize(maximumPoolSize);
        }

        Integer minimumIdle = environment.getProperty("appli.datasource.hikari.minimum-idle", Integer.class);
        if (minimumIdle != null) {
            dataSource.setMinimumIdle(minimumIdle);
        }

        Long connectionTimeoutMs = environment.getProperty("appli.datasource.hikari.connection-timeout-ms", Long.class);
        if (connectionTimeoutMs != null) {
            dataSource.setConnectionTimeout(connectionTimeoutMs);
        }

        Long validationTimeoutMs = environment.getProperty("appli.datasource.hikari.validation-timeout-ms", Long.class);
        if (validationTimeoutMs != null) {
            dataSource.setValidationTimeout(validationTimeoutMs);
        }

        Long idleTimeoutMs = environment.getProperty("appli.datasource.hikari.idle-timeout-ms", Long.class);
        if (idleTimeoutMs != null) {
            dataSource.setIdleTimeout(idleTimeoutMs);
        }

        Long maxLifetimeMs = environment.getProperty("appli.datasource.hikari.max-lifetime-ms", Long.class);
        if (maxLifetimeMs != null) {
            dataSource.setMaxLifetime(maxLifetimeMs);
        }

        Long keepaliveTimeMs = environment.getProperty("appli.datasource.hikari.keepalive-time-ms", Long.class);
        if (keepaliveTimeMs != null) {
            dataSource.setKeepaliveTime(keepaliveTimeMs);
        }

        Long leakDetectionThresholdMs = environment.getProperty("appli.datasource.hikari.leak-detection-threshold-ms", Long.class);
        if (leakDetectionThresholdMs != null) {
            dataSource.setLeakDetectionThreshold(leakDetectionThresholdMs);
        }

        String poolName = environment.getProperty("appli.datasource.hikari.pool-name");
        if (StringUtils.hasText(poolName)) {
            dataSource.setPoolName(poolName);
        }
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
        springLiquibase.setDataSource(getDataSource());
        springLiquibase.setContexts(appliProperties.getDatasource().getContext());
        return springLiquibase;
    }
}
