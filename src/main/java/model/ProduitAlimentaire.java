package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Représente un produit alimentaire avec une date de péremption.
 * La TVA applicable est fixée à 5,5%.
 */
public class ProduitAlimentaire extends Produit {

    private String datePeremption;
    private static final double TVA = 0.055;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * @param nom             Nom du produit
     * @param prixHT          Prix hors taxe
     * @param quantiteStock   Quantité en stock
     * @param datePeremption  Date de péremption au format DD-MM-YYYY
     */
    public ProduitAlimentaire(String nom, double prixHT, int quantiteStock, String datePeremption) {
        super(nom, prixHT, quantiteStock);
        this.datePeremption = datePeremption;
    }

    /** @return la date de péremption au format DD-MM-YYYY */
    public String getDatePeremption() { return datePeremption; }
    /** @param datePeremption nouvelle date de péremption */
    public void setDatePeremption(String datePeremption) { this.datePeremption = datePeremption; }

    /**
     * Calcule le prix TTC en appliquant la TVA alimentaire fixe de 5,5%.
     * @param tva ignoré, la TVA fixe de 5,5% est toujours utilisée
     * @return prix TTC
     */
    @Override
    public double calculerPrixTTC(double tva) { return super.calculerPrixTTC(TVA); }

    /** Affiche les informations complètes du produit alimentaire. */
    @Override
    public void afficherInfos() {
        System.out.println("Produit Alimentaire : " + getNom());
        System.out.println("Prix HT : " + getPrixHT());
        System.out.println("Stock : " + getQuantiteStock());
        System.out.println("Date péremption : " + datePeremption);
        System.out.println("Prix TTC (5.5%) : " + calculerPrixTTC(TVA));
    }

    /**
     * Vérifie si le produit est périmé (date dépassée).
     * @return true si périmé
     */
    public boolean estPerime() {
        try {
            LocalDate peremption = LocalDate.parse(datePeremption, DATE_FORMAT);
            return peremption.isBefore(LocalDate.now());
        } catch (Exception e) { return false; }
    }
}