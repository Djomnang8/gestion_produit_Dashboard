import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.ProduitAlimentaire;
import model.ProduitElectronique;


/**
 * Tests unitaires JUnit 5 pour les modèles de produits.
 * Vérifie la création, les contraintes de stock/prix et les calculs de TVA.
 *
 * <p>Lancer avec : {@code mvn test} ou via l'IDE (Run JUnit).</p>
 */
@DisplayName("Tests de la gestion des produits")
class ProduitTest {

    private ProduitAlimentaire farine;
    private ProduitElectronique pc;

    /**
     * Initialise les produits de test avant chaque méthode.
     * farine : alimentaire 1500 F, stock 10, date "01/07/2026"
     * pc     : électronique 300 000 F, stock 20, garantie 48 mois
     */
    @BeforeEach
    void setUp() {
        farine = new ProduitAlimentaire("farine", 1500, 10, "01/07/2026");
        pc     = new ProduitElectronique("PC", 300000, 20, 48);
    }

    @Test
    @DisplayName("Création d'un produit alimentaire valide")
    void testCreationProduitAlimentaire() {
        assertEquals("farine", farine.getNom());
        assertEquals(1500,        farine.getPrixHT());
        assertEquals(10,          farine.getQuantiteStock());
        assertEquals("01/07/2026", farine.getDatePeremption());
    }

    @Test
    @DisplayName("Création d'un produit électronique valide")
    void testCreationProduitElectronique() {
        assertEquals("PC",    pc.getNom());
        assertEquals(300000,  pc.getPrixHT());
        assertEquals(20,      pc.getQuantiteStock());
        assertEquals(48,      pc.getDureeGarantie());
    }

    @Test
    @DisplayName("Prix négatif à la création doit être ramené à 0")
    void testPrixInvalide() {
        ProduitAlimentaire produit = new ProduitAlimentaire("mais", -500, 5, "2024-12-31");
        assertEquals(0, produit.getPrixHT(),
                "Un prix inférieur à 0 devrait être traité comme 0");
    }

    @Test
    @DisplayName("Stock négatif à la création doit être ramené à 0")
    void testStockInvalide() {
        ProduitElectronique produit = new ProduitElectronique("souris", 5000, -3, 12);
        assertEquals(0, produit.getQuantiteStock(),
                "Un stock inférieur à 0 devrait être traité comme 0");
    }

    @Test
    @DisplayName("Ajout de stock valide")
    void testAjouterStock() {
        farine.ajouterStock(5);
        assertEquals(15, farine.getQuantiteStock(),
                "Le stock devrait être mis à jour après l'ajout");

        pc.ajouterStock(10);
        assertEquals(30, pc.getQuantiteStock(),
                "Le stock devrait être mis à jour après l'ajout");
    }

    @Test
    @DisplayName("Retrait de stock valide")
    void testRetirerStock() {
        farine.retirerStock(2);
        assertEquals(8, farine.getQuantiteStock(),
                "Le stock devrait être mis à jour après le retrait");

        pc.retirerStock(4);
        assertEquals(16, pc.getQuantiteStock(),
                "Le stock devrait être mis à jour après le retrait");
    }

    @Test
    @DisplayName("Retrait impossible si stock insuffisant")
    void testRetirerStockInsuffisant() {
        farine.retirerStock(3);
        assertEquals(7, farine.getStock(),
                "Le stock ne doit pas changer si retrait impossible");

        pc.retirerStock(5);
        assertEquals(15, pc.getQuantiteStock(),
                "Le stock de PC doit passer de 20 à 15");
    }

    @Test
    @DisplayName("Peu importe le taux, la TVA alimentaire doit toujours être 5,5%")
    void testPrixTTCAlimentaire() {
        double prixTTC = farine.calculerPrixTTC(0.10);
        assertEquals(1582.5, prixTTC, 0.001,
                "1500 * 1.055 = 1582.5");
    }

    @Test
    @DisplayName("Peu importe le taux, la TVA électronique doit toujours être 20%")
    void testPrixTTCElectronique() {
        double prixTTC = pc.calculerPrixTTC(0.30);
        assertEquals(360000, prixTTC, 0.001,
                "300000 * 1.20 = 360000");
    }
}