package service;

import model.Produit;
import java.util.List;

/**
 * Interface du service métier de gestion des produits.
 */
public interface ProduitService {

    /** Crée un nouveau produit.
     * @param produit produit à créer */
    void creer_produit(Produit produit);

    /** @return liste de tous les produits */
    List<Produit> getAllProduits();

    /** Recherche par id.
     * @param id identifiant
     * @return produit ou null */
    Produit getProduitById(int id);

    /** Recherche par nom. @param nom nom @return produit ou null */
    Produit getProduitByNom(String nom);

    /** Met à jour un produit. @param produit produit modifié */
    void updateProduit(Produit produit);

    /** Supprime un produit.
     * @param nom nom du produit */
    void deleteProduit(String nom);

    /** Vérifie si un nom de produit existe déjà.
     * @param nom nom à vérifier @return true si existant */
    boolean existsByNom(String nom);
}