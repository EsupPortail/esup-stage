package org.esup_portail.esup_stage.config;

import liquibase.integration.spring.SpringLiquibase;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfiguration {

    @Autowired
    private AppliProperties appliProperties;

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(appliProperties.getDatasource().getDriver());
        dataSourceBuilder.url(appliProperties.getDatasource().getUrl());
        dataSourceBuilder.username(appliProperties.getDatasource().getUsername());
        dataSourceBuilder.password(appliProperties.getDatasource().getPassword());
        return dataSourceBuilder.build();
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
        springLiquibase.setDataSource(getDataSource());
        return springLiquibase;
    }
}