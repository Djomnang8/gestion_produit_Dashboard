package Controller;

import Model.Produit;
import Service.ProduitService;
import java.util.List;

public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    public void creer_produit(Produit produit) {
        produitService.creer_produit(produit);
    }

    public List<Produit> getAllProduits() {
        return produitService.getAllProduits();
    }

    public Produit getProduitById(int id) {
        return produitService.getProduitById(id);
    }

    public Produit getProduitByNom(String nom) {
        return produitService.getProduitByNom(nom);
    }

    public void updateProduit(Produit produit) {
        produitService.updateProduit(produit);
    }

    public void deleteProduit(String nom) {
        produitService.deleteProduit(nom);
    }
}