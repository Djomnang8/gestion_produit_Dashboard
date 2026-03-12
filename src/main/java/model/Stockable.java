package model;

public interface Stockable {

    void ajouterStock(int quantite);

    void retirerStock(int quantite);

    // Retourne la quantité actuelle disponible
    int getStock();
}