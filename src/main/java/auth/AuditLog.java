package auth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Journal d'activité (audit log) : enregistre toutes les actions utilisateurs.
 * Singleton utilisé pour journaliser qui a modifié quoi et quand.
 */
public class AuditLog {
    public record Entry(String username, String action, String detail, LocalDateTime timestamp) {
        @Override public String toString() {
            return String.format("[%s] %s : %s (%s)",
                timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                username, action, detail);
        }
    }

    private static final List<Entry> logs = new ArrayList<>();

    /**
     * Enregistre une nouvelle entrée dans le journal.
     * @param username nom de l'utilisateur ayant effectué l'action
     * @param action   type d'action (ex: "AJOUT_PRODUIT")
     * @param detail   détail de l'action
     */
    public static void log(String username, String action, String detail) {
        logs.add(new Entry(username, action, detail, LocalDateTime.now()));
        System.out.println("[AUDIT] " + new Entry(username, action, detail, LocalDateTime.now()));
    }

    /** @return la liste complète des entrées du journal */
    public static List<Entry> getLogs() { return new ArrayList<>(logs); }

    /** @return les N dernières entrées */
    public static List<Entry> getLastN(int n) {
        int size = logs.size();
        return new ArrayList<>(logs.subList(Math.max(0, size - n), size));
    }
}
