import controller.ProduitController;
import repository.ProduitRepository;
import repository.ProduitRepositorySql;
import service.ProduitServiceImpl;
import view.LoginView;
import db.Connecter;

import java.sql.Connection;

public class Main {

    /**
     * Point d'entrée principal de l'application StockManager Pro.
     * Lance la fenêtre de connexion au démarrage.
     * **/
    public static void main(String[] args) {

        // Connexion à la base de données
        Connection connection = Connecter.connecter();
        if (connection == null) {
            System.out.println("Impossible de se connecter à la base.");
            return;
        }

        ProduitRepository produitRepository = new ProduitRepositorySql(connection);
        ProduitServiceImpl produitService = new ProduitServiceImpl(produitRepository);
        ProduitController produitController = new ProduitController(produitService);

        new LoginView(produitController);
    }
}