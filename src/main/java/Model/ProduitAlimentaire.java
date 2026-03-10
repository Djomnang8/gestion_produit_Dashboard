package Model;

public class ProduitAlimentaire extends Produit {

    private String datePeremption;
    private final double TVA = 0.055;

    public ProduitAlimentaire(String nom, double prixHT, int quantiteStock, String datePeremption) {
        super(nom, prixHT, quantiteStock);
        this.datePeremption = datePeremption;
    }

    public String getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(String datePeremption) {
        this.datePeremption = datePeremption;
    }

    // Calcule prix avec TVA fournis
    public double calculerPrixTTC(double tva) {
        return super.calculerPrixTTC(TVA);
    }

    // Affiche les informations pour les produits alimentaires
    public void afficherInfos() {
        System.out.println("Produit Alimentaire : " + getNom());
        System.out.println("Prix HT : " + getPrixHT());
        System.out.println("Stock : " + getQuantiteStock());
        System.out.println("Date péremption : " + datePeremption);
        System.out.println("Prix TTC (5.5%) : " + calculerPrixTTC(TVA));
    }
}