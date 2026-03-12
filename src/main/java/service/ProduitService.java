package Service;

import Model.Produit;
import java.util.List;

public interface ProduitService {
    void creer_produit(Produit produit);

    List<Produit> getAllProduits();

    Produit getProduitById(int id);

    Produit getProduitByNom(String nom);

    void updateProduit(Produit produit);

    void deleteProduit(String nom);
}