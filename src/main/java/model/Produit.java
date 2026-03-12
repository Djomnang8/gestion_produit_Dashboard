package Model;

public abstract class Produit implements Stockable {

    private int id;
    private String nom;
    private double prixHT;
    private int quantiteStock;
    private String type;

    public Produit(String nom, double prixHT, int quantiteStock) {
        this.nom = nom;

        if (prixHT < 0) {
            this.prixHT = 0;
        } else {
            this.prixHT = prixHT;
        }

        if (quantiteStock < 0) {
            this.quantiteStock = 0;
        } else {
            this.quantiteStock = quantiteStock;
        }
    }

    // Recupere les information entrer pour le produit
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrixHT() {
        return prixHT;
    }

    public void setPrixHT(double prixHT) {
        if (prixHT >= 0)
            this.prixHT = prixHT;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        if (quantiteStock >= 0)
            this.quantiteStock = quantiteStock;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // Calcule le prix TTC selon le taux fourni
    public double calculerPrixTTC(double tva) {
        return prixHT * (1 + tva);
    }

    public boolean estDisponible() {
        return quantiteStock > 0;
    }

    // Augmente quantité disponible
    public void ajouterStock(int quantite) {
        if (quantite > 0)
            this.quantiteStock = quantiteStock + quantite;
    }

    // Diminu quantité disponible
    public void retirerStock(int quantite) {
        if (quantite > quantiteStock) {
            System.out.println("Stock insuffisant !");
        } else {
            this.quantiteStock = quantiteStock - quantite;
        }
    }

    public int getStock() {
        return quantiteStock;
    }

    // affiche les informations de chaque sous classe
    public abstract void afficherInfos();

}