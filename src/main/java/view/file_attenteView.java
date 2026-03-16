package service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * File d'attente partagée (singleton) pour les commandes en attente de validation vendeur.
 * Thread-safe via CopyOnWriteArrayList.
 */
public class file_attenteView {

    // ── Représentation d'une commande en attente ──────────────────────────────
    public static class PendingOrder {
        private static int counter = 1;

        public final int    id;
        public final String clientUsername;
        public final String nomProduit;
        public final int    quantite;
        public final double prixUnitaire;
        public       Status status;

        public enum Status { ATTENTE, ACCEPTEE, REFUSEE }

        public PendingOrder(String clientUsername, String nomProduit, int quantite, double prixUnitaire) {
            this.id             = counter++;
            this.clientUsername = clientUsername;
            this.nomProduit     = nomProduit;
            this.quantite       = quantite;
            this.prixUnitaire   = prixUnitaire;
            this.status         = Status.ATTENTE;
        }

        public double getTotal() { return prixUnitaire * quantite; }
    }

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static final file_attenteView INSTANCE = new file_attenteView();
    public static file_attenteView getInstance() { return INSTANCE; }

    private final List<PendingOrder>        orders    = new CopyOnWriteArrayList<>();
    private final List<Consumer<PendingOrder>> listeners = new CopyOnWriteArrayList<>();

    private file_attenteView() {}

    // ── API publique ──────────────────────────────────────────────────────────

    /**
     * Soumet une commande côté client.
     * Notifie tous les listeners enregistrés (ex : ProduitView).
     */
    public PendingOrder submit(String clientUsername, String nomProduit, int quantite, double prixUnitaire) {
        PendingOrder order = new PendingOrder(clientUsername, nomProduit, quantite, prixUnitaire);
        orders.add(order);
        listeners.forEach(l -> l.accept(order));
        return order;
    }

    /** @return commandes dont le statut est ATTENTE */
    public List<PendingOrder> getPending() {
        List<PendingOrder> result = new ArrayList<>();
        for (PendingOrder o : orders)
            if (o.status == PendingOrder.Status.ATTENTE) result.add(o);
        return result;
    }

    /** @return toutes les commandes */
    public List<PendingOrder> getAll() { return new ArrayList<>(orders); }

    /**
     * Enregistre un listener appelé à chaque nouvelle commande soumise.
     * Utilisé par ProduitView pour rafraîchir son panneau de notifications.
     */
    public void addListener(Consumer<PendingOrder> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<PendingOrder> listener) {
        listeners.remove(listener);
    }
}