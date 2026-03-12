package service;

import auth.AuthService;
import repository.ProduitRepository;
import model.Produit;
import java.util.List;

/**
 * Implémentation du service produit.
 * Vérifie les doublons avant insertion.
 */
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository repo;

    /** @param repo dépôt de persistance à utiliser */
    public ProduitServiceImpl(ProduitRepository repo) { this.repo = repo; }

    /**
     * Crée un produit après vérification de l'unicité du nom.
     * @param produit produit à créer
     * @throws IllegalArgumentException si le nom existe déjà
     */
    @Override
    public void creer_produit(Produit produit) {
        if (existsByNom(produit.getNom()))
            throw new IllegalArgumentException("Un produit avec ce nom existe déjà : " + produit.getNom());
        repo.save(produit);
    }

    @Override
    public List<Produit> getAllProduits() { return repo.getAllProduits(); }

    @Override
    public Produit getProduitById(int id) {
        return repo.getAllProduits().stream()
                .filter(p -> p.getId() == id)
                .findFirst().orElse(null);
    }

    @Override
    public Produit getProduitByNom(String nom) { return repo.getProduitByNom(nom); }

    /**
     * Met à jour un produit existant.
     * @param produit produit avec les nouvelles valeurs
     */
    @Override
    public void updateProduit(Produit produit) {
        repo.update(produit);
    }

    /**
     * Supprime un produit par son nom.
     * @param nom nom du produit à supprimer
     */
    @Override
    public void deleteProduit(String nom) {
        repo.deleteProduit(nom);
    }

    /**
     * Vérifie si un produit de même nom existe déjà.
     * @param nom nom à vérifier
     * @return true si existant
     */
    @Override
    public boolean existsByNom(String nom) { return repo.getProduitByNom(nom) != null; }
}