package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import mpv5.usermanagement.MPSecurityManager;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private static final int ADMIN = MPSecurityManager.RIGHT_TO_ADMINISTRATE;
    private static final int USER = MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE;

    private final List<UserService.Account> others = Arrays.asList(
            new UserService.Account(1, "admin", ADMIN, true),
            new UserService.Account(2, "kasse", USER, true));

    @Test
    void newUserNeedsLoginAndPassword() {
        UserService.Account n = new UserService.Account(-1, "", USER, true);
        assertEquals(2, UserService.check(n, new char[0], new char[0], others).size());
        UserService.Account ok = new UserService.Account(-1, "lager", USER, true);
        assertTrue(UserService.check(ok, "geheim".toCharArray(), "geheim".toCharArray(), others).isEmpty());
    }

    @Test
    void loginMustBeUniqueAndWithoutSpaces() {
        assertEquals(1, UserService.check(new UserService.Account(-1, "Kasse", USER, true),
                "geheim".toCharArray(), "geheim".toCharArray(), others).size());
        assertEquals(1, UserService.check(new UserService.Account(-1, "max muster", USER, true),
                "geheim".toCharArray(), "geheim".toCharArray(), others).size());
        //the user itself does not count
        assertTrue(UserService.check(new UserService.Account(2, "kasse", USER, true),
                new char[0], new char[0], others).isEmpty());
    }

    @Test
    void passwordMustBeRepeatedAndLongEnough() {
        UserService.Account u = new UserService.Account(2, "kasse", USER, true);
        assertEquals(1, UserService.check(u, "geheim".toCharArray(), "geheIm".toCharArray(), others).size());
        assertEquals(1, UserService.check(u, "abc".toCharArray(), "abc".toCharArray(), others).size());
        //empty: the password stays
        assertTrue(UserService.check(u, new char[0], new char[0], others).isEmpty());
    }

    @Test
    void theLastAdministratorStays() {
        //turning the only administrator into a user or disabling him: refused
        assertEquals(1, UserService.check(new UserService.Account(1, "admin", USER, true), new char[0], new char[0], others).size());
        assertEquals(1, UserService.check(new UserService.Account(1, "admin", ADMIN, false), new char[0], new char[0], others).size());
        assertNotNull(UserService.whyNotDelete(others.get(0), 2, others));
        //not oneself, but any other user
        assertNotNull(UserService.whyNotDelete(others.get(1), 2, others));
        assertNull(UserService.whyNotDelete(others.get(1), 1, others));
    }
}
