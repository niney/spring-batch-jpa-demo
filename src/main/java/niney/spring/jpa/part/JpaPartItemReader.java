package niney.spring.jpa.part;

import niney.spring.jpa.entity.User;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

public class JpaPartItemReader extends JpaPagingItemReader<User> {

    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    @PostConstruct
    public void init() {
        this.setEntityManagerFactory(entityManagerFactoryBean.getObject());
        this.setQueryString("select i from User i where id between :min and :max");
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        Map<String, Object> paramMap = new HashMap<>();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        if(executionContext.get("minValue") == null) {
            return;
        }
        long minValue = executionContext.getLong("minValue");
        long maxValue = executionContext.getLong("maxValue");
        paramMap.put("min", minValue);
        paramMap.put("max", maxValue);
        this.setParameterValues(paramMap);
    }

}
