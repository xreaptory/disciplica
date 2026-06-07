package com.disciplica.integration;

import model.springdata.repository.CompletionRepository;
import model.springdata.repository.HabitRepository;
import model.springdata.repository.UserSpringRepository;
import model.springdata.service.HabitService;
import model.springdata.service.HabitServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "model.springdata.repository")
public class IntegrationTestConfig {
    private static final String DB_URL = "jdbc:sqlite:" + resolveDbPath();

    private static String resolveDbPath() {
        Path path = Paths.get("target", "integration-tests.db").toAbsolutePath();
        return path.toString();
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl(DB_URL);
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("model.domain.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        em.setJpaPropertyMap(props);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

    @Bean
    public HabitService habitService(HabitRepository habitRepository,
                                     UserSpringRepository userRepository,
                                     CompletionRepository completionRepository) {
        return new HabitServiceImpl(habitRepository, userRepository, completionRepository);
    }
}
