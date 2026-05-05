package org.catalog.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan({"org.catalog.services", "org.catalog.repositories", "org.catalog.mappers"})
@PropertySource("classpath:application.properties")
public class PersistenceConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(env.getProperty("db.url"));
        cfg.setUsername(env.getProperty("db.username"));
        cfg.setPassword(env.getProperty("db.password"));
        cfg.setDriverClassName(Objects.requireNonNull(env.getProperty("db.driver")));
        cfg.setMaximumPoolSize(10);
        return new HikariDataSource(cfg);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource, LocalValidatorFactoryBean validator) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("org.catalog.entities");

        Properties props = hibernateProperties();
        props.put("jakarta.persistence.validation.factory", validator);
        sessionFactory.setHibernateProperties(props);
        return sessionFactory;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();

        String[] propertyKeys =
                {
                        "hibernate.dialect",
                        "hibernate.hbm2ddl.auto",
                        "hibernate.show_sql",
                        "hibernate.cache.use_second_level_cache",
                        "hibernate.cache.use_query_cache",
                        "hibernate.cache.region.factory_class",
                        "hibernate.javax.cache.provider",
                        "jakarta.persistence.sharedCache.mode",
                        "hibernate.generate_statistics",
                        "hibernate.javax.cache.missing_cache_strategy",
                        "hibernate.javax.cache.uri"
                };

        for (String key : propertyKeys) {
            String value = env.getProperty(key);
            if (value != null) {
                properties.put(key, value);
            }
        }

        return properties;
    }
}
