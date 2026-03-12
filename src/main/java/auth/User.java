package auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur authentifié du système.
 */
public class User {
    public enum Role { ADMIN, VENDEUR, CLIENT }

    private int id;
    private String username;
    private String passwordHash;
    private Role role;
    private boolean active;

    public User(int id, String username, String passwordHash, Role role) {
        this.id = id; this.username = username;
        this.passwordHash = passwordHash; this.role = role;
        this.active = true;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public void setRole(Role role) { this.role = role; }

    /** Vérifie si l'utilisateur possède une permission donnée. */
    public boolean hasPermission(Permission p) {
        return getRolePermissions(role).contains(p);
    }

    private List<Permission> getRolePermissions(Role r) {
        List<Permission> perms = new ArrayList<>();
        switch (r) {
            case ADMIN:
                for (Permission p : Permission.values()) perms.add(p);
                break;
            case VENDEUR:
                perms.add(Permission.VOIR_PRODUITS);
                perms.add(Permission.AJOUTER_PRODUIT);
                perms.add(Permission.MODIFIER_PRODUIT);
                perms.add(Permission.SUPPRIMER_PRODUIT);
                perms.add(Permission.VOIR_STATS);
                break;
            case CLIENT:
                perms.add(Permission.VOIR_PRODUITS);
                perms.add(Permission.PASSER_COMMANDE);
                break;
        }
        return perms;
    }
}