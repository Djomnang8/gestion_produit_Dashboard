package Model;

public class ProduitElectronique extends Produit {

    private int dureeGarantie;
    private final double TVA = 0.20;

    public ProduitElectronique(String nom, double prixHT, int quantiteStock, int dureeGarantie) {
        super(nom, prixHT, quantiteStock);
        this.dureeGarantie = dureeGarantie;
    }

    public int getDureeGarantie() {
        return dureeGarantie;
    }

    public void setDureeGarantie(int dureeGarantie) {
        this.dureeGarantie = dureeGarantie;
    }

    //Affiche les informations pour les produits electroniques
    public void afficherInfos() {
        System.out.println("Produit Electronique : " + getNom());
        System.out.println("Prix HT : " + getPrixHT());
        System.out.println("Stock : " + getQuantiteStock());
        System.out.println("Garantie : " + dureeGarantie + " mois");
        System.out.println("Prix TTC (20%) : " + calculerPrixTTC(TVA));
    }
}