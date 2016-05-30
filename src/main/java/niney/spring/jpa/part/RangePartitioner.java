package niney.spring.jpa.part;

import niney.spring.jpa.entity.User;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;

public class RangePartitioner implements Partitioner {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> q = cb.createQuery(Object.class);

        Root<User> root = q.from(User.class);

        String dataPK = "id";
        CriteriaQuery<Object> select = q.multiselect(
                cb.count(root),
                cb.min(root.<Number>get(dataPK)),
                cb.max(root.<Number>get(dataPK))
        );
        Object[] result = (Object[]) entityManager
                .createQuery(select)
                .getSingleResult();
        long idCount = (Long) result[0];
        if(idCount == 0) {
            return new HashMap<String, ExecutionContext>();
        }

        long min = ((Long) result[1]); // 해당 클래스의 id 와 같은 type
        long max = ((Long) result[2]);

        long targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> rangeResult = new HashMap<String, ExecutionContext>();
        int number = 0;
        long start = min;
        long end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            rangeResult.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }
            value.putLong("minValue", start);
            value.putLong("maxValue", end);
            start += targetSize;
            end += targetSize;
            number++;
        }

        return rangeResult;
    }
}
