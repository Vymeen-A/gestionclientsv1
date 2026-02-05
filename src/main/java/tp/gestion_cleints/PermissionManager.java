package tp.gestion_cleints;

import javafx.scene.Node;

public class PermissionManager {

    public static boolean canManageUsers(User user) {
        return user != null && user.isAdmin();
    }

    public static boolean canDelete(User user) {
        return user != null && user.isAdmin();
    }

    public static boolean canWrite(User user) {
        return user != null && (user.isAdmin() || user.getRole() == Role.ACCOUNTANT);
    }

    public static void applyPermissions(User user, Node... nodes) {
        if (user == null)
            return;
        boolean canWrite = canWrite(user);
        for (Node node : nodes) {
            node.setVisible(canWrite);
            node.setManaged(canWrite);
        }
    }

    public static void applyAdminOnly(User user, Node... nodes) {
        if (user == null)
            return;
        boolean isAdmin = user.isAdmin();
        for (Node node : nodes) {
            node.setVisible(isAdmin);
            node.setManaged(isAdmin);
        }
    }

    public static void applyAdminOnlyForMenuItems(User user, javafx.scene.control.MenuItem... items) {
        if (user == null)
            return;
        boolean isAdmin = user.isAdmin();
        for (javafx.scene.control.MenuItem item : items) {
            item.setVisible(isAdmin);
        }
    }

    public static void applyPermissionsForMenuItems(User user, javafx.scene.control.MenuItem... items) {
        if (user == null)
            return;
        boolean canWrite = canWrite(user);
        for (javafx.scene.control.MenuItem item : items) {
            item.setVisible(canWrite);
        }
    }
}
