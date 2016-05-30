package niney.spring.jpa.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataBaseConfig {
    private static final String DEFAULT_NAMING_STRATEGY
            = "org.hibernate.cfg.DefaultNamingStrategy";

    private static final String IMPROVE_NAMING_STRATEGY
            = "org.hibernate.cfg.ImprovedNamingStrategy";

    // ---------------------------------------------------
    // batch
    @Bean
    @ConfigurationProperties(prefix = "dataSource.batch")
    public DataSource userDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "entityManagerFactoryBasic")
    public LocalContainerEntityManagerFactoryBean batchEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        return builder.dataSource(userDataSource())
                .packages("niney.spring.jpa.entity")
                .properties(batchAdditionalJpaProperties())
                .build();
    }

    Map<String, String> batchAdditionalJpaProperties() {
        Map<String, String> propertiesHashMap = new HashMap<>();
        propertiesHashMap.put("hibernate.ejb.naming_strategy", IMPROVE_NAMING_STRATEGY);

        // 초기화 할때 만 쓴다
        propertiesHashMap.put("hibernate.hbm2ddl.auto", "");
//        propertiesHashMap.put("show-sql", "true");
//        propertiesHashMap.put("hibernate.format_sql", "true");

        return propertiesHashMap;
    }

    @Bean(name = "transactionManagerBasic")
    @Primary
    PlatformTransactionManager userTransactionManagerMain(
            EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(batchEntityManagerFactory(builder).getObject());
    }

    @Configuration
    @EnableJpaRepositories(
            basePackages= "niney.spring.jpa.repo",
            entityManagerFactoryRef = "entityManagerFactoryBasic",
            transactionManagerRef = "transactionManagerBasic")
    static class DbUserJpaRepositoriesConfig {
    }


}