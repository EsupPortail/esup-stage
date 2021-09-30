package fr.dauphine.estage.config;

import fr.dauphine.estage.bootstrap.ApplicationBootstrap;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfiguration {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Bean
    public DataSource getDataSource()
    {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(applicationBootstrap.getAppConfig().getDatasourceDriver());
        dataSourceBuilder.url(applicationBootstrap.getAppConfig().getDatasourceUrl());
        dataSourceBuilder.username(applicationBootstrap.getAppConfig().getDatasourceUsername());
        dataSourceBuilder.password(applicationBootstrap.getAppConfig().getDatasourcePassword());
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
