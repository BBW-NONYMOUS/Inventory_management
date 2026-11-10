package inventory;

/**
 * The currently signed-in user.
 */
public final class Session {

    private static User user;

    private Session() {
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User u) {
        user = u;
    }

    public static void clear() {
        user = null;
    }

    public static boolean isAdmin() {
        return user != null && user.isAdmin();
    }

    public static int userId() {
        return user == null ? 0 : user.getId();
    }
}
