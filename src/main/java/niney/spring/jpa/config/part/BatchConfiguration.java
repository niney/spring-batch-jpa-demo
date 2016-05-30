package niney.spring.jpa.config.part;

import niney.spring.jpa.UserProcessor;
import niney.spring.jpa.entity.User;
import niney.spring.jpa.part.JpaPartItemReader;
import niney.spring.jpa.part.RangePartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    @Autowired
    PlatformTransactionManager transactionManager;


    @Bean
    public Partitioner partitioner() {
        return new RangePartitioner();
    }

    // tag::readerwriterprocessor[]
    @Bean
    public JpaPagingItemReader<User> reader() {
        return new JpaPartItemReader();
    }

    @Bean
    public UserProcessor processor() {
        return new UserProcessor();
    }

    @Bean
    public JpaItemWriter<User> writer() {
        JpaItemWriter<User> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactoryBean.getObject());
        return writer;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob() {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .flow(step0())
                .end()
                .build();
    }

    @Bean
    public Step step0() {
        return stepBuilderFactory.get("step0")
                .partitioner("part", partitioner())
                .step(step1())
                .gridSize(1)
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<User, User> chunk(3)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .transactionManager(transactionManager)
                .build();
    }
    // end::jobstep[]


}
