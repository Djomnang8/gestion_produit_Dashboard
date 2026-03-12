package auth;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur authentifié du système.
 * Deux rôles : VENDEUR et CLIENT.
 *
 * Permissions disponibles :
 *   VOIR_PRODUITS, AJOUTER_PRODUIT, MODIFIER_PRODUIT, SUPPRIMER_PRODUIT,
 *   VOIR_STATS, PASSER_COMMANDE
 */
public class User {

    // ── Rôles ────────────────────────────────────────────────────────────────
    public enum Role { VENDEUR, CLIENT }

    // ── Permissions (anciennement dans Permission.java) ───────────────────────
    public enum Permission {
        VOIR_PRODUITS,
        AJOUTER_PRODUIT,
        MODIFIER_PRODUIT,
        SUPPRIMER_PRODUIT,
        VOIR_STATS,
        PASSER_COMMANDE
    }

    // ── Champs ────────────────────────────────────────────────────────────────
    private int    id;
    private String username;
    private String passwordHash;
    private Role   role;
    private boolean active;

    public User(int id, String username, String passwordHash, Role role) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.active       = true;
    }

    // ── Accesseurs ────────────────────────────────────────────────────────────
    public int     getId()           { return id; }
    public String  getUsername()     { return username; }
    public String  getPasswordHash() { return passwordHash; }
    public Role    getRole()         { return role; }
    public boolean isActive()        { return active; }
    public void    setActive(boolean active) { this.active = active; }
    public void    setRole(Role role)        { this.role = role; }

    /**
     * Vérifie si l'utilisateur possède une permission donnée.
     * @param p permission à vérifier
     * @return true si l'utilisateur possède cette permission
     */
    public boolean hasPermission(Permission p) {
        return getRolePermissions(role).contains(p);
    }

    private List<Permission> getRolePermissions(Role r) {
        List<Permission> perms = new ArrayList<>();
        switch (r) {
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