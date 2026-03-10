package Service;

import Repository.ProduitRepository;
import Model.Produit;

import java.util.List;

public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    public ProduitServiceImpl(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Override
    public void creer_produit(Produit produit) {
        produitRepository.save(produit);
    }

    public List<Produit> getAllProduits() {
        return produitRepository.getAllProduits();
    }

    @Override
    public Produit getProduitById(int id) {
        return null;
    }

    public Produit getProduitByNom(String nom) {

        return produitRepository.getProduitByNom(nom);
    }

    public void updateProduit(Produit produit) {
        produitRepository.update(produit);
    }

    public void deleteProduit(String nom) {
        produitRepository.deleteProduit(nom);
    }
}