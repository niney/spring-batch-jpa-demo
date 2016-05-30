package niney.spring.jpa;

import niney.spring.jpa.entity.User;
import org.springframework.batch.item.ItemProcessor;

public class UserProcessor implements ItemProcessor<User, User> {
    @Override
    public User process(User item) throws Exception {
        item.setName(item.getName().toUpperCase());
        return item;
    }
}
