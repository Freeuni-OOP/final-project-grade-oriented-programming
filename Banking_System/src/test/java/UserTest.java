import Entities.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {


    @Test
    public void testGetFirstName() {
        User user = new User(67,"Giga", "Beradze", BigDecimal.valueOf(10000000L));

        assertEquals("Giga", user.getFirstName());
        assertEquals("Beradze", user.getLastName());
        assertEquals(BigDecimal.valueOf(10000000L), user.getAccountBalance());
    }

}
