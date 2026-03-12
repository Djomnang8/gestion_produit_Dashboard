package service;

import java.time.LocalDate;
import java.util.*;

/**
 * Utilitaire de suivi des ventes par produit, mois et année.
 * Données conservées en mémoire (singleton statique).
 * Déplacé dans le package service pour supprimer le dossier util.
 */
public class SalesTracker {

    // Map : nomProduit -> (année -> (mois -> quantité vendue))
    private static final Map<String, Map<Integer, Map<Integer, Integer>>> sales = new LinkedHashMap<>();

    /**
     * Enregistre une vente pour un produit à la date courante.
     * @param nomProduit nom du produit vendu
     * @param quantite   quantité vendue
     */
    public static void recordSale(String nomProduit, int quantite) {
        LocalDate now = LocalDate.now();
        sales.computeIfAbsent(nomProduit, k -> new TreeMap<>())
                .computeIfAbsent(now.getYear(), k -> new TreeMap<>())
                .merge(now.getMonthValue(), quantite, Integer::sum);
    }

    /**
     * Retourne les ventes mensuelles d'un produit pour une année donnée.
     * @param nomProduit nom du produit
     * @param year       année
     * @return tableau de 12 entiers (janvier = index 0)
     */
    public static int[] getMonthlySales(String nomProduit, int year) {
        int[] result = new int[12];
        Map<Integer, Map<Integer, Integer>> byYear = sales.get(nomProduit);
        if (byYear != null) {
            Map<Integer, Integer> byMonth = byYear.get(year);
            if (byMonth != null) byMonth.forEach((m, q) -> result[m - 1] = q);
        }
        return result;
    }

    /**
     * Retourne les ventes totales par produit (toutes années confondues).
     * @return map nomProduit → total vendu
     */
    public static Map<String, Integer> getTotalSalesPerProduct() {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (var entry : sales.entrySet()) {
            int total = entry.getValue().values().stream()
                    .flatMap(m -> m.values().stream()).mapToInt(Integer::intValue).sum();
            totals.put(entry.getKey(), total);
        }
        return totals;
    }

    /**
     * @return les années pour lesquelles des données de ventes existent
     */
    public static Set<Integer> getAvailableYears() {
        Set<Integer> years = new TreeSet<>();
        sales.values().forEach(m -> years.addAll(m.keySet()));
        if (years.isEmpty()) years.add(LocalDate.now().getYear());
        return years;
    }

    /**
     * @return tous les noms de produits enregistrés dans le tracker
     */
    public static Set<String> getProducts() { return sales.keySet(); }
}