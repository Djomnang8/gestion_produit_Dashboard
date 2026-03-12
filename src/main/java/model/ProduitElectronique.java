package model;

/**
 * Représente un produit électronique avec une durée de garantie.
 * La TVA applicable est fixée à 20%.
 */
public class ProduitElectronique extends Produit {

    private int dureeGarantie;
    private static final double TVA = 0.20;

    /**
     * @param nom           Nom du produit
     * @param prixHT        Prix hors taxe
     * @param quantiteStock Quantité en stock
     * @param dureeGarantie Durée de garantie en mois
     */
    public ProduitElectronique(String nom, double prixHT, int quantiteStock, int dureeGarantie) {
        super(nom, prixHT, quantiteStock);
        this.dureeGarantie = dureeGarantie;
    }

    /** @return la durée de garantie en mois */
    public int getDureeGarantie() { return dureeGarantie; }
    /** @param dureeGarantie nouvelle durée de garantie en mois */
    public void setDureeGarantie(int dureeGarantie) { this.dureeGarantie = dureeGarantie; }

    /** Affiche les informations complètes du produit électronique. */
    @Override
    public void afficherInfos() {
        System.out.println("Produit Electronique : " + getNom());
        System.out.println("Prix HT : " + getPrixHT());
        System.out.println("Stock : " + getQuantiteStock());
        System.out.println("Garantie : " + dureeGarantie + " mois");
        System.out.println("Prix TTC (20%) : " + calculerPrixTTC(TVA));
    }
}