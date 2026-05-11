package org.catalog.testsupport;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.catalog.services.DatabaseSeeder;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan(
        basePackages = {"org.catalog.services", "org.catalog.repositories", "org.catalog.mappers"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DatabaseSeeder.class
        )
)
public class TestPersistenceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(MySqlTestContainer.INSTANCE.getJdbcUrl());
        cfg.setUsername(MySqlTestContainer.INSTANCE.getUsername());
        cfg.setPassword(MySqlTestContainer.INSTANCE.getPassword());
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setMaximumPoolSize(5);
        return new HikariDataSource(cfg);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource, LocalValidatorFactoryBean validator) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("org.catalog.entities");

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.hbm2ddl.auto", "create");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");

        props.put("hibernate.cache.use_second_level_cache", "true");
        props.put("hibernate.cache.use_query_cache", "true");
        props.put("hibernate.cache.region.factory_class", "jcache");
        props.put("hibernate.javax.cache.provider",
                "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
        props.put("jakarta.persistence.sharedCache.mode", "ENABLE_SELECTIVE");
        props.put("hibernate.generate_statistics", "true");
        props.put("hibernate.javax.cache.missing_cache_strategy", "fail");
        props.put("hibernate.javax.cache.uri", "caffeine-jcache.conf");

        props.put("jakarta.persistence.validation.factory", validator);

        sessionFactory.setHibernateProperties(props);
        return sessionFactory;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager tm) {
        return new TransactionTemplate(tm);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
