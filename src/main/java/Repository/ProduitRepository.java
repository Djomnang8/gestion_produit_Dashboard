package Repository;

import Model.Produit;
import java.util.List;

public interface ProduitRepository {

    // Enregistre les produits en memoire
    void save(Produit produit);

    //affiche la liste des produits
    List<Produit> getAllProduits();

    //rechercher un produit par nom
    Produit getProduitByNom(String nom);

    void update(Produit produit);

    void deleteProduit(String nom);
}