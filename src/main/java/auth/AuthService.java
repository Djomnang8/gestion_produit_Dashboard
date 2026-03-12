package auth;

import db.Connecter;
import java.sql.*;
import java.util.*;

/**
 * Service d'authentification et de gestion des utilisateurs.
 * Les utilisateurs inscrits sont persistés en base de données (table utilisateurs).
 * Le compte Vendeur est prédéfini et rechargé depuis la DB au démarrage.
 * Seuls deux rôles existent : VENDEUR et CLIENT.
 */
public class AuthService {

    private static final Map<String, User> users = new LinkedHashMap<>();
    private static User currentUser = null;

    static {
        // Compte vendeur prédéfini garanti même si la DB est vide
        insererSiAbsent("vendeur1", "vendeur123", User.Role.VENDEUR);
        // Charger tous les utilisateurs depuis la base
        chargerDepuisDB();
    }

    // ── Persistance DB ───────────────────────────────────────────────────────

    /**
     * Charge tous les utilisateurs depuis la table {@code utilisateurs}.
     * Complète la map en mémoire sans écraser les comptes déjà présents.
     */
    private static void chargerDepuisDB() {
        String sql = "SELECT username, password_hash, role, active FROM utilisateurs";
        try (Connection con = Connecter.connecter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                if (!users.containsKey(username)) {
                    String roleStr = rs.getString("role").toUpperCase();
                    // Ne charger que les rôles valides (VENDEUR, CLIENT)
                    if (!roleStr.equals("VENDEUR") && !roleStr.equals("CLIENT")) continue;
                    User.Role role = User.Role.valueOf(roleStr);
                    User u = new User(users.size() + 1, username, rs.getString("password_hash"), role);
                    u.setActive(rs.getBoolean("active"));
                    users.put(username, u);
                }
            }
        } catch (Exception e) {
            System.err.println("[AuthService] Impossible de charger les utilisateurs depuis la DB : " + e.getMessage());
        }
    }

    /**
     * Insère un utilisateur en DB s'il n'existe pas déjà.
     * Utilisé pour garantir le compte prédéfini Vendeur.
     */
    private static void insererSiAbsent(String username, String password, User.Role role) {
        if (!users.containsKey(username)) {
            users.put(username, new User(users.size() + 1, username, password, role));
        }
        String check  = "SELECT COUNT(*) FROM utilisateurs WHERE username = ?";
        String insert = "INSERT INTO utilisateurs (username, password_hash, role, active) VALUES (?, ?, ?, TRUE)";
        try (Connection con = Connecter.connecter()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(check)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ins = con.prepareStatement(insert)) {
                        ins.setString(1, username);
                        ins.setString(2, password);
                        ins.setString(3, role.name());
                        ins.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AuthService] Erreur vérification/insertion compte prédéfini : " + e.getMessage());
        }
    }

    /**
     * Persiste un nouvel utilisateur CLIENT en base de données.
     * @return true si l'insertion a réussi
     */
    private static boolean sauvegarderEnDB(String username, String password, User.Role role) {
        String sql = "INSERT INTO utilisateurs (username, password_hash, role, active) VALUES (?, ?, ?, TRUE)";
        try (Connection con = Connecter.connecter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.name());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("[AuthService] Erreur lors de l'enregistrement en DB : " + e.getMessage());
            return false;
        }
    }

    // ── API publique ─────────────────────────────────────────────────────────

    /**
     * Inscrit un nouveau CLIENT en mémoire ET en base de données.
     * @param username nom d'utilisateur souhaité
     * @param password mot de passe en clair
     * @return true si l'inscription a réussi, false si le nom existe déjà
     */
    public static boolean inscrireClient(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) return false;
        if (users.containsKey(username)) return false;

        boolean dbOk = sauvegarderEnDB(username, password, User.Role.CLIENT);
        // Ajout en mémoire même si la DB échoue (mode dégradé)
        users.put(username, new User(users.size() + 1, username, password, User.Role.CLIENT));
        return true;
    }

    /**
     * Authentifie un utilisateur.
     * @return l'utilisateur connecté, ou null si échec
     */
    public static User login(String username, String password) {
        User u = users.get(username);
        if (u != null && u.isActive() && u.getPasswordHash().equals(password)) {
            currentUser = u;
            return u;
        }
        return null;
    }

    /** Déconnecte l'utilisateur courant. */
    public static void logout() {
        currentUser = null;
    }

    /** @return l'utilisateur actuellement connecté, ou null */
    public static User getCurrentUser() { return currentUser; }

    /**
     * Vérifie si l'utilisateur connecté possède la permission donnée.
     * @param p permission à vérifier
     * @return true si autorisé
     */
    public static boolean isAllowed(User.Permission p) {
        return currentUser != null && currentUser.hasPermission(p);
    }

    /** @return collection de tous les utilisateurs enregistrés */
    public static Collection<User> getAllUsers() { return users.values(); }

    /**
     * Vérifie si un nom d'utilisateur est déjà pris.
     * @param username nom à vérifier
     * @return true si le nom est déjà utilisé
     */
    public static boolean usernameExists(String username) {
        return users.containsKey(username);
    }
}