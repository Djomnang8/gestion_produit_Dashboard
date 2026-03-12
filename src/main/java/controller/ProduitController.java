package controller;

import model.Produit;
import service.ProduitService;
import java.util.List;

/**
 * Contrôleur de la gestion des produits.
 * Fait le lien entre les vues et le service métier.
 */
public class ProduitController {
    private final ProduitService service;

    /** @param service service métier produit */
    public ProduitController(ProduitService service) { this.service = service; }

    /** Crée un produit via le service. @param produit produit à créer */
    public void creer_produit(Produit produit) { service.creer_produit(produit); }

    /** @return liste de tous les produits */
    public List<Produit> getAllProduits() { return service.getAllProduits(); }

    /** @param id identifiant @return produit ou null */
    public Produit getProduitById(int id) { return service.getProduitById(id); }

    /** @param nom nom du produit @return produit ou null */
    public Produit getProduitByNom(String nom) { return service.getProduitByNom(nom); }

    /** Met à jour un produit. @param produit produit modifié */
    public void updateProduit(Produit produit) { service.updateProduit(produit); }

    /** Supprime un produit par nom. @param nom nom du produit */
    public void deleteProduit(String nom) { service.deleteProduit(nom); }

    /** Vérifie si le nom est déjà utilisé. @param nom nom à vérifier @return true si existant */
    public boolean existsByNom(String nom) { return service.existsByNom(nom); }
}